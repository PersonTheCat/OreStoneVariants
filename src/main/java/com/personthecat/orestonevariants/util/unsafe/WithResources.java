package com.personthecat.orestonevariants.util.unsafe;

import com.personthecat.orestonevariants.util.interfaces.*;

/**
 * A helper classed used for generating Results with two closeable resources.
 * Equivalent to using try-with-resources.
 *
 * @param <R1> The resource being consumed and closed by this handler.
 * @param <R2> The second resource being consumed and closed by this handler.
 * @param <E> The type of error to be caught by the handler.
 */
class WithResources<R1 extends AutoCloseable, R2 extends AutoCloseable, E extends Throwable> {
    private final ThrowingSupplier<R1, E> r1Getter;
    private final ThrowingSupplier<R2, E> r2Getter;

    WithResources(ThrowingSupplier<R1, E> r1Getter, ThrowingSupplier<R2, E> r2Getter) {
        this.r1Getter = r1Getter;
        this.r2Getter = r2Getter;
    }

    /**
     * Constructs a new Result, consuming the resources and yielding a new value.
     * Equivalent to calling Result#of after queueing resources.
     * This ultimately changes the type of error handled by the wrapper, but the
     * original error must be a subclass of the new error in order for it to work.
     */
    public <T, E2 extends Throwable> Result<T, E2> of(ThrowingBiFunction<R1, R2, T, E2> attempt) {
        return new Result<>(() -> getResult(attempt));
    }

    /** Constructs a new Result, consuming the resources without yielding a value. */
    public <E2 extends Throwable> Result<Void, E2> of(ThrowingBiConsumer<R1, R2, E2> attempt) {
        return of((r1, r2) -> {
            attempt.accept(r1, r2);
            return Void.INSTANCE;
        });
    }

    /** Variant of Result#getResult based on the standard try-with-resources functionality. */
    private <T, E2 extends Throwable> Value<T, E2> getResult(ThrowingBiFunction<R1, R2, T, E2> attempt) {
        try (R1 r1 = r1Getter.get(); R2 r2 = r2Getter.get()) {
            return Value.ok(attempt.apply(r1, r2));
        } catch (Throwable e) {
            return Value.err(Result.errorFound(e));
        }
    }
}