package net.woggioni.worth.traversal;

import lombok.SneakyThrows;
import net.woggioni.worth.serialization.json.JSONParser;
import net.woggioni.worth.serialization.json.JSONTest;
import net.woggioni.worth.utils.Tuple2;
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

    private static class TestVisitor implements ValueVisitor<Void> {

        public List<Value> preValues = new ArrayList<>();
        public List<Value> postValues = new ArrayList<>();

        @Override
        public boolean visitPre(TraversalContext<Void> ctx) {
            Value value = WorthUtils.tail(ctx.getStack()).getValue();
            preValues.add(value);
            return true;
        }

        @Override
        public void visitPost(TraversalContext<Void> ctx) {
            Value value = WorthUtils.tail(ctx.getStack()).getValue();
            postValues.add(value);
        }
    }

    private void walk(List<Value> preResult, List<Value> postResult, Value value) {
        ObjectValue ov;
        ArrayValue av;
        preResult.add(value);
        if((av = WorthUtils.dynamicCast(value, ArrayValue.class)) != null) {
            for(Value v : av) {
                walk(preResult, postResult, v);
            }
        } else if((ov = WorthUtils.dynamicCast(value, ObjectValue.class)) != null) {
            for(Map.Entry<String, Value> entry : ov) {
                walk(preResult, postResult, entry.getValue());
            }
        }
        postResult.add(value);
    }

    private Tuple2<List<Value>, List<Value>> recursiveWalk(Value root) {
        List<Value> preResult = new ArrayList<>();
        List<Value> postResult = new ArrayList<>();
        walk(preResult, postResult, root);
        return new Tuple2<>(preResult, postResult);
    }

    @Test
    public void testWalk() {
        Value v = JSONParser.newInstance().parse(JSONTest.getTestSource("/test.json"));
        TestVisitor visitor = new TestVisitor();
        ValueWalker.walk(v, visitor);
        Assert.assertFalse(visitor.preValues.isEmpty());
        Assert.assertFalse(visitor.postValues.isEmpty());
        Assert.assertEquals(recursiveWalk(v), new Tuple2<>(visitor.preValues, visitor.postValues));
    }
}
