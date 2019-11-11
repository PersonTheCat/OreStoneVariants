package com.personthecat.orestonevariants.util.interfaces;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
}