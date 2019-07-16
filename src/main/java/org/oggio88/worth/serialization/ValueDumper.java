package org.oggio88.worth.serialization;

import lombok.RequiredArgsConstructor;
import org.oggio88.worth.exception.NotImplementedException;
import org.oggio88.worth.value.ArrayValue;
import org.oggio88.worth.value.ObjectValue;
import org.oggio88.worth.xface.Dumper;
import org.oggio88.worth.xface.Value;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;

public abstract class ValueDumper implements Dumper {

    @RequiredArgsConstructor
    protected static class StackLevel {
        public int index = 0;
        public final Value value;
    }

    protected static class ArrayStackLevel extends StackLevel implements Iterator<Value> {
        private final Iterator<Value> iterator = value.asArray().iterator();

        @Override
        public Value next() {
            ++index;
            return iterator.next();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        public ArrayStackLevel(ArrayValue value) {
            super(value);
        }
    }

    protected static class ObjectStackLevel extends StackLevel implements Iterator<Map.Entry<String, Value>> {
        private final Iterator<Map.Entry<String, Value>> iterator = value.asObject().entrySet().iterator();

        @Override
        public Map.Entry<String, Value> next() {
            ++index;
            return iterator.next();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        public ObjectStackLevel(ObjectValue value) {
            super(value);
        }
    }


    protected ArrayDeque<StackLevel> stack;

    protected ValueDumper() {
        stack = new ArrayDeque<>();
    }

    @Override
    public void dump(Value value, OutputStream stream) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public void dump(Value value, Writer writer) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public void dump(Value value, OutputStream stream, Charset encoding) {
        dump(value, new OutputStreamWriter(stream, encoding));
    }

    protected abstract void beginObject(int size);

    protected abstract void endObject();

    protected abstract void beginArray(int size);

    protected abstract void endArray();

    protected abstract void objectKey(String key);

    protected abstract void stringValue(String value);

    protected abstract void integerValue(long value);

    protected abstract void floatValue(double value);

    protected abstract void booleanValue(boolean value);

    protected abstract void nullValue();
}
