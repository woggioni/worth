package net.woggioni.worth.serialization;

import lombok.RequiredArgsConstructor;
import net.woggioni.worth.exception.NotImplementedException;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.value.*;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;

public class ValueParser implements Parser {

    protected final Value.Configuration cfg;

    @RequiredArgsConstructor
    protected static class StackLevel {
        public final Value value;
        public final long expectedSize;
    }

    protected static class ArrayStackLevel extends StackLevel {
        public ArrayStackLevel() {
            super(new ArrayValue(), -1);
        }
        public ArrayStackLevel(long expectedSize) {
            super(new ArrayValue(), expectedSize);
        }
    }

    protected static class ObjectStackLevel extends StackLevel {
        public String currentKey;

        public ObjectStackLevel(Value.Configuration cfg) {
            super(ObjectValue.newInstance(cfg), -1);
        }

        public ObjectStackLevel(Value.Configuration cfg, long expectedSize) {
            super(ObjectValue.newInstance(cfg), expectedSize);
        }
    }

    protected ArrayDeque<StackLevel> stack;

    private void add2Last(Value value) {
        StackLevel last = stack.getFirst();
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
        this(Value.configuration);
    }

    protected ValueParser(Value.Configuration cfg) {
        this.cfg = cfg;
        stack = new ArrayDeque<>();
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
        stack.push(new ObjectStackLevel(cfg));
    }

    protected void beginObject(long size) {
        stack.push(new ObjectStackLevel(cfg, size));
    }


    protected void endObject() {
        add2Last(stack.pop().value);
    }

    protected void beginArray() {
        stack.push(new ArrayStackLevel());
    }

    protected void beginArray(long size) {
        stack.push(new ArrayStackLevel(size));
    }

    protected void endArray() {
        add2Last(stack.pop().value);
    }

    protected void objectKey(String key) {
        ObjectStackLevel osl = (ObjectStackLevel) stack.getFirst();
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
