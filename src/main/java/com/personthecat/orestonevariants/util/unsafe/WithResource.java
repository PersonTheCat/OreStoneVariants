package com.personthecat.orestonevariants.util.unsafe;

import com.personthecat.orestonevariants.util.interfaces.ThrowingConsumer;
import com.personthecat.orestonevariants.util.interfaces.ThrowingFunction;
import com.personthecat.orestonevariants.util.interfaces.ThrowingSupplier;

/**
 * A helper classed used for generating Results with a single closeable resource.
 * Equivalent to using try-with-resources.
 *
 * @param <R> The resource being consumed and closed by this handler.
 * @param <E> The type of error to be caught by the handler.
 */
public class WithResource<R extends AutoCloseable, E extends Throwable> {
    private final ThrowingSupplier<R, E> rGetter;

    WithResource(ThrowingSupplier<R, E> rGetter) {
        this.rGetter = rGetter;
    }

    /**
     * Constructs a new Result, consuming the resource and yielding a new value.
     * Equivalent to calling Result#of after queueing resources.
     * This ultimately changes the type of error handled by the wrapper, but the
     * original error must be a subclass of the new error in order for it to work.
     */
    public <T, E2 extends Throwable> Result<T, E2> of(ThrowingFunction<R, T, E2> attempt) {
        return new Result<>(() -> getResult(attempt));
    }

    /** Constructs a new Result, consuming the resource without yielding a value. */
    public <E2 extends Throwable> Result<Void, E2> of(ThrowingConsumer<R, E2> attempt) {
        return of(r -> {
            attempt.accept(r);
            return Void.INSTANCE;
        });
    }

    /** Constructs a new handler with an additional resource. */
    public <R2 extends AutoCloseable> WithResources<R, R2, E> with(ThrowingSupplier<R2, E> resource) {
        return new WithResources<>(rGetter, resource);
    }

    /** Constructs a new handler with an addition resource yielded from the first. */
    public <R2 extends AutoCloseable> WithResources<R, R2, E> with(ThrowingFunction<R, R2, E> getter) {
        return new WithResources<>(rGetter, () -> getter.apply(rGetter.get()));
    }

    /** Variant of Result#getResult based on the standard try-with-resources functionality. */
    private <T, E2 extends Throwable> Value<T, E2> getResult(ThrowingFunction<R, T, E2> attempt) {
        try (R r = rGetter.get()) {
            return Value.ok(attempt.apply(r));
        } catch (Throwable e) {
            return Value.err(Result.errorFound(e));
        }
    }
}