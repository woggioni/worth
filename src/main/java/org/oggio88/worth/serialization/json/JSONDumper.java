package org.oggio88.worth.serialization.json;

import lombok.SneakyThrows;
import org.oggio88.worth.serialization.ValueDumper;
import org.oggio88.worth.value.ArrayValue;
import org.oggio88.worth.value.ObjectValue;
import org.oggio88.worth.xface.Dumper;
import org.oggio88.worth.xface.Value;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.function.Consumer;

import static org.oggio88.worth.utils.WorthUtils.dynamicCast;

public class JSONDumper extends ValueDumper {

    public static Dumper newInstance() {
        return new JSONDumper();
    }

    protected Writer writer;

    @Override
    public void dump(Value value, OutputStream stream) {
        dump(value, new OutputStreamWriter(stream));
    }

    @Override
    @SneakyThrows
    public void dump(Value value, Writer writer) {
        this.writer = writer;
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
                    stack.push(new ArrayStackLevel(dynamicCast(v, ArrayValue.class)));
                    beginArray();
                    break;
                case OBJECT:
                    stack.push(new ObjectStackLevel(dynamicCast(v, ObjectValue.class)));
                    beginObject();
                    break;
            }
        };

        handle_value.accept(value);
        while (stack.size() > 0) {
            StackLevel last = stack.lastElement();
            ArrayStackLevel arrayStackLevel;
            ObjectStackLevel objectStackLevel;
            if ((arrayStackLevel = dynamicCast(last, ArrayStackLevel.class)) != null) {
                if (arrayStackLevel.hasNext()) {
                    if (arrayStackLevel.index > 0) {
                        writer.write(",");
                    }
                    handle_value.accept(arrayStackLevel.next());
                } else {
                    endArray();
                    stack.pop();
                }
            } else if ((objectStackLevel = dynamicCast(last, ObjectStackLevel.class)) != null) {
                if (objectStackLevel.hasNext()) {
                    if (objectStackLevel.index > 0) {
                        writer.write(",");
                    }
                    Map.Entry<String, Value> entry = objectStackLevel.next();
                    objectKey(entry.getKey());
                    writer.write(":");
                    handle_value.accept(entry.getValue());
                } else {
                    endObject();
                    stack.pop();
                }
            }
        }
        this.writer.flush();
        this.writer = null;
    }

    @Override
    @SneakyThrows
    protected void beginObject() {
        this.writer.write("{");
    }

    @Override
    @SneakyThrows
    protected void endObject() {
        this.writer.write("}");
    }

    @Override
    @SneakyThrows
    protected void beginArray() {
        this.writer.write("[");
    }

    @Override
    @SneakyThrows
    protected void endArray() {
        this.writer.write("]");
    }

    @Override
    @SneakyThrows
    protected void objectKey(String key) {
        this.writer.write("\"" + key + "\"");
    }

    @Override
    @SneakyThrows
    protected void stringValue(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default: {
                    if (c < 128)
                        sb.append(c);
                    else {
                        sb.append("\\u").append(String.format("%04X", (int) c));
                    }
                }
            }
        }
        this.writer.write("\"" + sb.toString() + "\"");
    }

    @Override
    @SneakyThrows
    protected void integerValue(long value) {
        this.writer.write(Long.toString(value));
    }

    @Override
    @SneakyThrows
    protected void floatValue(double value) {
        this.writer.write(Double.toString(value));
    }

    @Override
    @SneakyThrows
    protected void booleanValue(boolean value) {
        this.writer.write(Boolean.toString(value));
    }

    @Override
    @SneakyThrows
    protected void nullValue() {
        this.writer.write("null");
    }
}
