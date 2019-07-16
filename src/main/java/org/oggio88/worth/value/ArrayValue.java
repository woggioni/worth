package org.oggio88.worth.value;

import lombok.EqualsAndHashCode;
import org.oggio88.worth.xface.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
public class ArrayValue implements Value, Iterable<Value> {

    private final List<Value> value;

    public ArrayValue() {
        this.value = new ArrayList();
    }

    public ArrayValue(List<Value> value) {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.ARRAY;
    }

    @Override
    public void add(Value value) {
        this.value.add(value);
    }

    @Override
    public Value get(int index) {
        int sz = size();
        if(index < sz) {
            return value.get(Math.floorMod(index, sz));
        } else {
            return Value.Null;
        }
    }

    @Override
    public Value pop() {
        Value last = tail();
        value.remove(value.size() - 1);
        return last;
    }

    @Override
    public Value head() {
        return value.get(0);
    }

    @Override
    public Value tail() {
        return value.get(value.size() - 1);
    }

    @Override
    public List<Value> asArray() {
        return value;
    }


    @Override
    public Iterator<Value> iterator() {
        return value.iterator();
    }

    @Override
    public int size() {
        return value.size();
    }
}
