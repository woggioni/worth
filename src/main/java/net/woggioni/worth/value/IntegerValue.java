package net.woggioni.worth.value;

import lombok.EqualsAndHashCode;
import net.woggioni.worth.xface.Value;

@EqualsAndHashCode
public class IntegerValue implements Value {

    private final long value;

    public IntegerValue(long value) {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.INTEGER;
    }

    @Override
    public long asInteger() {
        return value;
    }
}
