package net.woggioni.worth.traversal;

import lombok.Getter;
import net.woggioni.worth.exception.NotImplementedException;
import net.woggioni.worth.value.ArrayValue;
import net.woggioni.worth.xface.Value;

import java.util.Iterator;

import static net.woggioni.worth.utils.WorthUtils.newThrowable;

class ArrayStackElement<T> extends AbstractStackElement<T> {

    private final Iterator<Value> it;

    @Getter
    private int currentIndex;

    private Value currentValue = null;

    ArrayStackElement(ArrayValue av) {
        super(av);
        it = av.iterator();
    }

    Value next() {
        currentValue = it.next();
        currentIndex++;
        return currentValue;
    }

    boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public String getCurrentKey() {
        throw newThrowable(NotImplementedException.class,
                "currentKey not supported for value of type '%s'", getValue().type());
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }
}
