package net.woggioni.worth.utils;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Tuple2<T, U> {
    public final T _1;
    public final U _2;
}
