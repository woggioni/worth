package net.woggioni.wson.traversal;

import net.woggioni.wson.exception.NotImplementedException;
import net.woggioni.wson.value.ArrayValue;
import net.woggioni.wson.xface.Value;

import java.util.Iterator;

import static net.woggioni.jwo.JWO.newThrowable;

class ArrayStackElement<T> extends AbstractStackElement<T> {

    private final Iterator<Value> it;

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
