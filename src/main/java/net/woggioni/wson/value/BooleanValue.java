package net.woggioni.wson.value;

import lombok.EqualsAndHashCode;
import net.woggioni.wson.xface.Value;

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
