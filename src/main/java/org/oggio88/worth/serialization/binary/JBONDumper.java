package org.oggio88.worth.serialization.binary;

import lombok.SneakyThrows;
import org.oggio88.worth.exception.NotImplementedException;
import org.oggio88.worth.serialization.ValueDumper;
import org.oggio88.worth.utils.Leb128;
import org.oggio88.worth.value.ArrayValue;
import org.oggio88.worth.value.ObjectValue;
import org.oggio88.worth.xface.Dumper;
import org.oggio88.worth.xface.Value;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import java.util.function.Consumer;

import static org.oggio88.worth.utils.WorthUtils.dynamicCast;

public class JBONDumper extends ValueDumper {

    public static Dumper newInstance() {
        return new JBONDumper();
    }

    protected OutputStream os;

    @Override
    public void dump(Value value, Writer writer) {
        throw new NotImplementedException(null);
    }

    @Override
    @SneakyThrows
    public void dump(Value value, OutputStream outputStream) {
        this.os = outputStream;
        final Consumer<Value> handle_value = (v) -> {
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
                    ArrayValue arrayValue = dynamicCast(v, ArrayValue.class);
                    stack.push(new ArrayStackLevel(arrayValue));
                    beginArray(arrayValue.size());
                    break;
                case OBJECT:
                    ObjectValue objectValue = dynamicCast(v, ObjectValue.class);
                    stack.push(new ObjectStackLevel(objectValue));
                    beginObject(objectValue.size());
                    break;
            }
        };

        handle_value.accept(value);
        while (stack.size() > 0) {
            StackLevel last = stack.getFirst();
            ArrayStackLevel arrayStackLevel;
            ObjectStackLevel objectStackLevel;
            if ((arrayStackLevel = dynamicCast(last, ArrayStackLevel.class)) != null) {
                if (arrayStackLevel.hasNext()) {
                    handle_value.accept(arrayStackLevel.next());
                } else {
                    endArray();
                    stack.pop();
                }
            } else if ((objectStackLevel = dynamicCast(last, ObjectStackLevel.class)) != null) {
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
        } else {
            os.write(BinaryMarker.LargeObject.value);
            Leb128.encode(os, size);
        }
    }

    @Override
    @SneakyThrows
    protected void endObject() {
    }

    @Override
    @SneakyThrows
    protected void beginArray(int size) {
        if(size == 0) {
            os.write(BinaryMarker.EmptyArray.value);
        } else {
            os.write(BinaryMarker.LargeArray.value);
            Leb128.encode(os, size);
        }
    }

    @Override
    @SneakyThrows
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
            os.write(BinaryMarker.LargeString.value);
            byte[] bytes = value.getBytes();
            Leb128.encode(os, bytes.length);
            os.write(value.getBytes());
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
}
