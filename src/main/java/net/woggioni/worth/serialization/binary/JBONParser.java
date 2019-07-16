package net.woggioni.worth.serialization.binary;

import lombok.SneakyThrows;
import net.woggioni.worth.buffer.LookAheadInputStream;
import net.woggioni.worth.exception.ParseException;
import net.woggioni.worth.serialization.ValueParser;
import net.woggioni.worth.utils.Leb128;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;

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
                int c = stream.read();
                if(c == -1) {
                    break;
                }
                if(c == BinaryMarker.Null.value) {
                    nullValue();
                } else if(c == BinaryMarker.True.value) {
                    booleanValue(true);
                } else if(c == BinaryMarker.False.value) {
                    booleanValue(false);
                } else if(c == BinaryMarker.Int.value) {
                    integerValue(decoder.decode());
                } else if(c == BinaryMarker.Float.value) {
                    floatValue(decoder.decodeDouble());
                } else if(c == BinaryMarker.EmptyString.value) {
                    stringValue("");
                } else if(c > BinaryMarker.EmptyString.value &&
                    c < BinaryMarker.LargeString.value) {
                    byte[] buffer = new byte[c - BinaryMarker.EmptyString.value];
                    stream.read(buffer);
                    String text = new String(buffer);
                    stringValue(text);
                } else if(c == BinaryMarker.LargeString.value) {
                    byte[] buffer = new byte[(int) decoder.decode()];
                    stream.read(buffer);
                    String text = new String(buffer);
                    stringValue(text);
                } else if(c == BinaryMarker.EmptyArray.value) {
                    beginArray(0);
                } else if(c > BinaryMarker.EmptyArray.value && c < BinaryMarker.LargeArray.value) {
                    beginArray(c - BinaryMarker.EmptyArray.value);
                } else if(c == BinaryMarker.LargeArray.value) {
                    long size = decoder.decode();
                    beginArray(size);
                } else if(c == BinaryMarker.EmptyObject.value) {
                    beginObject(0);
                } else if(c > BinaryMarker.EmptyObject.value && c < BinaryMarker.LargeObject.value) {
                    beginObject(c - BinaryMarker.EmptyObject.value);
                } else if(c == BinaryMarker.LargeObject.value) {
                    long size = decoder.decode();
                    beginObject(size);
                } else {
                    throw new ParseException(String.format("Illegal byte at position %d: 0x%02x", cursor, c));
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