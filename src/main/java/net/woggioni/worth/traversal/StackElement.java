package net.woggioni.worth.traversal;

import net.woggioni.worth.xface.Value;

public interface StackElement<T> {
    T getContext();
    void setContext(T ctx);
    Value getValue();
    String getCurrentKey();
    int getCurrentIndex();
}
