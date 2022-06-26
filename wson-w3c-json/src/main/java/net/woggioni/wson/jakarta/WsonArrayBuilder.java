package net.woggioni.wson.jakarta;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import net.woggioni.wson.value.ArrayValue;

import java.math.BigDecimal;
import java.math.BigInteger;

public class WsonArrayBuilder implements JsonArrayBuilder {

    private final ArrayValue arrayValue = new ArrayValue();

    @Override
    public JsonArrayBuilder add(JsonValue value) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(String value) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(BigDecimal value) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(BigInteger value) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(int value) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(long value) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(double value) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(boolean value) {
        return null;
    }

    @Override
    public JsonArrayBuilder addNull() {
        return null;
    }

    @Override
    public JsonArrayBuilder add(JsonObjectBuilder builder) {
        return null;
    }

    @Override
    public JsonArrayBuilder add(JsonArrayBuilder builder) {
        return null;
    }

    @Override
    public JsonArray build() {
        return null;
    }
}
