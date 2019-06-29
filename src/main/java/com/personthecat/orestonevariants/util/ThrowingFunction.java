package com.personthecat.orestonevariants.util;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;

    static <I, O> Function<I, O> wrap(ThrowingFunction<I, O, Throwable> func) {
        return v -> {
            try {
                return func.apply(v);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }
}