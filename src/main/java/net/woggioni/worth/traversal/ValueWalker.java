package net.woggioni.worth.traversal;

import net.woggioni.worth.value.ArrayValue;
import net.woggioni.worth.value.ObjectValue;
import net.woggioni.worth.xface.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.woggioni.worth.utils.WorthUtils.dynamicCast;
import static net.woggioni.worth.utils.WorthUtils.pop;
import static net.woggioni.worth.utils.WorthUtils.tail;

class TraversalContextImpl<T> implements TraversalContext<T> {
    private final List<StackElement<T>> immutableStack;

    public TraversalContextImpl(List<? extends StackElement<T>> stack) {
        immutableStack = Collections.unmodifiableList(stack);
    }

    @Override
    public List<StackElement<T>> getStack() {
        return immutableStack;
    }

    @Override
    public String getPath() {
        StringBuilder sb = new StringBuilder();
        for (StackElement se : immutableStack) {
            ArrayStackElement ase;
            ObjectStackElement ose;
            if ((ase = dynamicCast(se, ArrayStackElement.class)) != null) {
                sb.append("[");
                sb.append(ase.getCurrentIndex());
                sb.append("]");
            } else if ((ose = dynamicCast(se, ObjectStackElement.class)) != null) {
                sb.append("[\"");
                sb.append(ose.getCurrentKey());
                sb.append("\"]");
            }
        }
        return sb.toString();
    }
}

public class ValueWalker {

    private Value parent;

    public ValueWalker(Value root) {
        parent = root;
    }

    public ValueWalker get(String key) {
        if (parent.type() == Value.Type.OBJECT) {
            parent = parent.get(key);
        } else {
            parent = Value.Null;
        }
        return this;
    }

    public ValueWalker get(int index) {
        if (parent.type() == Value.Type.ARRAY) {
            parent = parent.get(index);
        } else {
            parent = Value.Null;
        }
        return this;
    }

    public Value get() {
        return parent;
    }

    public <T> Optional<T> map(Function<Value, T> callback) {
        if (isPresent()) {
            return Optional.of(callback.apply(parent));
        } else {
            return Optional.empty();
        }
    }

    public <T> Optional<T> flatMap(Function<Value, Optional<T>> callback) {
        if (isPresent()) {
            return callback.apply(parent);
        } else {
            return Optional.empty();
        }
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return parent.type() == Value.Type.NULL;
    }

    private static <T> AbstractStackElement<T> stackElementFromValue(Value value) {
        AbstractStackElement<T> result;
        switch (value.type()) {
            case ARRAY:
                result = new ArrayStackElement<>((ArrayValue) value);
                break;
            case OBJECT:
                result = new ObjectStackElement<>((ObjectValue) value);
                break;
            default:
                result = new LeafStackElement<>(value);
                break;
        }
        return result;
    }

    private static <T> AbstractStackElement<T> nextChildStackElement(StackElement<T> parent) {
        AbstractStackElement<T> result = null;
        if (parent instanceof ArrayStackElement) {
            ArrayStackElement ase = (ArrayStackElement) parent;
            if (ase.hasNext()) {
                result = stackElementFromValue(ase.next());
            }
        } else if (parent instanceof ObjectStackElement) {
            ObjectStackElement ose = (ObjectStackElement) parent;
            if (ose.hasNext()) {
                result = stackElementFromValue(ose.next());
            }
        }
        return result;
    }

    public static <T> void walk(Value root, ValueVisitor<T> visitor) {
        List<AbstractStackElement<T>> stack = new ArrayList<>();
        TraversalContext<T> ctx = new TraversalContextImpl<>(stack);
        AbstractStackElement<T> stackElement = stackElementFromValue(root);
        stack.add(stackElement);
        stackElement.traverseChildren = visitor.visitPre(ctx);
        while (!stack.isEmpty()) {
            AbstractStackElement<T> last = tail(stack);
            if(last.traverseChildren) {
                AbstractStackElement<T> childStackElement = nextChildStackElement(last);
                if(childStackElement != null) {
                    stack.add(childStackElement);
                    childStackElement.traverseChildren = visitor.visitPre(ctx);
                    continue;
                }
            }
            visitor.visitPost(ctx);
            pop(stack);
        }
    }
}
