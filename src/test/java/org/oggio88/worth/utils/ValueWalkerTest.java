package org.oggio88.worth.utils;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.oggio88.worth.serialization.json.JSONParser;
import org.oggio88.worth.xface.Parser;
import org.oggio88.worth.xface.Value;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Optional;

public class ValueWalkerTest {

    @Test
    @SneakyThrows
    public void test() {
        Value value;
        try(InputStream is = new BufferedInputStream(getClass().getResourceAsStream("/test.json"))) {
            Parser parser = JSONParser.newInstance();
            value = parser.parse(is);
        }
        ValueWalker valueWalker = new ValueWalker(value);
        Optional<String> text = valueWalker.get("widget").get("image").get("tags").get(1).map(Value::asString);
        Assert.assertTrue(text.isPresent());
        Assert.assertEquals("Amazon", text.get());
    }
}
