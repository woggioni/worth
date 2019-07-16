package org.oggio88.worth.value;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.oggio88.worth.xface.Value;

import java.util.*;

public interface ObjectValue extends Value, Iterable<Map.Entry<String, Value>> {

    boolean listBasedImplementation = Boolean.valueOf(
            System.getProperty(ObjectValue.class.getName() + ".listBasedImplementation", "false"));
    boolean preserveKeyOrder = Boolean.valueOf(
            System.getProperty(ObjectValue.class.getName() + ".preserveKeyOrder", "false"));

    static ObjectValue newInstance() {
        if (listBasedImplementation) {
            return new ListObjectValue();
        } else {
            return new MapObjectValue();
        }
    }

    @Override
    default Type type() {
        return Type.OBJECT;
    }
}

final class ObjectEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public ObjectEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}

@EqualsAndHashCode
class MapObjectValue implements ObjectValue {

    private final Map<String, Value> value;

    public MapObjectValue() {
        this.value = ObjectValue.preserveKeyOrder ? new LinkedHashMap<>() : new HashMap<>();
    }

    public MapObjectValue(Map<String, Value> value) {
        this.value = value;
    }

    @Override
    public Map<String, Value> asObject() {
        return Collections.unmodifiableMap(value);
    }

    @Override
    public Value get(String key) {
        return value.getOrDefault(key, Value.Null);
    }

    @Override
    public Value getOrDefault(String key, Value defaultValue) {
        return value.getOrDefault(key, defaultValue);
    }

    @Override
    public Value getOrPut(String key, Value value2Put) {
        if (value.containsKey(key))
            return value.get(key);
        else {
            put(key, value2Put);
            return value2Put;
        }
    }

    @Override
    public void put(String key, Value value2Put) {
        this.value.put(key, value2Put);
    }


    @Override
    public boolean has(String key) {
        return value.containsKey(key);
    }

    @Override
    public Iterator<Map.Entry<String, Value>> iterator() {
        return value.entrySet().iterator();
    }

    @Override
    public int size() {
        return value.size();
    }
}

@NoArgsConstructor
@EqualsAndHashCode
class ListObjectValue implements ObjectValue {

    private final List<Map.Entry<String, Value>> value = new ArrayList<>();

    public ListObjectValue(Map<String, Value> map) {
        this.value.addAll(map.entrySet());
    }

    @Override
    public Map<String, Value> asObject() {
        Map<String, Value> result = preserveKeyOrder ? new LinkedHashMap<>() : new HashMap<>();
        for (Map.Entry<String, Value> entry : value) {
            result.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Value get(String key) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return entry.getValue();
        }
        return Value.Null;
    }

    @Override
    public Value getOrDefault(String key, Value defaultValue) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return entry.getValue();
        }
        return defaultValue;
    }

    @Override
    public Value getOrPut(String key, Value value2Put) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return entry.getValue();
        }
        put(key, value2Put);
        return value2Put;
    }

    @Override
    public void put(String key, Value value2Put) {
        value.add(new ObjectEntry(key, value2Put));
    }


    @Override
    public boolean has(String key) {
        for (Map.Entry<String, Value> entry : value) {
            if(Objects.equals(entry.getKey(), key)) return true;
        }
        return false;
    }

    @Override
    public Iterator<Map.Entry<String, Value>> iterator() {
        return value.iterator();
    }

    @Override
    public int size() {
        return value.size();
    }
}
