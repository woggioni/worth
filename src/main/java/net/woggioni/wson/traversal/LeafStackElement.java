package net.woggioni.wson.traversal;

import net.woggioni.wson.exception.NotImplementedException;
import net.woggioni.wson.xface.Value;

import static net.woggioni.jwo.JWO.newThrowable;

class LeafStackElement<T> extends AbstractStackElement<T> {
    public LeafStackElement(Value value) {
        super(value);
    }

    @Override
    public String getCurrentKey() {
        throw newThrowable(NotImplementedException.class,
                "currentKey not supported for value of type '%s'", getValue().type());
    }

    @Override
    public int getCurrentIndex() {
        throw newThrowable(NotImplementedException.class,
                "currentIndex not supported for value of type '%s'", getValue().type());
    }
}
