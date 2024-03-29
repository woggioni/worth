package net.woggioni.wson.serialization.binary;

import lombok.SneakyThrows;
import net.woggioni.jwo.Leb128;
import net.woggioni.wson.exception.NotImplementedException;
import net.woggioni.wson.serialization.ValueDumper;
import net.woggioni.wson.traversal.ValueIdentity;
import net.woggioni.wson.value.ArrayValue;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Dumper;
import net.woggioni.wson.xface.Value;

import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class JBONDumper extends ValueDumper {

    public static Dumper newInstance() {
        return new JBONDumper();
    }

    public static Dumper newInstance(Value.Configuration cfg) {
        return new JBONDumper(cfg);
    }

    public JBONDumper() {
        super(Value.configuration);
    }

    public JBONDumper(Value.Configuration cfg) {
        super(cfg);
    }

    protected OutputStream os;

    @Override
    public void dump(Value value, Writer writer) {
        throw new NotImplementedException(null);
    }

    @Override
    @SneakyThrows
    public void dump(Value value, OutputStream outputStream) {
        Map<ValueIdentity, Integer> ids;
        Set<Integer> dumpedId;
        if(cfg.serializeReferences) {
            ids = getIdMap(value);
            dumpedId = new HashSet<>();
        } else {
            ids = null;
            dumpedId = null;
        }
        this.os = outputStream;
        final Consumer<Value> handle_value = (v) -> {
            Integer id;
            switch (v.type()) {
                case NULL:
                    nullValue();
                    break;
                case BOOLEAN:
                    booleanValue(v.asBoolean());
                    break;
                case INTEGER:
                    integerValue(v.asInteger());
                    break;
                case DOUBLE:
                    floatValue(v.asFloat());
                    break;
                case STRING:
                    stringValue(v.asString());
                    break;
                case ARRAY:
                    ArrayValue arrayValue = (ArrayValue) v;
                    if(ids != null && (id = ids.get(new ValueIdentity(arrayValue))) != null) {
                        if(dumpedId.add(id)) {
                            stack.push(new ArrayStackLevel(arrayValue));
                            valueId(id);
                            beginArray(arrayValue.size());
                        } else {
                            valueReference(id);
                        }
                    } else {
                        stack.push(new ArrayStackLevel(arrayValue));
                        beginArray(arrayValue.size());
                    }
                    break;
                case OBJECT:
                    ObjectValue objectValue = (ObjectValue) v;
                    if(ids != null && (id = ids.get(new ValueIdentity(objectValue))) != null) {
                        if(dumpedId.add(id)) {
                                stack.push(new ObjectStackLevel(v));
                                valueId(id);
                                beginObject(objectValue.size());
                            } else {
                                valueReference(id);
                            }
                    } else {
                        stack.push(new ObjectStackLevel(v));
                        beginObject(objectValue.size());
                    }
                    break;
            }
        };

        handle_value.accept(value);
        while (stack.size() > 0) {
            StackLevel last = stack.getFirst();
            ArrayStackLevel arrayStackLevel;
            ObjectStackLevel objectStackLevel;
            if (last instanceof ArrayStackLevel) {
                arrayStackLevel = (ArrayStackLevel) last;
                if (arrayStackLevel.hasNext()) {
                    handle_value.accept(arrayStackLevel.next());
                } else {
                    endArray();
                    stack.pop();
                }
            } else if (last instanceof ObjectStackLevel) {
                objectStackLevel = (ObjectStackLevel) last;
                if (objectStackLevel.hasNext()) {
                    Map.Entry<String, Value> entry = objectStackLevel.next();
                    objectKey(entry.getKey());
                    handle_value.accept(entry.getValue());
                } else {
                    endObject();
                    stack.pop();
                }
            }
        }
        this.os.flush();
        this.os = null;
    }

    @Override
    @SneakyThrows
    protected void beginObject(int size) {
        if(size == 0) {
            os.write(BinaryMarker.EmptyObject.value);
        } else if(size < BinaryMarker.LargeObject.value - BinaryMarker.EmptyObject.value) {
            os.write(BinaryMarker.EmptyObject.value + size);
        } else {
            os.write(BinaryMarker.LargeObject.value);
            Leb128.encode(os, size);
        }
    }

    @Override
    protected void endObject() {
    }

    @Override
    @SneakyThrows
    protected void beginArray(int size) {
        if(size == 0) {
            os.write(BinaryMarker.EmptyArray.value);
        } else if(size < BinaryMarker.LargeArray.value - BinaryMarker.EmptyArray.value) {
            os.write(BinaryMarker.EmptyArray.value + size);
        } else {
            os.write(BinaryMarker.LargeArray.value);
            Leb128.encode(os, size);
        }
    }

    @Override
    protected void endArray() {
    }

    @Override
    @SneakyThrows
    protected void objectKey(String key) {
        byte[] bytes = key.getBytes();
        Leb128.encode(os, bytes.length);
        os.write(key.getBytes());
    }

    @Override
    @SneakyThrows
    protected void stringValue(String value) {
        if(value.isEmpty()) {
            os.write(BinaryMarker.EmptyString.value);
        } else {
            byte[] bytes = value.getBytes();
            if(bytes.length < BinaryMarker.LargeString.value - BinaryMarker.EmptyString.value) {
                os.write(BinaryMarker.EmptyString.value + bytes.length);
                os.write(value.getBytes());
            } else {
                os.write(BinaryMarker.LargeString.value);
                Leb128.encode(os, bytes.length);
                os.write(value.getBytes());
            }
        }
    }

    @Override
    @SneakyThrows
    protected void integerValue(long value) {
        os.write(BinaryMarker.Int.value);
        Leb128.encode(os, value);
    }

    @Override
    @SneakyThrows
    protected void floatValue(double value) {
        os.write(BinaryMarker.Float.value);
        Leb128.encode(os, value);
    }

    @Override
    @SneakyThrows
    protected void booleanValue(boolean value) {
        if(value) {
            os.write(BinaryMarker.True.value);
        } else {
            os.write(BinaryMarker.False.value);
        }
    }

    @Override
    @SneakyThrows
    protected void nullValue() {
        os.write(BinaryMarker.Null.value);
    }

    @Override
    @SneakyThrows
    protected void valueReference(int id) {
        os.write(BinaryMarker.Reference.value);
        Leb128.encode(os, id);
    }

    @Override
    @SneakyThrows
    protected void valueId(int id) {
        os.write(BinaryMarker.Id.value);
        Leb128.encode(os, id);
    }
}
