package net.woggioni.worth.serialization.json;

import lombok.SneakyThrows;
import net.woggioni.worth.buffer.LookAheadTextInputStream;
import net.woggioni.worth.exception.IOException;
import net.woggioni.worth.exception.NotImplementedException;
import net.woggioni.worth.exception.ParseException;
import net.woggioni.worth.serialization.ValueParser;
import net.woggioni.worth.utils.WorthUtils;
import net.woggioni.worth.xface.Parser;
import net.woggioni.worth.xface.Value;

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

    static int parseHex(LookAheadTextInputStream stream) {
        int result = 0;
        int c = stream.getCurrentByte();
        while (c != -1) {
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
            c = stream.read();
        }
        return result;
    }

    private final String parseNumber(LookAheadTextInputStream stream) {
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
        return sb.toString();
    }

    private int parseId(LookAheadTextInputStream stream) {
        StringBuilder sb = new StringBuilder();
        boolean digitsStarted = false;
        boolean digitsEnded = false;
        while (true) {
            int b = stream.getCurrentByte();
            if (b == '(') {
            } else if (Character.isWhitespace(b)) {
                if (digitsStarted) digitsEnded = true;
            } else if (b < 0 || b == ')') {
                break;
            } else if (isDecimal(b)) {
                if (digitsEnded) {
                    error(ParseException::new, "error parsing id");
                } else {
                    digitsStarted = true;
                    sb.appendCodePoint(b);
                }
            }
            stream.read();
        }
        return Integer.parseInt(sb.toString());
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

    @Override
    protected <T extends RuntimeException> T error(Function<String, T> constructor, String fmt, Object... args) {
        return constructor.apply(String.format("Error at line %d column %d: %s",
                currentLine, currentColumn, String.format(fmt, args)));
    }

    public static Parser newInstance() {
        return new JSONParser();
    }

    public static Parser newInstance(Value.Configuration cfg) {
        return new JSONParser(cfg);
    }

    public JSONParser() {
        super(Value.configuration);
    }

    public JSONParser(Value.Configuration cfg) {
        super(cfg);
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
        stream.read();

        try {
            Integer currentId = null;
            while (true) {
                int c = stream.getCurrentByte();
                if (c == -1) {
                    break;
                } else if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                } else if (c == '(') {
                    currentId = parseId(stream);
                } else if (c == '{') {
                    Value newObject = beginObject();
                    if (currentId != null) valueId(currentId, newObject);
                    currentId = null;
                } else if (c == '}') {
                    endObject();
                } else if (c == '[') {
                    Value newArray = beginArray();
                    if (currentId != null) valueId(currentId, newArray);
                    currentId = null;
                } else if (c == ']') {
                    endArray();
                } else if (isDecimal(c)) {
                    try {
                        String text = parseNumber(stream);
                        if (text.indexOf('.') > 0) {
                            floatValue(Double.parseDouble(text));
                        } else {
                            integerValue(Long.parseLong(text));
                        }
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
                } else if (idMap != null && c == '$') {
                    stream.read();
                    String text = parseNumber(stream);
                    valueReference(Integer.parseInt(text));
                    continue;
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
            return ((ArrayStackLevel) stack.getFirst()).value.get(0);
        } catch (NumberFormatException e) {
            throw error(ParseException::new, e.getMessage());
        } finally {
            stack.clear();
        }
    }
}
