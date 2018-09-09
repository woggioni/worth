package org.oggio88.worth.serialization;

import lombok.RequiredArgsConstructor;
import org.oggio88.worth.exception.NotImplementedException;
import org.oggio88.worth.utils.WorthUtils;
import org.oggio88.worth.value.ArrayValue;
import org.oggio88.worth.value.BooleanValue;
import org.oggio88.worth.value.FloatValue;
import org.oggio88.worth.value.IntegerValue;
import org.oggio88.worth.value.ObjectValue;
import org.oggio88.worth.value.StringValue;
import org.oggio88.worth.xface.Parser;
import org.oggio88.worth.xface.Value;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Stack;

public class ValueParser implements Parser {

    @RequiredArgsConstructor
    protected static class StackLevel {
        public final Value value;
    }

    protected static class ArrayStackLevel extends StackLevel {
        public ArrayStackLevel() {
            super(new ArrayValue());
        }
    }

    protected static class ObjectStackLevel extends StackLevel {
        public String currentKey;

        public ObjectStackLevel() {
            super(ObjectValue.newInstance());
        }
    }

    protected Stack<StackLevel> stack;

    private void add2Last(Value value) {
        StackLevel last = stack.lastElement();
        ArrayStackLevel asl;
        ObjectStackLevel osl;
        if ((asl = WorthUtils.dynamicCast(last, ArrayStackLevel.class)) != null)
            asl.value.add(value);
        else if ((osl = WorthUtils.dynamicCast(last, ObjectStackLevel.class)) != null) {
            osl.value.put(osl.currentKey, value);
            osl.currentKey = null;
        }
    }

    protected ValueParser() {
        stack = new Stack<>();
        stack.push(new ArrayStackLevel());
    }

    @Override
    public Value parse(InputStream is) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public Value parse(Reader reader) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public Value parse(InputStream stream, Charset encoding) {
        return parse(new InputStreamReader(stream, encoding));
    }

    protected void beginObject() {
        stack.push(new ObjectStackLevel());
    }

    protected void endObject() {
        add2Last(stack.pop().value);
    }

    protected void beginArray() {
        stack.push(new ArrayStackLevel());
    }

    protected void endArray() {
        add2Last(stack.pop().value);
    }

    protected void objectKey(String key) {
        ObjectStackLevel osl = (ObjectStackLevel) stack.lastElement();
        osl.currentKey = key;
    }

    protected void stringValue(String value) {
        add2Last(new StringValue(value));
    }

    protected void integerValue(long value) {
        add2Last(new IntegerValue(value));
    }

    protected void floatValue(double value) {
        add2Last(new FloatValue(value));
    }

    protected void booleanValue(boolean value) {
        add2Last(new BooleanValue(value));
    }

    protected void nullValue() {
        add2Last(Value.Null);
    }
}
