package org.oggio88.worth.serialization.json;

import lombok.SneakyThrows;
import org.oggio88.worth.buffer.CircularBuffer;
import org.oggio88.worth.exception.IOException;
import org.oggio88.worth.exception.NotImplementedException;
import org.oggio88.worth.exception.ParseException;
import org.oggio88.worth.serialization.ValueParser;
import org.oggio88.worth.utils.WorthUtils;
import org.oggio88.worth.xface.Parser;
import org.oggio88.worth.xface.Value;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Function;

public class JSONParser extends ValueParser {

    private int currentLine = 1, currentColumn = 1;

    private static boolean isBlank(int c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static boolean isDecimal(int c) {
        return c >= '0' && c <= '9' || c == '+' || c == '-' || c == '.' || c == 'e';
    }

    private static int parseHex(CircularBuffer circularBuffer) {
        int result = 0;
        while (true) {
            int c = circularBuffer.next();
            if (c >= '0' && c <= '9') {
                result = result << 4;
                result += (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                result = result << 4;
                result += 10 + (c - 'a');
            } else if (c >= 'A' && c <= 'F') {
                result = result << 4;
                result += 10 + (c - 'A');
            } else {
                circularBuffer.prev();
                break;
            }
        }
        return result;
    }

    private final void parseNumber(CircularBuffer circularBuffer) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int b = circularBuffer.next();
            if (isDecimal(b)) {
                sb.appendCodePoint(b);
            } else {
                circularBuffer.prev();
                break;
            }
        }
        String text = sb.toString();
        if (text.indexOf('.') > 0) {
            floatValue(Double.valueOf(text));
        } else {
            integerValue(Long.valueOf(text));
        }
    }

    private final String readString(CircularBuffer circularBuffer) {
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        while (true) {
            int c = circularBuffer.next();
            if (c < 0) {
                circularBuffer.prev();
                break;
            } else if (escape) {
                switch (c) {
                    case '"':
                        sb.append('\"');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'u':
                        int codePoint = parseHex(circularBuffer);
                        sb.appendCodePoint(codePoint);
                        break;
                    default:
                        throw error(ParseException::new, "Unrecognized escape sequence '\\%c'", c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '\"') {
                break;
            } else {
                sb.appendCodePoint(c);
            }
        }
        return sb.toString();
    }

    private final void consumeExpected(CircularBuffer circularBuffer, String expected, String errorMessage) {
        for (int i = 0; i < expected.length(); i++) {
            int c = circularBuffer.next();
            if (c < 0) {
                throw error(IOException::new, "Unexpected end of stream");
            }
            if (c != expected.codePointAt(i)) throw error(ParseException::new, errorMessage);
        }
    }

    private <T extends RuntimeException> T error(Function<String, T> constructor, String fmt, Object... args) {
        return constructor.apply(
                String.format("Error at line %d column %d: %s",
                        currentLine, currentColumn, String.format(fmt, args)));
    }

    public static Parser newInstance() {
        return new JSONParser();
    }

    @Override
    public Value parse(InputStream stream) {
        return parse(new InputStreamReader(stream));
    }

    @Override
    @SneakyThrows
    public Value parse(Reader reader) {
        final CircularBuffer circularBuffer = new CircularBuffer(reader, 8) {

            @Override
            public int next() {
                int result = super.next();
                if (result == '\n') {
                    ++currentLine;
                    currentColumn = 1;
                } else {
                    ++currentColumn;
                }
                return result;
            }

            @Override
            public int prev() {
                int result = super.prev();
                if (result == '\n') {
                    --currentLine;
                } else {
                    --currentColumn;
                }
                return result;
            }
        };

        try {
            while (true) {
                int c = circularBuffer.next();
                if (c == -1) {
                    break;
                } else if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    continue;
                } else if (c == '{') {
                    beginObject();
                } else if (c == '}') {
                    endObject();
                } else if (c == '[') {
                    beginArray();
                } else if (c == ']') {
                    endArray();
                } else if (isDecimal(c)) {
                    circularBuffer.prev();
                    try {
                        parseNumber(circularBuffer);
                    } catch (NumberFormatException nfe) {

                    }
                } else if (c == '\"') {
                    String text = readString(circularBuffer);
                    ObjectStackLevel osl;
                    if ((osl = WorthUtils.dynamicCast(stack.lastElement(), ObjectStackLevel.class)) != null && osl.currentKey == null) {
                        objectKey(text);
                    } else {
                        stringValue(text);
                    }
                } else if (c == 't') {
                    consumeExpected(circularBuffer, "rue", "Unrecognized boolean value");
                    booleanValue(true);
                } else if (c == 'f') {
                    consumeExpected(circularBuffer, "alse", "Unrecognized boolean value");
                    booleanValue(false);
                } else if (c == 'n') {
                    consumeExpected(circularBuffer, "ull", "Unrecognized null value");
                    nullValue();
                }
            }
            if (stack.size() > 1) {
                char c;
                if (stack.lastElement() instanceof ArrayStackLevel) {
                    c = ']';
                } else if (stack.lastElement() instanceof ObjectStackLevel) {
                    c = '}';
                } else {
                    throw new NotImplementedException("This should never happen");
                }
                throw error(ParseException::new, "Missing '%c' token", c);
            }
            return WorthUtils.dynamicCast(stack.lastElement(), ArrayStackLevel.class).value.get(0);
        } catch (NumberFormatException e) {
            throw error(ParseException::new, e.getMessage());
        } finally {
            stack.clear();
        }
    }
}
