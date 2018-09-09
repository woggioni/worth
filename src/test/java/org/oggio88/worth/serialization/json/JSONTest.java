package org.oggio88.worth.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.oggio88.worth.buffer.CircularBuffer;
import org.oggio88.worth.exception.NotImplementedException;
import org.oggio88.worth.utils.WorthUtils;
import org.oggio88.worth.value.ArrayValue;
import org.oggio88.worth.value.ObjectValue;
import org.oggio88.worth.xface.Parser;
import org.oggio88.worth.xface.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Map;

public class JSONTest {

    private String[] testFiles = new String[]{"/test.json", "/wordpress.json"};

    private InputStream getTestSource(String filename) {
        return getClass().getResourceAsStream(filename);
    }

    private boolean compareValueAndJsonNode(Value value, JsonNode jsonNode) {
        switch (value.type()) {
            case NULL:
                return jsonNode.getNodeType() == JsonNodeType.NULL;
            case INTEGER:
                if (jsonNode.getNodeType() == JsonNodeType.NUMBER) {
                    return value.asInteger() == jsonNode.asLong();
                } else {
                    return false;
                }
            case DOUBLE:
                if (jsonNode.getNodeType() == JsonNodeType.NUMBER) {
                    return value.asFloat() == jsonNode.asDouble();
                } else {
                    return false;
                }
            case BOOLEAN:
                if (jsonNode.getNodeType() == JsonNodeType.BOOLEAN) {
                    return value.asBoolean() == jsonNode.asBoolean();
                } else {
                    return false;
                }
            case STRING:
                if (jsonNode.getNodeType() == JsonNodeType.STRING) {
                    return value.asString().equals(jsonNode.asText());
                } else {
                    return false;
                }
            case ARRAY:
                ArrayValue array = WorthUtils.dynamicCast(value, ArrayValue.class);
                if (jsonNode.getNodeType() == JsonNodeType.ARRAY && array.size() == jsonNode.size()) {
                    for (int i = 0; i < array.size(); i++) {
                        if (!compareValueAndJsonNode(array.get(i), jsonNode.get(i))) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            case OBJECT:
                ObjectValue object = WorthUtils.dynamicCast(value, ObjectValue.class);
                if (jsonNode.getNodeType() == JsonNodeType.OBJECT) {
                    for (Map.Entry<String, Value> entry : object) {
                        if (!jsonNode.has(entry.getKey())) {
                            return false;
                        } else if (!compareValueAndJsonNode(entry.getValue(), jsonNode.get(entry.getKey())))
                            return false;
                    }
                    return true;
                } else {
                    return false;
                }
            default:
                throw new NotImplementedException("This should never happen");
        }
    }

    @Test
    @SneakyThrows
    public void consistencyTest() {
        System.setProperty("org.oggio88.javason.value.ObjectValue.preserveKeyOrder", "true");
        for (String testFile : testFiles) {
            Parser parser = new JSONParser();
            Value parsedValue = parser.parse(getTestSource(testFile));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JSONDumper.newInstance().dump(parsedValue, baos);
            String dumpedJSON = new String(baos.toByteArray());
            byte[] barray = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(barray);
            parser = new JSONParser();
            Value reParsedValue = parser.parse(bais);
            Assert.assertEquals(parsedValue, reParsedValue);
            baos = new ByteArrayOutputStream();
            JSONDumper.newInstance().dump(reParsedValue, baos);
            String reDumpedJSON = new String(baos.toByteArray());
            Assert.assertEquals(dumpedJSON, reDumpedJSON);
        }
    }

    @Test
    @SneakyThrows
    public void comparativeTest() {
        for (String testFile : testFiles) {
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(getTestSource(testFile));
            Value value = new JSONParser().parse(getTestSource(testFile));
            Assert.assertTrue(compareValueAndJsonNode(value, jsonNode));
        }
    }

    @Test
    @SneakyThrows
    public void hexTest() {
        String hex = "1F608";
        byte[] buffer = new String(hex).getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        Method method = JSONParser.class.getDeclaredMethod("parseHex", CircularBuffer.class);
        method.setAccessible(true);
        int result = (int) method.invoke(null, new CircularBuffer(new InputStreamReader(bais), 5));
        Assert.assertEquals((int) Integer.valueOf(hex, 16), result);
    }
}
