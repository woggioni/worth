package org.oggio88.worth.xface;

import org.oggio88.worth.exception.TypeException;
import org.oggio88.worth.value.NullValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Value {

    Value Null = new NullValue();

    enum Type {
        OBJECT, ARRAY, STRING, DOUBLE, INTEGER, BOOLEAN, NULL
    }

    Type type();

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

    default List<Value> asArray() {
        throw new TypeException("Not an array");
    }

    default Map<String, Value> asObject() {
        throw new TypeException("Not an object");
    }

    default int size() {
        throw new TypeException("Neither an array nor an object");
    }

    default void add(Value value) {
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
}
