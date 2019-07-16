package net.woggioni.worth.value;

import lombok.EqualsAndHashCode;
import net.woggioni.worth.xface.Value;

@EqualsAndHashCode
public class FloatValue implements Value {

    private final double value;

    public FloatValue(double value) {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.DOUBLE;
    }

    @Override
    public double asFloat() {
        return value;
    }
}
