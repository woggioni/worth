package net.woggioni.wson.jakarta;

import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import lombok.RequiredArgsConstructor;

import java.io.Reader;
import java.math.BigDecimal;

@RequiredArgsConstructor
class WsonJsonParser implements JsonParser {
    private final Reader reader;

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Event next() {
        return null;
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public boolean isIntegralNumber() {
        return false;
    }

    @Override
    public int getInt() {
        return 0;
    }

    @Override
    public long getLong() {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal() {
        return null;
    }

    @Override
    public JsonLocation getLocation() {
        return null;
    }

    @Override
    public void close() {

    }
}
