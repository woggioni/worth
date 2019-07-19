package net.woggioni.worth.traversal;

import net.woggioni.worth.xface.Value;

import java.util.List;

public interface TraversalContext {
    Value getRoot();
    List<StackElement> getStack();
    String getPath();
}
