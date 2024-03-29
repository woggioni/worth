package net.woggioni.wson.xface;

import lombok.Builder;
import net.woggioni.wson.exception.TypeException;
import net.woggioni.wson.value.NullValue;
import net.woggioni.wson.value.ObjectValue;

import java.util.Map;

public interface Value {

    Value Null = new NullValue();

    enum Type {
        OBJECT, ARRAY, STRING, DOUBLE, INTEGER, BOOLEAN, NULL
    }

    Type type();

    default boolean isNull() {
        return type() == Value.Null.type();
    }

    default boolean asBoolean() {
        throw new TypeException("Not a boolean");
    }

    default long asInteger() {
        throw new TypeException("Not an integer");
    }

    default double asFloat() {
        throw new TypeException("Not a float");
    }

    default String asString() {
        throw new TypeException("Not a String");
    }

    default Iterable<Value> asArray() {
        throw new TypeException("Not an array");
    }

    default Iterable<Map.Entry<String, Value>> asObject() {
        throw new TypeException("Not an object");
    }

    default int size() {
        throw new TypeException("Neither an array nor an object");
    }

    default void add(Value value) {
        throw new TypeException("Not an array");
    }

    default void set(int index, Value value) {
        throw new TypeException("Not an array");
    }

    default Value pop() {
        throw new TypeException("Not an array");
    }

    default Value head() {
        throw new TypeException("Not an array");
    }

    default Value tail() {
        throw new TypeException("Not an array");
    }

    default Value get(int index) {
        throw new TypeException("Not an array");
    }

    default void put(String key, Value value) {
        throw new TypeException("Not an object");
    }

    default Value get(String key) {
        throw new TypeException("Not an object");
    }

    default Value getOrDefault(String key, Value defaultValue) {
        throw new TypeException("Not an object");
    }

    default Value getOrPut(String key, Value value2Put) {
        throw new TypeException("Not an object");
    }

    default boolean has(String key) {
        throw new TypeException("Not an object");
    }

    @Builder
    class Configuration {

        @Builder.Default
        public final ObjectValue.Implementation objectValueImplementation = ObjectValue.Implementation.valueOf(
            System.getProperty(ObjectValue.class.getName() + ".implementation", "TreeMap"));

        @Builder.Default
        public final int maxDepth =
            Integer.parseInt(System.getProperty(Value.class.getName() + ".maxDepth", "1048576"));

        @Builder.Default
        public final boolean serializeReferences =
            Boolean.parseBoolean(System.getProperty(
            Value.class.getName() + ".serializeReferences", "false"));
    }

    Configuration configuration = Configuration.builder().build();
}
