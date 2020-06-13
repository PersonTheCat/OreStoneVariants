package com.personthecat.orestonevariants.util.unsafe;

import com.personthecat.orestonevariants.util.interfaces.ThrowingSupplier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static com.personthecat.orestonevariants.util.CommonMethods.runEx;

/**
 * A set of procedures for handling specific error types.
 */
public class Protocol {
    private Set<Procedure<? extends Exception>> procedures = new HashSet<>();

    /** Defines a new procedure for handling a specific type of error. */
    public <E extends Exception> Protocol define(Class<E> type, Consumer<E> func) {
        procedures.add(new Procedure<>(type, func));
        return this;
    }

    public <T> Result<T, Exception> of(ThrowingSupplier<T, Exception> attempt) {
        return new Result.Handled<>(getResult(attempt));
    }

    private <T> Value<T, Exception> getResult(ThrowingSupplier<T, Exception> attempt) {
        try {
            return Value.ok(attempt.get());
        } catch (Exception e) {
            if (tryHandle(e)) {
                return Value.err(e);
            }
            throw runEx("No procedure defined for handling error:", e);
        }
    }

    /**
     * Attempts to handle the input exception, returning whether a procedure is
     * implemented.
     */
    private boolean tryHandle(Exception e) {
        return procedures.stream().anyMatch(proc -> {
            if (proc.clazz.isInstance(e)) {
                proc.func.accept(cast(e));
                return true;
            }
            return false;
        });
    }

    @SuppressWarnings("unchecked")
    private <E extends Exception> E cast(Exception e) {
        return (E) e;
    }

    /** A prcedure defined for handling a specific type of error. */
    private static class Procedure<T> {
        final Class<T> clazz;
        final Consumer<T> func;

        Procedure(Class<T> clazz, Consumer<T> func) {
            this.clazz = clazz;
            this.func = func;
        }
    }
}