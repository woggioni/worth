package org.oggio88.worth.serialization.json;

import lombok.SneakyThrows;
import org.oggio88.worth.buffer.LookAheadTextInputStream;
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

    private static int parseHex(LookAheadTextInputStream stream) {
        int result = 0;
        while (true) {
            int c = stream.getCurrentByte();
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
                break;
            }
            stream.read();
        }
        return result;
    }

    private final void parseNumber(LookAheadTextInputStream stream) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int b = stream.getCurrentByte();
            if (b < 0) {
                break;
            } else if (isDecimal(b)) {
                sb.appendCodePoint(b);
            } else {
                break;
            }
            stream.read();
        }
        String text = sb.toString();
        if (text.indexOf('.') > 0) {
            floatValue(Double.valueOf(text));
        } else {
            integerValue(Long.valueOf(text));
        }
    }

    private String readString(LookAheadTextInputStream stream) {
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        boolean start = false;
        while (true) {
            int c = stream.getCurrentByte();
            if (c < 0) {
                break;
            } else if (escape) {
                escape = false;
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
                        stream.read();
                        int codePoint = parseHex(stream);
                        sb.appendCodePoint(codePoint);
                        continue;
                    default:
                        throw error(ParseException::new, "Unrecognized escape sequence '\\%c'", c);
                }
            } else if (c == '\\') {
                escape = true;
            } else if (c == '\"') {
                if (start) break;
                else start = true;
            } else {
                sb.appendCodePoint(c);
            }
            stream.read();
        }
        return sb.toString();
    }

    private void consumeExpected(LookAheadTextInputStream stream, String expected, String errorMessage) {
        int i = 0;
        while (true) {
            int c = stream.getCurrentByte();
            if (c < 0) {
                throw error(IOException::new, "Unexpected end of stream");
            }
            if (c != expected.codePointAt(i)) throw error(ParseException::new, errorMessage);
            else if (++i >= expected.length()) {
                break;
            }
            stream.read();
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
        final LookAheadTextInputStream stream = new LookAheadTextInputStream(reader) {
            @Override
            public int read() {
                int result = super.read();
                if (result == '\n') {
                    ++currentLine;
                    currentColumn = 1;
                } else {
                    ++currentColumn;
                }
                return result;
            }
        };

        try {
            while (true) {
                int c = stream.getCurrentByte();
                if (c == -1) {
                    break;
                } else if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                } else if (c == '{') {
                    beginObject();
                } else if (c == '}') {
                    endObject();
                } else if (c == '[') {
                    beginArray();
                } else if (c == ']') {
                    endArray();
                } else if (isDecimal(c)) {
                    try {
                        parseNumber(stream);
                        continue;
                    } catch (NumberFormatException nfe) {
                        throw error(ParseException::new, nfe.getMessage());
                    }
                } else if (c == '\"') {
                    String text = readString(stream);
                    ObjectStackLevel osl;
                    if ((osl = WorthUtils.dynamicCast(stack.getFirst(), ObjectStackLevel.class)) != null && osl.currentKey == null) {
                        objectKey(text);
                    } else {
                        stringValue(text);
                    }
                } else if (c == 't') {
                    consumeExpected(stream, "true", "Unrecognized boolean value");
                    booleanValue(true);
                } else if (c == 'f') {
                    consumeExpected(stream, "false", "Unrecognized boolean value");
                    booleanValue(false);
                } else if (c == 'n') {
                    consumeExpected(stream, "null", "Unrecognized null value");
                    nullValue();
                }
                stream.read();
            }
            if (stack.size() > 1) {
                char c;
                if (stack.getFirst() instanceof ArrayStackLevel) {
                    c = ']';
                } else if (stack.getFirst() instanceof ObjectStackLevel) {
                    c = '}';
                } else {
                    throw new NotImplementedException("This should never happen");
                }
                throw error(ParseException::new, "Missing '%c' token", c);
            }
            return WorthUtils.dynamicCast(stack.getFirst(), ArrayStackLevel.class).value.get(0);
        } catch (NumberFormatException e) {
            throw error(ParseException::new, e.getMessage());
        } finally {
            stack.clear();
        }
    }
}
