package net.woggioni.worth.serialization;

import lombok.SneakyThrows;
import net.woggioni.worth.serialization.binary.JBONDumper;
import net.woggioni.worth.serialization.binary.JBONParser;
import net.woggioni.worth.serialization.json.JSONDumper;
import net.woggioni.worth.serialization.json.JSONParser;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.value.IntegerValue;
import net.woggioni.worth.value.ObjectValue;
import net.woggioni.worth.xface.Dumper;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.function.Function;

public class ReferenceTest {

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
        WorthUtils.writeBytes2File(Paths.get("/tmp/ciao.jbon"), bytes);
        Value reparsedValue;
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            Parser parser = parserConstructor.apply(cfg);
            reparsedValue = parser.parse(bais);
        }
        Assert.assertEquals(reparsedValue, reparsedValue.get("child"));
        Assert.assertEquals(value.get("id"), reparsedValue.get("id"));
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
