package net.woggioni.wson.traversal;

import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.xface.Value;

import java.util.Iterator;
import java.util.Map;

class ObjectStackElement<T> extends AbstractStackElement<T> {

    private final Iterator<Map.Entry<String, Value>> it;

    private int currentIndex;

    private String currentKey;

    private Value currentValue;

    public ObjectStackElement(ObjectValue ov) {
        super(ov);
        it = ov.iterator();
        currentIndex = -1;
    }

    public Value next() {
        Map.Entry<String, Value> result = it.next();
        currentKey = result.getKey();
        currentIndex++;
        currentValue = result.getValue();
        return currentValue;
    }

    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public String getCurrentKey() {
        return currentKey;
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }
}
