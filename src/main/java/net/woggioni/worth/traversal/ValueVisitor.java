package net.woggioni.worth.traversal;

public interface ValueVisitor<T> {
    default boolean visitPre(TraversalContext<T> ctx) { return true; }
    default void visitPost(TraversalContext<T> ctx) {}
}