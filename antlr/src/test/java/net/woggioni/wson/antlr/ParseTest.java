package net.woggioni.wson.antlr;

import lombok.SneakyThrows;
import net.woggioni.wson.serialization.json.JSONDumper;
import net.woggioni.wson.xface.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;

import static net.woggioni.wson.test.JsonBomb.infiniteJson;

public class ParseTest {

    @Test
    @SneakyThrows
    public void test() {
        CodePointCharStream inputStream = CharStreams.fromReader(new InputStreamReader(getClass().getResourceAsStream("/test.json")));
        JSONLexer lexer = new JSONLexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        JSONParser parser = new JSONParser(commonTokenStream);
        JSONListenerImpl listener = new JSONListenerImpl();
        ParseTreeWalker walker = new ParseTreeWalker();
 		walker.walk(listener, parser.json());
        Value result = listener.result;
        new JSONDumper().dump(result, System.out);
    }

    @Test
    @Disabled
    @SneakyThrows
    public void antlr() {
        CharStream inputStream = CharStreams.fromReader(new InputStreamReader(infiniteJson()));
        JSONLexer lexer = new JSONLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        net.woggioni.wson.antlr.JSONParser parser = new net.woggioni.wson.antlr.JSONParser(commonTokenStream);
        JSONListenerImpl listener = new JSONListenerImpl();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, parser.json());
    }
}
