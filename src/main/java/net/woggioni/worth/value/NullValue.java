package net.woggioni.worth.value;

import lombok.EqualsAndHashCode;
import net.woggioni.worth.xface.Value;

@EqualsAndHashCode
public class NullValue implements Value {

    @Override
    public Type type() {
        return Type.NULL;
    }
}
