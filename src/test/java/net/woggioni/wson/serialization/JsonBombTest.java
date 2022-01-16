package net.woggioni.wson.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.wson.exception.MaxDepthExceededException;
import net.woggioni.wson.serialization.json.JSONParser;
import net.woggioni.wson.test.JsonBomb;
import net.woggioni.wson.xface.Parser;
import net.woggioni.wson.xface.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JsonBombTest {
    @Test
    @Disabled
    @SneakyThrows
    public void jackson() {
        ObjectMapper om = new ObjectMapper();
        Assertions.assertThrows(StackOverflowError.class, () -> {
            om.readTree(JsonBomb.infiniteJson());
        });
    }

    @Test
    @SneakyThrows
    public void worth() {
        Value.Configuration cfg = Value.Configuration.builder().maxDepth(1024).build();
        Parser parser = JSONParser.newInstance(cfg);
        Assertions.assertThrows(MaxDepthExceededException.class, () -> {
            parser.parse(JsonBomb.infiniteJson());
        });
    }
}
