package com.personthecat.orestonevariants.util.interfaces;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}