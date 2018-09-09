package org.oggio88.worth.utils;

import lombok.SneakyThrows;

import java.util.concurrent.Callable;

public class WorthUtils {

    @SneakyThrows
    public static <T> T uncheckCall(final Callable<T> callable) {
        return callable.call();
    }

    public static <T> T dynamicCast(final Object o, final Class<T> cls) {
        if (cls.isInstance(o)) {
            return (T) o;
        } else {
            return null;
        }
    }

    public static boolean equalsNullSafe(Object o1, Object o2) {
        if (o1 == null) return o2 == null;
        else return o1.equals(o2);
    }
}
