package com.personthecat.orestonevariants.util.unsafe;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;

    static <T, R> Function<T, R> wrap(ThrowingFunction<T, R, Throwable> func) {
        return t -> {
            try {
                return func.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}