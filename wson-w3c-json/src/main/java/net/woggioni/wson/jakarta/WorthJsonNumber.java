package net.woggioni.wson.jakarta;

import jakarta.json.JsonNumber;
import net.woggioni.wson.value.FloatValue;
import net.woggioni.wson.value.IntegerValue;
import net.woggioni.wson.xface.Value;

import java.math.BigDecimal;
import java.math.BigInteger;


public class WorthJsonNumber implements JsonNumber {
    private final Value value;

    public WorthJsonNumber(IntegerValue value) {
        this.value = value;
    }

    public WorthJsonNumber(FloatValue value) {
        this.value = value;
    }

    @Override
    public boolean isIntegral() {
        return value.type() == Value.Type.INTEGER;
    }

    @Override
    public int intValue() {
        return (int) value.asInteger();
    }

    @Override
    public int intValueExact() {
        if(isIntegral()) return intValue();
        else {
            double res = value.asFloat();
            double res2 = Math.floor(res);
            if(res != res2) throw new ArithmeticException();
            else return (int) res;
        }
    }

    @Override
    public long longValue() {
        return value.asInteger();
    }

    @Override
    public long longValueExact() {
        if(isIntegral()) return intValue();
        else {
            double res = value.asFloat();
            double res2 = Math.floor(res);
            if(res != res2) throw new ArithmeticException();
            else return (long) res;
        }
    }

    @Override
    public BigInteger bigIntegerValue() {
        return bigDecimalValue().toBigInteger();
    }

    @Override
    public BigInteger bigIntegerValueExact() {
        if(isIntegral()) return bigDecimalValue().toBigInteger();
        else {
            double res = value.asFloat();
            double res2 = Math.floor(res);
            if(res != res2) throw new ArithmeticException();
            else return BigInteger.valueOf((long) res);
        }
    }

    @Override
    public double doubleValue() {
        return value.asFloat();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        if(isIntegral()) return BigDecimal.valueOf(value.asInteger());
        else return BigDecimal.valueOf(value.asFloat());
    }

    @Override
    public ValueType getValueType() {
        return ValueType.NUMBER;
    }
}
