package org.oggio88.worth.value;

import lombok.EqualsAndHashCode;
import org.oggio88.worth.xface.Value;

@EqualsAndHashCode
public class BooleanValue implements Value {

    private final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.BOOLEAN;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }
}
