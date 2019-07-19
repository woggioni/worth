package net.woggioni.worth.traversal;

import lombok.SneakyThrows;
import net.woggioni.worth.serialization.json.JSONParser;
import net.woggioni.worth.serialization.json.JSONTest;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.value.*;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private static class TestVisitor implements ValueVisitor {

        public List<Value> values = new ArrayList<>();

        @Override
        public void visit(ObjectValue value, TraversalContext ctx) {
            values.add(value);
        }

        @Override
        public void visit(ArrayValue value, TraversalContext ctx) {
            values.add(value);
        }

        @Override
        public void visit(BooleanValue value, TraversalContext ctx) {
            values.add(value);
        }

        @Override
        public void visit(StringValue value, TraversalContext ctx) {
            values.add(value);
        }

        @Override
        public void visit(IntegerValue value, TraversalContext ctx) {
            values.add(value);
        }

        @Override
        public void visit(FloatValue value, TraversalContext ctx) {
            values.add(value);
        }

        @Override
        public void visit(NullValue value, TraversalContext ctx) {
            values.add(value);
        }
    }

    private void walk(List<Value> result, Value value) {
        ObjectValue ov;
        ArrayValue av;
        if((av = WorthUtils.dynamicCast(value, ArrayValue.class)) != null) {
            for(Value v : av) {
                walk(result, v);
            }
        } else if((ov = WorthUtils.dynamicCast(value, ObjectValue.class)) != null) {
            for(Map.Entry<String, Value> entry : ov) {
                walk(result, entry.getValue());
            }
        }
        result.add(value);
    }

    private List<Value> recursiveWalk(Value root) {
        List<Value> result = new ArrayList<>();
        walk(result, root);
        return result;
    }

    @Test
    public void testWalk() {
        Value v = JSONParser.newInstance().parse(JSONTest.getTestSource("/test.json"));
        TestVisitor visitor = new TestVisitor();
        ValueWalker.walk(v, visitor);
        Assert.assertFalse(visitor.values.isEmpty());
        Assert.assertEquals(recursiveWalk(v), visitor.values);
    }
}
