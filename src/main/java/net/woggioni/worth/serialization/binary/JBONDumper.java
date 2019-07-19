package net.woggioni.worth.serialization.binary;

import lombok.SneakyThrows;
import net.woggioni.worth.exception.NotImplementedException;
import net.woggioni.worth.serialization.ValueDumper;
import net.woggioni.worth.serialization.json.JSONDumper;
import net.woggioni.worth.utils.Leb128;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.value.ArrayValue;
import net.woggioni.worth.value.ObjectValue;
import net.woggioni.worth.xface.Dumper;
import net.woggioni.worth.xface.Value;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
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
                    ArrayValue arrayValue = WorthUtils.dynamicCast(v, ArrayValue.class);
                    stack.push(new ArrayStackLevel(arrayValue));
                    beginArray(arrayValue.size());
                    break;
                case OBJECT:
                    ObjectValue objectValue = WorthUtils.dynamicCast(v, ObjectValue.class);
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
            if ((arrayStackLevel = WorthUtils.dynamicCast(last, ArrayStackLevel.class)) != null) {
                if (arrayStackLevel.hasNext()) {
                    handle_value.accept(arrayStackLevel.next());
                } else {
                    endArray();
                    stack.pop();
                }
            } else if ((objectStackLevel = WorthUtils.dynamicCast(last, ObjectStackLevel.class)) != null) {
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
}
