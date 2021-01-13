package net.woggioni.wson.value;

import lombok.EqualsAndHashCode;
import net.woggioni.wson.xface.Value;

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
