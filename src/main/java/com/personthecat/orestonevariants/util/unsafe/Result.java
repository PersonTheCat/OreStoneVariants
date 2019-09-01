package com.personthecat.orestonevariants.util.unsafe;

import com.electronwill.nightconfig.core.NullObject;
import com.google.common.reflect.TypeToken;
import com.personthecat.orestonevariants.util.Lazy;
import net.jodah.typetools.TypeResolver;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/**
 *   A counterpart to java.util.Optional used for neatly handling errors.
 * Accepts an expression which is not processed until a procedure for
 * handling any potential errors is implemented.
 *
 *   The obvious downside to this object is that it does not provide any
 * safety benefits not already present in the language. In fact, it is
 * certainly less safe and less specific than vanilla error handling in Java,
 * as it requires a bit of runtime reflection and only accepts one error
 * parameter. It also requires a bit of memory overhead which likely has at
 * least a minor impact on performance; however, it does provide some
 * additional syntactic clarity when executing simple, error-prone procedures.
 * Typical try-catch blocks that often consume 3-5 lines of code can be
 * written in as little as 30-40 characters. Moreover, it offers a much more
 * functional approach to handling errors when dealing with uncertain
 * functionalities. This has the modest benefit of being easier to maintain,
 * though its considerations for readability are debatable, as it strays from
 * the standard conventions that most Java developers are used to.
 *
 * ¯\_(ツ)_/¯
 *
 * @param <T> The type of value to be returned.
 * @param <E> The type of error to be caught.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked"})
public class Result<T, E extends Throwable> {
    /** A static field to avoid unnecessary instantiation. */
    private static final Result<Void, ?> OK = new Result<>(new Lazy<>(Value::ok));

    /**
     * Accepts an error and ignores it, while still coercing it to its lowest type, thus
     * not allowing any unspecified error types to be ignored.
     * e.g. Result.of(...).handle(Result::IGNORE);
     */
    public static <E extends Throwable> void IGNORE(E e) {}

    /** A lazily-initialized result of the supplied operation. Not computed until handled. */
    private final Lazy<Value<T, E>> result;

    /** Constructs a new result with a raw, uncalculated value. */
    private Result(Lazy<Value<T, E>> attempt) {
        result = attempt;
    }

    /** Returns a generic result containing no value or error. */
    public static <E extends Throwable> Result<Void, E> ok() {
        return (Result<Void, E>) OK;
    }

    public static <T, E extends Throwable> Result<T, E> ok(T val) {
        return new Result<>(new Lazy<>(new Value<>(full(val), empty())));
    }

    /** Constructs a new Result from an operation which may yield a value. */
    public static <T, E extends Throwable> Result<T, E> of(ThrowingSupplier<T, E> attempt) {
        return new Result<>(new Lazy<>(() -> getResult(attempt)));
    }

    /** Constructs a new Result from an operation which will not yield a value. */
    public static <E extends Throwable> Result<Void, E> of(ThrowingRunnable<E> attempt) {
        return new Result<>(new Lazy<>(() -> getResult(attempt)));
    }

    public static <R extends AutoCloseable, E extends Throwable> Result<Void, E>
    with(ThrowingSupplier<R, E> resource, ThrowingConsumer<R, E> attempt) {
        return new Result<>(new Lazy<>(() -> getResult(resource, attempt)));
    }

    public static <R extends AutoCloseable, T, E extends Throwable> Result<T, E>
    with(ThrowingSupplier<R, E> resource, ThrowingFunction<R, T, E> attempt) {
        return new Result<>(new Lazy<>(() -> getResult(resource, attempt)));
    }

    public static <T, E extends Throwable> Result<T, E> manual(Optional<T> val, Optional<E> err) {
        return new Result<>(new Lazy<>(new Value<>(val, err)));
    }

    public static <E extends Throwable> Result<Void, E> manual(Optional<E> err) {
        return new Result<>(new Lazy<>(() -> getResult(() -> throwIfErr(err))));
    }

    /** Returns whether an error occurred in the process. */
    public boolean isErr() {
        return result.get().err.isPresent();
    }

    /** Returns whether a value was yielded or no error occurred. */
    public boolean isOk() {
        return !isErr();
    }

    /** Accepts an expression for handling the expected error, if present. */
    public Result<T, E> handle(Consumer<E> func) {
        try {
            result.get().err.ifPresent(func);
        } catch (ClassCastException e) {
            throw wrongErrorFound(result.get().err.get());
        }
        return this;
    }

    /** Accepts an expression for handling the expected value, if present. */
    public Result<T, E> ifPresent(Consumer<T> func) {
        result.get().result.ifPresent(func);
        return this;
    }

    /** Accepts an expression for what to do in the event of no error occurring. */
    public Result<T, E> ifPresent(Runnable func) {
        result.get().result.ifPresent((result -> func.run()));
        return this;
    }

    /** Replaces the underlying value with the result of func.apply. */
    public <M> Result<M, E> map(Function<T, M> func) {
        final Optional<M> mapped = result.get().result.map(func);
        final Optional<E> err = result.get().err;
        return new Result<>(new Lazy<>(new Value<>(mapped, err)));
    }

    /** Returns the value or throws the exception, whichever possible. */
    public T throwIfErr() throws E {
        throwIfErr(result.get().err);
        return result.get().result.orElseThrow(() -> runEx("No value or error present in result."));
    }

    /** Throws the exception, if present, and returns result.orElse(val). */
    public T throwOrElse(T val) throws E {
        throwIfErr(result.get().err);
        return orElse(val);
    }

    /** Throws an optional error. */
    private static <E extends Throwable> void throwIfErr(Optional<E> err) throws E {
        if (err.isPresent()) {
            throw err.get();
        }
    }

    /** Yields the underlying value, throwing an exception an error occurs. */
    public T expect(String message) {
        handle(err -> { throw new RuntimeException(message, err); });
        return result.get().result.orElseThrow(() -> runEx(message));
    }

    /** Variant of Result#expect which doesn't assert that a result is present. */
    public T expectOrElse(String message, T val) {
        handle(err -> { throw new RuntimeException(message, err); });
        return orElse(val);
    }

    /** Yields the underlying value, if present and an error has been handled. */
    public Optional<T> get() {
        return result.getIfComputed().flatMap(value -> value.result);
    }

    /** Yields the underlying value while handling any potential errors. */
    public Optional<T> get(Consumer<E> handler) {
        handle(handler);
        return get();
    }

    /** Yields the underlying value or else the input. */
    public T orElse(T val) {
        return result.get().result.orElse(val);
    }

    /** Runs the underlying process and converts it into a Result.Value. */
    private static <T, E extends Throwable> Value<T, E> getResult(ThrowingSupplier<T, E> attempt) {
        Optional<T> result = empty();
        Optional<E> err = empty();
        try {
            result = nullable(attempt.get());
        } catch (Throwable e) {
            err = full(errorFound(e));
        }
        return new Value<>(result, err);
    }

    private static <R extends AutoCloseable, T, E extends Throwable> Value<T, E>
    getResult(ThrowingSupplier<R, E> resource, ThrowingFunction<R, T, E> attempt) {
        Optional<T> result = empty();
        Optional<E> err = empty();
        try (R r = resource.get()) {
            result = nullable(attempt.apply(r));
        } catch (Throwable e) {
            err = full(errorFound(e));
        }
        return new Value<>(result, err);
    }

    /** Variant of Result#getResult which never yields a value. */
    private static <E extends Throwable> Value<Void, E> getResult(ThrowingRunnable<E> attempt) {
        return getResult(() -> {
            attempt.run();
            return null;
        });
    }

    private static <R extends AutoCloseable, E extends Throwable> Value<Void, E>
    getResult(ThrowingSupplier<R, E> resource, ThrowingConsumer<R, E> attempt) {
        return getResult(resource, r -> {
            attempt.accept(r);
            return null;
        });
    }

    private static <E extends Throwable> E errorFound(Throwable err) {
        // In some cases--e.g. when not using a method reference--the
        // actual type effectively does not get cast, as `E` is already
        // the type of `err` at runtime. Any ClassCastException will
        // occur when it is first retrieved, i.e. Result#handle.
        try {
            return (E) err;
        } catch (ClassCastException e) {
            throw wrongErrorFound(err);
        }
    }

    /** Forwards `err` and informs the user that the wrong kind of error was caught. */
    private static RuntimeException wrongErrorFound(Throwable err) {
        error("Unable to handle error in wrapper: ", err);
        return runEx("Wrong type of error caught by wrapper.");
    }

    /** A DTO holding any values or errors yielded. */
    private static class Value<T, E extends Throwable> {
        final Optional<T> result;
        final Optional<E> err;

        Value(Optional<T> result, Optional<E> err) {
            this.result = result;
            this.err = err;
        }

        /**
         * A generic Value with no result or error. The type parameters would be
         * lost at runtime and thus do not matter anyway.
         */
        static Value ok() {
            return new Value(full(NullObject.NULL_OBJECT), empty());
        }
    }

    /** Demonstrates that only the correct type of error will be caught by the wrapper. */
    public static void testErrorHandling() {
        Result.of(() -> falseError()).handle(Result::IGNORE); // -> crashes; wrong type of result.
    }

    /** A function which throws a different kind of error than its signature indicates. */
    private static void falseError() throws IOException {
        throw new RuntimeException("Actual error.");
    }
}