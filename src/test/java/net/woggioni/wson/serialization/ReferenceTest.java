package net.woggioni.wson.serialization;

import lombok.SneakyThrows;
import net.woggioni.jwo.JWO;
import net.woggioni.wson.serialization.binary.JBONDumper;
import net.woggioni.wson.serialization.binary.JBONParser;
import net.woggioni.wson.serialization.json.JSONDumper;
import net.woggioni.wson.serialization.json.JSONParser;
import net.woggioni.wson.value.IntegerValue;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Dumper;
import net.woggioni.wson.xface.Parser;
import net.woggioni.wson.xface.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.function.Function;

public class ReferenceTest {

    @TempDir
    Path testDir;

    @SneakyThrows
    private void common(Function<Value.Configuration, Dumper> dumperConstructor,
                        Function<Value.Configuration, Parser> parserConstructor) {
        Value.Configuration cfg = Value.Configuration.builder()
                .serializeReferences(true)
                .objectValueImplementation(ObjectValue.Implementation.HashMap)
                .build();
        Value value = ObjectValue.newInstance(cfg);
        value.put("child", value);
        value.put("id", new IntegerValue(25));

        byte[] bytes;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Dumper dumper = dumperConstructor.apply(cfg);
            dumper.dump(value, baos);
            bytes = baos.toByteArray();
        }
        JWO.writeBytes2File(testDir.resolve("ciao.jbon"), bytes);
        Value reparsedValue;
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            Parser parser = parserConstructor.apply(cfg);
            reparsedValue = parser.parse(bais);
        }
        Assertions.assertEquals(reparsedValue, reparsedValue.get("child"));
        Assertions.assertEquals(value.get("id"), reparsedValue.get("id"));
    }

    @Test
    @SneakyThrows
    public void json() {
        common(JSONDumper::new, JSONParser::new);
    }

    @Test
    @SneakyThrows
    public void jbon() {
        common(JBONDumper::new, JBONParser::new);
    }

    @Test
    public void test() {
        Value.Configuration cfg = Value.Configuration.builder().serializeReferences(true).build();
        Value root = ObjectValue.newInstance(cfg);
        root.put("child1", root);
        root.put("child2", ObjectValue.newInstance(cfg));
        new JSONDumper(cfg).dump(root, System.out);
    }
}
