package net.woggioni.wson.traversal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.woggioni.wson.xface.Value;

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
