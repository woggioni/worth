package org.oggio88.worth.serialization.binary;

import lombok.SneakyThrows;
import org.oggio88.worth.buffer.LookAheadInputStream;
import org.oggio88.worth.exception.ParseException;
import org.oggio88.worth.serialization.ValueParser;
import org.oggio88.worth.utils.Leb128;
import org.oggio88.worth.utils.WorthUtils;
import org.oggio88.worth.xface.Parser;
import org.oggio88.worth.xface.Value;

import java.io.InputStream;
import java.util.function.Function;

public class JBONParser extends ValueParser {

    private int cursor = 0;


    private <T extends RuntimeException> T error(Function<String, T> constructor, String fmt, Object... args) {
        return constructor.apply(
                String.format("Error at position %d: %s",
                        cursor, String.format(fmt, args)));
    }

    @Override
    @SneakyThrows
    public Value parse(InputStream is) {
        final LookAheadInputStream stream = new LookAheadInputStream(is) {
            @Override
            public int read() {
                int result = super.read();
                ++cursor;
                return result;
            }
        };
        stream.read();

        try {
            Leb128.Leb128Decoder decoder = new Leb128.Leb128Decoder(stream);
            ObjectStackLevel osl;
            while (true) {
                if ((osl = WorthUtils.dynamicCast(stack.getFirst(), ObjectStackLevel.class)) != null && osl.currentKey == null) {
                    int size = (int) decoder.decode();
                    byte[] buffer = new byte[size];
                    stream.read(buffer);
                    String text = new String(buffer);
                    objectKey(text);
                }
                byte b;
                {
                    int c = stream.read();
                    if(c == -1) {
                        break;
                    }
                    b = (byte) c;
                }
                if(b == BinaryMarker.Null.value) {
                    nullValue();
                } else if(b == BinaryMarker.True.value) {
                    booleanValue(true);
                } else if(b == BinaryMarker.False.value) {
                    booleanValue(false);
                } else if(b == BinaryMarker.Int.value) {
                    integerValue(decoder.decode());
                } else if(b == BinaryMarker.Float.value) {
                    floatValue(decoder.decodeDouble());
                } else if(b == BinaryMarker.EmptyString.value) {
                    stringValue("");
                } else if(b == BinaryMarker.LargeString.value) {
                    byte[] buffer = new byte[(int) decoder.decode()];
                    stream.read(buffer);
                    String text = new String(buffer);
                    stringValue(text);
                } else if(b == BinaryMarker.EmptyArray.value) {
                    beginArray(0);
                } else if(b == BinaryMarker.LargeArray.value) {
                    long size = decoder.decode();
                    beginArray(size);
                } else if(b == BinaryMarker.EmptyObject.value) {
                    beginObject(0);
                } else if(b == BinaryMarker.LargeObject.value) {
                    long size = decoder.decode();
                    beginObject(size);
                } else {
                    throw new ParseException(String.format("Illegal byte at position %d: 0x%02x", cursor, b));
                }
                while(stack.size() > 0) {
                    if ((osl = WorthUtils.dynamicCast(stack.getFirst(), ObjectStackLevel.class)) != null
                            && osl.value.size() == osl.expectedSize) {
                        endObject();
                        continue;
                    }
                    ArrayStackLevel asl;
                    if((asl = WorthUtils.dynamicCast(stack.getFirst(), ArrayStackLevel.class)) != null
                            && asl.value.size() == asl.expectedSize) {
                        endArray();
                        continue;
                    }
                    break;
                }
            }
            if (stack.size() > 1) {
                ValueParser.StackLevel last = stack.getFirst();
                String type;
                if(last instanceof ArrayStackLevel) {
                    type = "array";
                } else {
                    type = "object";
                }
                throw error(ParseException::new, "Unfinished %s", type);
            }
            return WorthUtils.dynamicCast(stack.getFirst(), ArrayStackLevel.class).value.get(0);
        } catch (NumberFormatException | NegativeArraySizeException e) {
            throw error(ParseException::new, e.getMessage());
        } finally {
            stack.clear();
        }
    }

    public static Parser newInstance() {
        return new JBONParser();
    }
}