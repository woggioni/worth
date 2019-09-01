package net.woggioni.worth.serialization;

import lombok.RequiredArgsConstructor;
import net.woggioni.worth.exception.MaxDepthExceededException;
import net.woggioni.worth.exception.NotImplementedException;
import net.woggioni.worth.exception.ParseException;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.value.ArrayValue;
import net.woggioni.worth.value.BooleanValue;
import net.woggioni.worth.value.FloatValue;
import net.woggioni.worth.value.IntegerValue;
import net.woggioni.worth.value.ObjectValue;
import net.woggioni.worth.value.StringValue;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.woggioni.worth.utils.WorthUtils.newThrowable;

public class ValueParser implements Parser {

    protected final Value.Configuration cfg;

    @RequiredArgsConstructor
    protected static class StackLevel {
        public final Value value;
        public final long expectedSize;
    }

    protected static class ArrayStackLevel extends StackLevel {
        public ArrayStackLevel(long expectedSize) {
            super(new ArrayValue(), expectedSize);
        }
    }

    protected static class ObjectStackLevel extends StackLevel {
        public String currentKey;

        public ObjectStackLevel(Value.Configuration cfg, long expectedSize) {
            super(ObjectValue.newInstance(cfg), expectedSize);
        }
    }

    protected ArrayDeque<StackLevel> stack;
    protected Map<Integer, Value> idMap;

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
        if (cfg.serializeReferences) {
            idMap = new HashMap<>();
        }
        stack = new ArrayDeque<>() {
            @Override
            public void push(StackLevel stackLevel) {
                if (size() == cfg.maxDepth) {
                    throw newThrowable(MaxDepthExceededException.class,
                            "Objects is too deep, max allowed depth is %d", cfg.maxDepth);
                }
                super.push(stackLevel);
            }
        };
        stack.push(new ArrayStackLevel(-1));
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

    protected Value beginObject() {
        return beginObject(-1);
    }

    protected Value beginObject(long size) {
        ObjectStackLevel osl = new ObjectStackLevel(cfg, size);
        stack.push(osl);
        return osl.value;
    }

    protected void endObject() {
        ValueParser.StackLevel sl = stack.pop();
        if(!(sl instanceof ValueParser.ObjectStackLevel)) {
            error(ParseException::new, "Unexpected object terminator");
        }
        add2Last(sl.value);
    }

    protected Value beginArray() {
        return beginArray(-1);
    }

    protected Value beginArray(long size) {
        ArrayStackLevel ale = new ArrayStackLevel(size);
        stack.push(ale);
        return ale.value;
    }

    protected void endArray() {
        ValueParser.StackLevel sl = stack.pop();
        if(!(sl instanceof ValueParser.ArrayStackLevel)) {
            error(ParseException::new, "Unexpected array terminator");
        }
        add2Last(sl.value);
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

    protected void valueId(int id, Value value) {
        idMap.put(id, value);
    }

    protected void valueReference(int id) {
        Value referencedValue = idMap.get(id);
        if (referencedValue == null) {
            error(ParseException::new, "got invalid id '%d'", id);
        }
        add2Last(referencedValue);
    }

    protected <T extends RuntimeException> T error(Function<String, T> constructor, String fmt, Object... args) {
        throw new NotImplementedException("Method not implemented");
    }
}
