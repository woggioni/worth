package net.woggioni.worth.traversal;

import net.woggioni.worth.xface.Value;

abstract class StackElement {
    abstract Value current();
    abstract Value next();
    abstract boolean hasNext();
}
