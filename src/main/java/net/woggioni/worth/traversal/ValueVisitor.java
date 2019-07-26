package net.woggioni.worth.traversal;

import net.woggioni.worth.value.*;
import net.woggioni.worth.xface.Value;

public interface ValueVisitor {

    default void visit(ObjectValue value, TraversalContext ctx) {}
    default void visit(ArrayValue value, TraversalContext ctx) {}
    default void visit(BooleanValue value, TraversalContext ctx) {}
    default void visit(StringValue value, TraversalContext ctx) {}
    default void visit(IntegerValue value, TraversalContext ctx) {}
    default void visit(FloatValue value, TraversalContext ctx) {}
    default void visit(NullValue value, TraversalContext ctx) {}
    default boolean filter(Value value, TraversalContext ctx) { return true; }
}