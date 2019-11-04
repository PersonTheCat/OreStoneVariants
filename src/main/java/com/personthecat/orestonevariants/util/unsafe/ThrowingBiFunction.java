package com.personthecat.orestonevariants.util.unsafe;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowingBiFunction<T1, T2, R, E extends Throwable> {
    R apply(T1 t1, T2 t2) throws E;

    static <T1, T2, R> BiFunction<T1, T2, R> wrap(ThrowingBiFunction<T1, T2, R, Throwable> func) {
        return (t1, t2) -> {
            try {
                return func.apply(t1, t2);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}