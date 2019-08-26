package net.woggioni.worth.traversal;

import lombok.RequiredArgsConstructor;
import net.woggioni.worth.xface.Value;

import java.util.Objects;

@RequiredArgsConstructor
public class ValueIdentity {

    private final Value value;

    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) {
            return false;
        } else if(!(other instanceof ValueIdentity)) {
            return false;
        } else {
            return value == ((ValueIdentity) other).value;
        }
    }

    public static boolean equals(Value v1, Value v2) {
        return Objects.equals(new ValueIdentity(v1), new ValueIdentity(v2));
    }
}
