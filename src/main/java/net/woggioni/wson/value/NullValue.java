package net.woggioni.wson.value;

import lombok.EqualsAndHashCode;
import net.woggioni.wson.xface.Value;

@EqualsAndHashCode
public class NullValue implements Value {

    @Override
    public Type type() {
        return Type.NULL;
    }
}
