package net.woggioni.wson.utils;

import lombok.SneakyThrows;
import net.woggioni.wson.xface.Value;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class WsonUtils {

    public static Value getOrNull(Value root, String... keys) {
        Value result = root;
        for (String key : keys) {
            if(result.type() != Value.Type.OBJECT) {
                result = null;
                break;
            }
            result = result.get(key);
        }
        return result;
    }

    public static Optional<Value> optGet(Value root, String... keys) {
        return Optional.ofNullable(getOrNull(root, keys));
    }

    @SneakyThrows
    public static <T> T getOrThrow(Value root, Function<Value, T> success, Supplier<Throwable> error, String... keys) {
        return optGet(root, keys).map(success).orElseThrow(error);
    }
}
