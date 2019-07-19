package net.woggioni.worth.traversal;

import lombok.Getter;
import net.woggioni.worth.value.ObjectValue;
import net.woggioni.worth.xface.Value;

import java.util.Iterator;
import java.util.Map;

public class ObjectStackElement extends StackElement {

    private final Iterator<Map.Entry<String, Value>> it;

    @Getter
    private final ObjectValue value;

    @Getter
    private int currentIndex;

    @Getter
    private String currentKey;

    private Value currentValue;

    public ObjectStackElement(ObjectValue ov) {
        value = ov;
        it = ov.iterator();
        currentIndex = -1;
    }

    @Override
    Value current() {
        return currentValue;
    }

    @Override
    Value next() {
        Map.Entry<String, Value> result = it.next();
        currentKey = result.getKey();
        currentIndex++;
        currentValue = result.getValue();
        return currentValue;
    }

    @Override
    boolean hasNext() {
        return it.hasNext();
    }
}
