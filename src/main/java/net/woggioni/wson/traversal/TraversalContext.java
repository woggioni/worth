package net.woggioni.wson.traversal;

import java.util.List;

public interface TraversalContext<T> {
    List<StackElement<T>> getStack();
    String getPath();
}
