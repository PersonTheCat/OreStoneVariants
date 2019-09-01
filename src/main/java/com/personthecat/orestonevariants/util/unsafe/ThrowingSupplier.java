package com.personthecat.orestonevariants.util.unsafe;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;

    static <T> Supplier<T> wrap(ThrowingSupplier<T, Throwable> func) {
        return () -> {
            try {
                return func.get();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }
}