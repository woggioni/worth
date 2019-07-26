package net.woggioni.worth.serialization;

import lombok.RequiredArgsConstructor;
import net.woggioni.worth.exception.NotImplementedException;
import net.woggioni.worth.traversal.TraversalContext;
import net.woggioni.worth.traversal.ValueIdentity;
import net.woggioni.worth.traversal.ValueVisitor;
import net.woggioni.worth.traversal.ValueWalker;
import net.woggioni.worth.value.*;
import net.woggioni.worth.xface.Dumper;
import net.woggioni.worth.xface.Value;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ValueDumper implements Dumper {

    protected final Value.Configuration cfg;

    @RequiredArgsConstructor
    protected static class StackLevel {
        public int index = 0;
        public final Value value;
    }

    protected Map<ValueIdentity, Integer> getIdMap(Value root) {
        Map<ValueIdentity, Integer> occurrencies = new HashMap<>();
        ValueVisitor visitor = new ValueVisitor(){

            private void visit(Value v) {
                ValueIdentity identity = new ValueIdentity(v);
                Integer i = occurrencies.getOrDefault(identity, 0);
                occurrencies.put(identity, ++i);
            }

            @Override
            public boolean filter(Value value, TraversalContext ctx) {
                if(value.type() == Value.Type.ARRAY || value.type() == Value.Type.OBJECT) {
                    ValueIdentity identity = new ValueIdentity(value);
                    Integer i = occurrencies.getOrDefault(identity, 0);
                    occurrencies.put(identity, ++i);
                    return i == 1;
                } else {
                    return true;
                }
            }

            @Override
            public void visit(ObjectValue value, TraversalContext ctx) {
                visit(value);
            }

            @Override
            public void visit(ArrayValue value, TraversalContext ctx) {
                visit(value);
            }
        };
        ValueWalker.walk(root, visitor);
        Map<ValueIdentity, Integer> result = new HashMap<>();
        int i = 0;
        for(ValueIdentity identity : occurrencies.keySet()) {
            result.put(identity, i++);
        }
        return result;
    }

    protected static class ArrayStackLevel extends StackLevel implements Iterator<Value> {
        private final Iterator<Value> iterator = ((ArrayValue) value).iterator();

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
        private final Iterator<Map.Entry<String, Value>> iterator = ((ObjectValue) value).iterator();

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

    protected ValueDumper(Value.Configuration cfg) {
        this.cfg = cfg;
        stack = new ArrayDeque<>();
    }

    protected ValueDumper() {
        this(Value.configuration);
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

    protected abstract void valueId(int id);

    protected abstract void valueReference(int id);
}
