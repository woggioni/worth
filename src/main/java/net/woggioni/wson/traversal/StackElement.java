package net.woggioni.wson.traversal;

import net.woggioni.wson.xface.Value;

public interface StackElement<T> {
    T getContext();
    void setContext(T ctx);
    Value getValue();
    String getCurrentKey();
    int getCurrentIndex();
}
