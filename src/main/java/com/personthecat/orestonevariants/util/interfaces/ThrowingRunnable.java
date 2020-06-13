package com.personthecat.orestonevariants.util.interfaces;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
    void run() throws E;
}