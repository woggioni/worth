package org.oggio88.worth.value;

import lombok.EqualsAndHashCode;
import org.oggio88.worth.xface.Value;

@EqualsAndHashCode
public class NullValue implements Value {

    @Override
    public Type type() {
        return Type.NULL;
    }
}
