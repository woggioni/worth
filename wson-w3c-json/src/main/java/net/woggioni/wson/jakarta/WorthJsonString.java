package net.woggioni.wson.jakarta;

import jakarta.json.JsonString;
import lombok.RequiredArgsConstructor;
import net.woggioni.wson.value.StringValue;

@RequiredArgsConstructor
public class WorthJsonString implements JsonString {
    private final StringValue value;

    @Override
    public String getString() {
        return value.asString();
    }

    @Override
    public CharSequence getChars() {
        return value.asString();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }
}
