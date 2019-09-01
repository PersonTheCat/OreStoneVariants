package com.personthecat.orestonevariants.util.unsafe;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;

    static <T, E extends Throwable> Consumer<T> wrap(ThrowingConsumer<T, E> func) {
        return t -> {
            try {
                func.accept(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}