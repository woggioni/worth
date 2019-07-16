package net.woggioni.worth.utils;

import net.woggioni.worth.xface.Value;

import java.util.Optional;
import java.util.function.Function;

public class ValueWalker {

    private Value parent;

    public ValueWalker(Value root) {
        parent = root;
    }

    public ValueWalker get(String key) {
        if(parent.type() == Value.Type.OBJECT) {
            parent = parent.get(key);
        } else {
            parent = Value.Null;
        }
        return this;
    }

    public ValueWalker get(int index) {
        if(parent.type() == Value.Type.ARRAY) {
            parent = parent.get(index);
        } else {
            parent = Value.Null;
        }
        return this;
    }

    public Value get() {
        return parent;
    }

    public <T> Optional<T> map(Function<Value, T> callback) {
        if(isPresent()) {
            return Optional.of(callback.apply(parent));
        } else {
            return Optional.empty();
        }
    }

    public <T> Optional<T> flatMap(Function<Value, Optional<T>> callback) {
        if(isPresent()) {
            return callback.apply(parent);
        } else {
            return Optional.empty();
        }
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return parent.type() == Value.Type.NULL;
    }
}
