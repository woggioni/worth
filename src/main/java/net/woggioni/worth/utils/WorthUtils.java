package net.woggioni.worth.utils;

import lombok.SneakyThrows;
import net.woggioni.worth.xface.Value;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class WorthUtils {

    public static Optional<Value> nested(Value root, String... keys) {
        Value result = root;
        for (String key : keys) {
            result = result.get(key);
            if (result == null) {
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    public static Value getOrNull(Value root, String... keys) {
        Value result = root;
        for (String key : keys) {
            result = result.get(key);
            if (result == null) {
                result = Value.Null;
                break;
            }
        }
        return result;
    }

    public static <T> T getOrNull(Value root, Function<Value, T> callback, String... keys) {
        Value result = getOrNull(root, keys);
        return result.type() == Value.Type.NULL ? null : callback.apply(result);
    }

    @SneakyThrows
    public static <T> T getOrThrow(Value root, Function<Value, T> success, Supplier<Throwable> error, String... keys) {
        Value result = getOrNull(root, keys);
        if (result.type() == Value.Type.NULL) {
            throw error.get();
        } else {
            return success.apply(result);
        }
    }
}
