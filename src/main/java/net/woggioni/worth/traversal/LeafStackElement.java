package net.woggioni.worth.traversal;

import net.woggioni.worth.exception.NotImplementedException;
import net.woggioni.worth.xface.Value;

import static net.woggioni.worth.utils.WorthUtils.newThrowable;

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
