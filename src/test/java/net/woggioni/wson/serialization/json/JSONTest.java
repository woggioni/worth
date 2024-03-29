package net.woggioni.wson.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.SneakyThrows;
import net.woggioni.jwo.LookAheadTextInputStream;
import net.woggioni.wson.exception.NotImplementedException;
import net.woggioni.wson.value.ArrayValue;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Parser;
import net.woggioni.wson.xface.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class JSONTest {

    private String[] testFiles = new String[]{"/test.json", "/wordpress.json"};

    public static InputStream getTestSource(String filename) {
        return JSONTest.class.getResourceAsStream(filename);
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
                ArrayValue array;
                if (jsonNode.getNodeType() == JsonNodeType.ARRAY && (array = (ArrayValue) value).size() == jsonNode.size()) {
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
                if (jsonNode.getNodeType() == JsonNodeType.OBJECT) {
                    ObjectValue object = (ObjectValue) value;
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

    private interface Callback {
        void call(Value value, JsonNode jsonNode);
    }

    private boolean compareValueAndJsonNode(Value value, JsonNode jsonNode, Callback cb) {
        switch (value.type()) {
            case NULL: {
                boolean result = jsonNode.getNodeType() == JsonNodeType.NULL;
                if (result) return true;
                else {
                    cb.call(value, jsonNode);
                    return false;
                }
            }
            case INTEGER:
                if (jsonNode.getNodeType() == JsonNodeType.NUMBER) {
                    boolean result = value.asInteger() == jsonNode.asLong();
                    if (result) return true;
                    else {
                        cb.call(value, jsonNode);
                        return false;
                    }
                } else {
                    cb.call(value, jsonNode);
                    return false;
                }
            case DOUBLE:
                if (jsonNode.getNodeType() == JsonNodeType.NUMBER) {
                    boolean result = value.asFloat() == jsonNode.asDouble();
                    if (result) return true;
                    else {
                        cb.call(value, jsonNode);
                        return false;
                    }
                } else {
                    cb.call(value, jsonNode);
                    return false;
                }
            case BOOLEAN:
                if (jsonNode.getNodeType() == JsonNodeType.BOOLEAN) {
                    boolean result = value.asBoolean() == jsonNode.asBoolean();
                    if (result) return true;
                    else {
                        cb.call(value, jsonNode);
                        return false;
                    }
                } else {
                    cb.call(value, jsonNode);
                    return false;
                }
            case STRING:
                if (jsonNode.getNodeType() == JsonNodeType.STRING) {
                    boolean result = value.asString().equals(jsonNode.asText());
                    if (result) return true;
                    else {
                        cb.call(value, jsonNode);
                        return false;
                    }
                } else {
                    cb.call(value, jsonNode);
                    return false;
                }
            case ARRAY:
                ArrayValue array;
                if (jsonNode.getNodeType() == JsonNodeType.ARRAY &&
                        (array = (ArrayValue) value).size() == jsonNode.size()) {
                    for (int i = 0; i < array.size(); i++) {
                        if (!compareValueAndJsonNode(array.get(i), jsonNode.get(i), cb)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    cb.call(value, jsonNode);
                    return false;
                }
            case OBJECT:
                if (jsonNode.getNodeType() == JsonNodeType.OBJECT) {
                    ObjectValue object = (ObjectValue) value;
                    for (Map.Entry<String, Value> entry : object) {
                        if (!jsonNode.has(entry.getKey())) {
                            cb.call(value, jsonNode);
                            return false;
                        } else if (!compareValueAndJsonNode(entry.getValue(), jsonNode.get(entry.getKey()), cb)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    cb.call(value, jsonNode);
                    return false;
                }
            default:
                throw new NotImplementedException("This should never happen");
        }
    }

    @Test
    @SneakyThrows
    public void consistencyTest() {
        Value.Configuration cfg = Value.Configuration.builder()
            .objectValueImplementation(ObjectValue.Implementation.ArrayList).build();
        for (String testFile : testFiles) {
            Parser parser = new JSONParser(cfg);
            Value parsedValue = parser.parse(getTestSource(testFile));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JSONDumper.newInstance().dump(parsedValue, baos);
            String dumpedJSON = new String(baos.toByteArray());
            byte[] barray = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(barray);
            parser = new JSONParser(cfg);
            Value reParsedValue = parser.parse(bais);
            Assertions.assertEquals(parsedValue, reParsedValue);
            baos = new ByteArrayOutputStream();
            JSONDumper.newInstance().dump(reParsedValue, baos);
            String reDumpedJSON = new String(baos.toByteArray());
            Assertions.assertEquals(dumpedJSON, reDumpedJSON);
        }
    }

    @Test
    @SneakyThrows
    public void comparativeTest() {
        ObjectMapper om = new ObjectMapper();
        for (String testFile : testFiles) {
            JsonNode jsonNode = om.readTree(getTestSource(testFile));
            Value value = new JSONParser().parse(getTestSource(testFile));
            Assertions.assertTrue(compareValueAndJsonNode(value, jsonNode, (v, j) -> {
                Assertions.fail("Difference found");
            }));
        }
    }

    @Test
    @SneakyThrows
    public void hexTest() {
        String hex = "1F608";
        byte[] buffer = hex.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        LookAheadTextInputStream ltis = new LookAheadTextInputStream(new InputStreamReader(bais));
        ltis.read();
        int result = JSONParser.parseHex(ltis);
        Assertions.assertEquals(Integer.parseInt(hex, 16), result);
    }
}
