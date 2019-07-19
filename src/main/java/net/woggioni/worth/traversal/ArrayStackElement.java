package net.woggioni.worth.traversal;

import lombok.Getter;
import net.woggioni.worth.value.ArrayValue;
import net.woggioni.worth.xface.Value;

import java.util.Iterator;

public class ArrayStackElement extends StackElement {

    @Getter
    private final ArrayValue value;

    private final Iterator<Value> it;

    @Getter
    private int currentIndex;

    private Value currentValue = null;

    ArrayStackElement(ArrayValue av) {
        this.value = av;
        it = av.iterator();
    }

    @Override
    Value current() {
        return currentValue;
    }

    @Override
    Value next() {
        currentValue = it.next();
        currentIndex++;
        return currentValue;
    }

    @Override
    boolean hasNext() {
        return it.hasNext();
    }
}
