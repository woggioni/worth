package net.woggioni.worth.serialization.json;

import lombok.SneakyThrows;
import net.woggioni.worth.serialization.ValueDumper;
import net.woggioni.worth.traversal.ValueIdentity;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.value.*;
import net.woggioni.worth.xface.Dumper;
import net.woggioni.worth.xface.Value;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JSONDumper extends ValueDumper {

    public static Dumper newInstance() {
        return new JSONDumper();
    }

    public static Dumper newInstance(Value.Configuration cfg) {
        return new JSONDumper(cfg);
    }

    public JSONDumper() {
        super(Value.configuration);
    }

    public JSONDumper(Value.Configuration cfg) {
        super(cfg);
    }

    private Writer writer;

    private String escapeString(String value){
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
        return sb.toString();
    }

    @Override
    public void dump(Value value, OutputStream stream) {
        dump(value, new OutputStreamWriter(stream));
    }

    @Override
    @SneakyThrows
    public void dump(Value value, Writer writer) {
        Map<ValueIdentity, Integer> ids;
        Set<Integer> dumpedId;
        if(cfg.serializeReferences) {
            ids = getIdMap(value);
            dumpedId = new HashSet<>();
        } else {
            ids = null;
            dumpedId = null;
        }
        this.writer = writer;
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
                    if(ids != null && (id = ids.get(new ValueIdentity(v))) != null) {
                        if(dumpedId.add(id)) {
                            stack.push(new ArrayStackLevel(v));
                            valueId(id);
                            beginArray(v.size());
                        } else {
                            valueReference(id);
                        }
                    } else {
                        stack.push(new ArrayStackLevel(v));
                        beginArray(v.size());
                    }
                    break;
                case OBJECT:
                    if(ids != null && (id = ids.get(new ValueIdentity(v))) != null) {
                        if(dumpedId.add(id)) {
                            stack.push(new ObjectStackLevel(v));
                            valueId(id);
                            beginObject(v.size());
                        } else {
                            valueReference(id);
                        }
                    } else {
                        stack.push(new ObjectStackLevel(v));
                        beginObject(v.size());
                    }
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
                    if (arrayStackLevel.index > 0) {
                        writer.write(",");
                    }
                    handle_value.accept(arrayStackLevel.next());
                } else {
                    endArray();
                    stack.pop();
                }
            } else if ((objectStackLevel = WorthUtils.dynamicCast(last, ObjectStackLevel.class)) != null) {
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
    protected void beginObject(int size) {
        this.writer.write("{");
    }

    @Override
    @SneakyThrows
    protected void endObject() {
        this.writer.write("}");
    }

    @Override
    @SneakyThrows
    protected void beginArray(int size) {
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
        this.writer.write("\"" + escapeString(key) + "\"");
    }

    @Override
    @SneakyThrows
    protected void stringValue(String value) {
        this.writer.write("\"" + escapeString(value) + "\"");
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

    @Override
    @SneakyThrows
    protected void valueId(int id) {
        this.writer.write("(" + id + ")");
    }

    @Override
    @SneakyThrows
    protected void valueReference(int id) {
        this.writer.write("$" + id);
    }
}
