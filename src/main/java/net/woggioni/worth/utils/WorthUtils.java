package net.woggioni.worth.utils;

import lombok.SneakyThrows;
import net.woggioni.worth.xface.Value;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WorthUtils {

    public static <T> T dynamicCast(final Object o, final Class<T> cls) {
        if (cls.isInstance(o)) {
            return (T) o;
        } else {
            return null;
        }
    }

    public static <T> Stream<T> iterable2stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static void writeObject2File(String fileName, Object o) {
        writeObject2File(Paths.get(fileName), o);
    }

    @SneakyThrows
    public static void writeObject2File(Path file, Object o) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file.toString()))) {
            writer.write(o.toString());
        }
    }

    @SneakyThrows
    public static void writeBytes2File(Path file, byte[] bytes) {
        try (OutputStream os = new FileOutputStream(file.toString())) {
            os.write(bytes);
        }
    }

    @SneakyThrows
    public static String readFile2String(Path file) {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file.toString())))) {
            char[] buffer = new char[1024];
            while (true) {
                int read = reader.read(buffer);
                builder.append(buffer, 0, read);
                if (read < buffer.length) break;
            }
        }
        return builder.toString();
    }

    @SneakyThrows
    public static String readResource2String(String classpath) {
        StringBuilder sb = new StringBuilder();
        try (Reader reader = new InputStreamReader(WorthUtils.class.getResourceAsStream(classpath))) {
            char[] buffer = new char[1024];
            while (true) {
                int read = reader.read(buffer);
                sb.append(buffer, 0, read);
                if (read < buffer.length) break;
            }
        }
        return sb.toString();
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(Class<T> cls, String format, Object... args) {
        Constructor<T> constructor = cls.getConstructor(String.class);
        return constructor.newInstance(String.format(format, args));
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(Class<T> cls, Throwable throwable, String format, Object... args) {
        Constructor<T> constructor = cls.getConstructor(String.class, Throwable.class);
        return constructor.newInstance(String.format(format, args), throwable);
    }

    @SneakyThrows
    public static <T extends Throwable> void raise(Class<T> cls, Throwable throwable, String format, Object... args) {
        throw newThrowable(cls, throwable, format, args);
    }

    @SneakyThrows
    public static <T extends Throwable> void raise(Class<T> cls, String format, Object... args) {
        throw newThrowable(cls, format, args);
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

    public static <T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }

    public static <V, T> Stream<V> flatMap(Stream<T> stream,
                                           Function<? super T, Optional<? extends V>> mappingFunction) {
        return stream.map(mappingFunction).filter(Optional::isPresent).map(Optional::get);
    }

    public static <T> Stream<T> optional2Stream(Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    public static void setSystemPropertyIfNotDefined(String key, String value) {
        if(System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
