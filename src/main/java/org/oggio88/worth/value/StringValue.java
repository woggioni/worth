package org.oggio88.worth.value;

import lombok.EqualsAndHashCode;
import org.oggio88.worth.xface.Value;

@EqualsAndHashCode
public class StringValue implements Value {

    private final String value;

    public StringValue(String value)
    {
        this.value = value;
    }

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public String asString() {
        return value;
    }
}
