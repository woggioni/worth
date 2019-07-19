package net.woggioni.worth.traversal;

import net.woggioni.worth.value.*;
import net.woggioni.worth.xface.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.woggioni.worth.utils.WorthUtils.dynamicCast;

public class ValueWalker {

    private Value parent;

    public ValueWalker(Value root) {
        parent = root;
    }

    public ValueWalker get(String key) {
        if(parent.type() == Value.Type.OBJECT) {
            parent = parent.get(key);
        } else {
            parent = Value.Null;
        }
        return this;
    }

    public ValueWalker get(int index) {
        if(parent.type() == Value.Type.ARRAY) {
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
        if(isPresent()) {
            return Optional.of(callback.apply(parent));
        } else {
            return Optional.empty();
        }
    }

    public <T> Optional<T> flatMap(Function<Value, Optional<T>> callback) {
        if(isPresent()) {
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

    public static void walk(Value root, ValueVisitor visitor) {
        List<StackElement> stack = new ArrayList<>();
        List<StackElement> immutableStack = Collections.unmodifiableList(stack);

        TraversalContext ctx = new TraversalContext() {

            @Override
            public Value getRoot() {
                return root;
            }

            @Override
            public List<StackElement> getStack() {
                return immutableStack;
            }

            @Override
            public String getPath() {
                StringBuilder sb = new StringBuilder();
                for(StackElement se : stack) {
                    ArrayStackElement ase;
                    ObjectStackElement ose;
                    if((ase = dynamicCast(se, ArrayStackElement.class)) != null) {
                        sb.append("[");
                        sb.append(ase.getCurrentIndex());
                        sb.append("]");
                    } else if((ose = dynamicCast(se, ObjectStackElement.class)) != null) {
                        sb.append("[\"");
                        sb.append(ose.getCurrentKey());
                        sb.append("\"]");
                    }
                }
                return sb.toString();
            }
        };

        ObjectValue ov;
        ArrayValue av = new ArrayValue();
        av.add(root);
        ArrayStackElement ase = new ArrayStackElement(av);
        stack.add(ase);
        while(true) {
            Value currentValue = stack.get(stack.size() - 1).next();
            if((av = dynamicCast(currentValue, ArrayValue.class)) != null) {
                ase = new ArrayStackElement(av);
                stack.add(ase);
            } else if((ov = dynamicCast(currentValue, ObjectValue.class)) != null) {
                ObjectStackElement ose = new ObjectStackElement(ov);
                stack.add(ose);
            } else {
                IntegerValue iv;
                BooleanValue bv;
                NullValue nv;
                FloatValue fv;
                StringValue sv;
                if((iv = dynamicCast(currentValue, IntegerValue.class)) != null) {
                    visitor.visit(iv, ctx);
                } else if((fv = dynamicCast(currentValue, FloatValue.class)) != null) {
                    visitor.visit(fv, ctx);
                } else if((bv = dynamicCast(currentValue, BooleanValue.class)) != null) {
                    visitor.visit(bv, ctx);
                } else if ((sv = dynamicCast(currentValue, StringValue.class)) != null) {
                    visitor.visit(sv, ctx);
                } else if ((nv = dynamicCast(currentValue, NullValue.class)) != null) {
                    visitor.visit(nv, ctx);
                }
            }
            while(true) {
                if(stack.size() == 1) return;
                int lastIndex = stack.size() - 1;
                StackElement se = stack.get(lastIndex);
                if(!se.hasNext()) {
                    ObjectStackElement ose;
                    if((ase = dynamicCast(se, ArrayStackElement.class)) != null) {
                        visitor.visit(ase.getValue(), ctx);
                    } else if((ose = dynamicCast(se, ObjectStackElement.class)) != null) {
                        visitor.visit(ose.getValue(), ctx);
                    }
                    stack.remove(lastIndex);
                } else {
                    break;
                }
            }
        }
    }
}
