package net.woggioni.worth.traversal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.woggioni.worth.xface.Value;

@RequiredArgsConstructor
abstract class AbstractStackElement<T> implements StackElement<T> {

    @Getter
    @Setter
    private T context;

    private final Value value;

    boolean traverseChildren;

    @Override
    public Value getValue() {
        return value;
    }
}
