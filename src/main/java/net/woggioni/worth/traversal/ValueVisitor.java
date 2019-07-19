package net.woggioni.worth.traversal;

import net.woggioni.worth.value.*;

public interface ValueVisitor {

    void visit(ObjectValue value, TraversalContext ctx);
    void visit(ArrayValue value, TraversalContext ctx);
    void visit(BooleanValue value, TraversalContext ctx);
    void visit(StringValue value, TraversalContext ctx);
    void visit(IntegerValue value, TraversalContext ctx);
    void visit(FloatValue value, TraversalContext ctx);
    void visit(NullValue value, TraversalContext ctx);
}