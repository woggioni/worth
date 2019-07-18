package net.woggioni.worth.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.worth.antlr.JSONLexer;
import net.woggioni.worth.antlr.JSONListenerImpl;
import net.woggioni.worth.exception.MaxDepthExceededException;
import net.woggioni.worth.serialization.json.JSONParser;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonBombTest {

    private InputStream infiniteJson() {
        return new InputStream() {
            int index = 0;
            final String monomer = "{\"key\":[";
            @Override
            public int read() {
                return (int) monomer.charAt(index++ % monomer.length());
            }
        };
    }

    @Test(expected = StackOverflowError.class)
    @SneakyThrows
    public void jackson() {
        ObjectMapper om = new ObjectMapper();
        om.readTree(infiniteJson());
    }

    @Test(expected = MaxDepthExceededException.class)
    @SneakyThrows
    public void worth() {
        Value.Configuration cfg = Value.Configuration.builder().maxDepth(1024).build();
        Parser parser = JSONParser.newInstance(cfg);
        parser.parse(infiniteJson());
    }

    @Test(expected = OutOfMemoryError.class)
    @SneakyThrows
    public void antlr() {
        CharStream inputStream = CharStreams.fromReader(new InputStreamReader(infiniteJson()));
        JSONLexer lexer = new JSONLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        net.woggioni.worth.antlr.JSONParser parser = new net.woggioni.worth.antlr.JSONParser(commonTokenStream);
        JSONListenerImpl listener = new JSONListenerImpl();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, parser.json());
    }
}
