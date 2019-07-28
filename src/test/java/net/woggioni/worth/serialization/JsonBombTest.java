package net.woggioni.worth.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.worth.exception.MaxDepthExceededException;
import net.woggioni.worth.serialization.json.JSONParser;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;
import org.junit.Test;

import java.io.InputStream;

public class JsonBombTest {

    public static InputStream infiniteJson() {
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
}
