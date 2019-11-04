package com.personthecat.orestonevariants.util.unsafe;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowingBiConsumer<T1, T2, E extends Throwable> {
    void accept(T1 t1, T2 t2) throws E;

    static <T1, T2, E extends Throwable> BiConsumer<T1, T2> wrap(ThrowingBiConsumer<T1, T2, E> func) {
        return (t1, t2) -> {
            try {
                func.accept(t1, t2);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}