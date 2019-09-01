package com.personthecat.orestonevariants.util.unsafe;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
    void run() throws E;

    static Runnable wrap(ThrowingRunnable<Throwable> func) {
        return () -> {
            try {
                func.run();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }
}