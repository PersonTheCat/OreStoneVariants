package com.personthecat.orestonevariants.util.unsafe;

import com.personthecat.orestonevariants.util.Lazy;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
 *   In essence, I wrote this class not because it was necessary or wise, but
 * to push myself and prove that I could. I may someday remove it, but I will
 * definitely be having fun with it in the meantime.
 *
 * ¯\_(ツ)_/¯
 *
 * @param <T> The type of value to be returned.
 * @param <E> The type of error to be caught.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked"})
public class Result<T, E extends Throwable> {
    /**
     * A singleton Result object used to avoid unnecessary instantiation
     * for Results that yield no value and produce no errors.
     */
    private static final Result<Void, ?> OK = new Result<>(Value::ok);

    /**
     * Accepts an error and ignores it, while still coercing it to its lowest type, thus
     * not allowing any unspecified error types to be ignored.
     * e.g. `Result.of(...).get(Result::IGNORE);`
     */
    public static <E extends Throwable> void IGNORE(E e) {}
    /**
     * Accepts an error and logs it as a warning. This implementation is also type safe.
     * e.g. `Result.of(...).get(Result::WARN);`
     */
    public static <E extends Throwable> void WARN(E e) { warn("{}", e); }
    /**
     * Accepts an error and throws it. This implementation is also type safe.
     * e.g. `Result.of(...).get(Result::THROW);`
     */
    public static <E extends Throwable> void THROW(E e) { throw runEx(e); }

    /**
     * A lazily-initialized result of the supplied operation. This value must be assigned
     * by the constructor, but is effectively null until a method for handling any potential
     * errors has been implemented.
     */
    private final Lazy<Value<T, E>> result;

    /**
     * Constructs a new result with a raw, un-calculated value. This is an internal
     * method which typically should not be needed by external libraries. Use
     * Result#of to quietly generate a new value supplier.
     */
    private Result(Supplier<Value<T, E>> attempt) {
        result = new Lazy<>(attempt);
    }

    /**
     * Variant of the primary constructor, used when a Value object has already been
     * created. This is an internal method which typically should not be needed by
     * external libraries. Use Result#manual or Result#ok for manual instantiation.
     */
    private Result(Value<T, E> value) {
        result = new Lazy<>(value);
    }

    /**
     * Returns a generic result containing no value or error. Use this method whenever
     * no operation needs to occur, but a Result value is being returned.
     */
    public static <E extends Throwable> Result<Void, E> ok() {
        return (Result<Void, E>) OK;
    }

    /**
     * Returns a new result with a non-null value and no errors. Use this method whenever
     * no operation needs to occur, but a value exists and must be wrapped in a Result.
     */
    public static <T, E extends Throwable> Result<T, E> ok(T val) {
        return new Result<>(new Value<>(full(val), empty()));
    }

    /**
     * This is the primary method for constructing a new Result object using any operation
     * which will either yield a value or produce an error.
     *
     * e.g. ```
     *   File f = getFile();
     *   // Get the result of calling f#mkdirs,
     *   // handling a potential SecurityException.
     *   boolean success = Result.of(f::mkdirs)
     *     .get(e -> {...}); // Handle the error, get the value.
     * ```
     */
    public static <T, E extends Throwable> Result<T, E> of(ThrowingSupplier<T, E> attempt) {
        return new Result<>(() -> getResult(attempt));
    }

    /**
     * Constructs a new Result from an operation which will not yield a value.
     *
     * e.g. ```
     *   // Functional equivalent to a standard try-catch block.
     *   Result.of(ClassName::thisWillFail)
     *     .get(e -> {...});
     * ```
     */
    public static <E extends Throwable> Result<Void, E> of(ThrowingRunnable<E> attempt) {
        return new Result<>(() -> getResult(attempt));
    }

    /**
     * Constructs a new Result from an operation which may neither err or return a value.
     *
     * e.g. ```
     *   // Use this to call a function which may either throw
     *   // an exception or simply return null. Call Optional#orElse
     *   // or Optional#orElseGet to substitute the value, if null.
     *   Object result = Result.nullable(ClassName::thisMayReturnNullOrFail)
     *     .expect("Error message!") // Potentially null value wrapped in Optional<T>.
     *     .orElseGet(() -> new Object());
     * ```
     */
    public static <T, E extends Throwable> Result<Optional<T>, E> nullable(ThrowingSupplier<T, E> attempt) {
        return new Result<>(() -> getResult(() -> Optional.ofNullable(attempt.get())));
    }

    /**
     * Constructs a handler for generating a new Result from an operation with resources.
     * The implementation returned, Result$WithResource, allows one additional `with`
     * clause, enabling multiple resources to be chained, consumed, and closed. This is
     * the functional equivalent of using a standard try-with-resources block.
     *
     * e.g. ```
     *   File f = getFile();
     *   // Attempt to generate a new FileWriter and
     *   // use it write lines into `f`.
     *   Result.with(() -> new FileWriter(f))
     *     .of(writer -> writer.write("Hello World!"))
     *     .expect("Tried to write. It failed.");
     * ```
     */
    public static <R extends AutoCloseable, E extends Throwable> WithResource<R, E> with(ThrowingSupplier<R, E> resource) {
        return new WithResource<>(resource);
    }

    /**
     * Shorthand method for following a Result#with call with a WithResource#of call.
     * Use this for example with convoluted Result#with calls that require additional
     * lines to avoid unnecessary indentation.
     *
     * e.g. ``
     *   File f = getFile();
     *   Result.with(() -> new FileWriter(f), writer -> {
     *     writer.write("Hello World!");
     *   }.expect("Tried to write. It failed.");
     * ``
     */
    public static <R extends AutoCloseable, T, E extends Throwable>
    Result<T, E> with(ThrowingSupplier<R, E> resource, ThrowingFunction<R, T, E> attempt) {
        return with(resource).of(attempt);
    }

    /**
     * Shorthand method for following a Result#with call with a WithResource#of call.
     * Use this for an alternative to Result#with(ThrowingSupplier, ThrowingFunction)
     * that does not return a value.
     */
    public static <R extends AutoCloseable, E extends Throwable>
    Result<Void, E> with(ThrowingSupplier<R, E> resource, ThrowingConsumer<R, E> attempt) {
        return with(resource).of(attempt);
    }

    /**
     * Wraps any exceptions from the array into a new Result object. Unfortunately,
     * type erasure implies Results with different error types may be joined. As a
     * result, this is even less safe than it needs to be. I may create an additional
     * handler for joint Results, but not yet.
     */
    public static <E extends Throwable> Result<Void, E> join(Result<Void, E>... results) {
        return of(() -> {
            for (Result<Void, E> result : results) {
                result.throwIfErr();
            }
        });
    }

    /**
     * Returns whether an error occurred in the process.
     *
     * e.g. ```
     *   Result<Void, RuntimeException> result = getResult();
     *   // Compute the result and proceed only if it errs.
     *   if (result.isErr()) {
     *       ...
     *   }
     * ```
     */
    public boolean isErr() {
        return getError().isPresent();
    }

    /**
     * Accepts an expression for what to do in the event of and error
     * being present. Use this whenever you want to functionally handle
     * both code paths (i.e. error vs. value).
     */
    public Result<T, E> ifErr(Consumer<E> func) {
        if (isErr()) {
            func.accept(getError().get());
        }
        return this;
    }

    /**
     * Returns whether a value was yielded or no error occurred.
     *
     * e.g. ```
     *   Result<Void, RuntimeException> result = getResult();
     *   // Compute the result and prodeed only if it does not err.
     *   if (result.isOk()) {
     *       ...
     *   }
     * ```
     */
    public boolean isOk() {
        return getValue().isPresent();
    }

    /**
     * Accepts an expression for what to do in the event of and error
     * being present. Use this whenever you want to functionally handle
     * both code paths (i.e. error vs. value).
     */
    public Result<T, E> ifOk(Consumer<T> func) {
        if (isOk()) {
            func.accept(getValue().get());
        }
        return this;
    }

    /**
     * Accepts an expression for handling the expected error, if present.
     * Use this whenever you want to get the error, but still have
     * access to the Result wrapper.
     *
     * e.g. ```
     *   Result.of(() -> getValue())
     *     .get(e -> {...})
     *     .ifPresent(t -> {...});
     * ```
     */
    public Optional<T> get(Consumer<E> func) {
        try {
            getError().ifPresent(func);
        } catch (ClassCastException e) {
            // An error is known to be present because a cast
            // was attempted on it.
            throw wrongErrorFound(getError().get());
        }
        return getValue();
    }

    /**
     * Replaces the underlying value with the result of func.apply.
     * Use this whenever you need to map a potential value to a
     * different value.
     *
     * e.g. ```
     *   int secretArrayLength = Result.of(() -> getSecretArray())
     *      .map(array -> array.length) // Return the length instead.
     *      .get(e -> {...}) // Handle any errors.
     *      .orElse(0); // Didn't work. Call Optional#orElse().f
     * ```
     */
    public <M> Result<M, E> map(Function<T, M> func) {
        final Optional<M> mapped = getValue().map(func);
        final Optional<E> err = getError();
        return new Result<>(new Value<>(mapped, err));
    }

    /**
     * Maps to a new Result if an error is present. Use this whenever
     * your first and second attempt at retrieving a value may fail.
     */
    public Result<T, E> tryIfErr(ThrowingFunction<E, T, E> func) {
        return isErr() ? Result.of(() -> func.apply(getError().get())) : this;
    }

    /**
     * Replaces the entire value with a new result, if present.
     * Use this whenever you need to map a potential value to
     * another function which yields a Result.
     */
    public Result<T, E> andThen(Function<T, Result<T, E>> func) {
        if (isOk()) {
            // A value is present. There should be no error.
            return func.apply(get().get());
        }
        return this;
    }

    /** Returns the value or throws the exception, whichever possible. */
    public T throwIfErr() throws E {
        throwIfErr(getError());
        return result.get().result.orElseThrow(() -> runEx("No value or error present in result."));
    }

    /**
     * Yields the underlying value, throwing a convenient, generic exception,
     * if an error occurs.
     *
     * e.g. ```
     *   // Runs an unsafe process, wrapping any original errors.
     *   Object result = getResult()
     *     .expect("Unable to get value from result.");
     * ```
     */
    public T expect(String message) {
        get(err -> { throw new RuntimeException(message, err); });
        return getValue().orElseThrow(() -> runEx(message));
    }

    /** Formatted variant of #expect. */
    public T expectF(String message, Object... args) {
        return expect(f(message, args));
    }

    /**
     * Yields the underlying value or else the input. This is equivalent to
     * running `getResult().get(e -> {...}).orElse();`
     */
    public T orElse(T val) {
        return getValue().orElse(val);
    }

    /**
     * Effectively casts this object into a standard Optional instance,
     * rendering all errors caught effectively moot.
     */
    public Optional<T> get() {
        if (isErr()) { // Don't ignore all possible exceptions.
            throw runEx(getError().get());
        }
        return getValue();
    }

    /**
     * Variant of Result#get which simultaneously handles a potential
     * error and yields a substitute value. Equivalent to following a
     * Result#get call with a call to Optional#orElseGet.
     */
    public T orElseGet(Function<E, T> func) {
        try {
            return getError().isPresent() ? func.apply(getError().get()) : getValue().orElseThrow(() ->
                runEx("No error or value was found in wrapper. Use Result#nullable.")
            );
        } catch (ClassCastException e) {
            // An error is known to be present because a cast
            // was attempted on it.
            throw wrongErrorFound(getError().get());
        }
    }

    /**
     * Variant of Result#orElseGet(Function) which does not specifically
     * consume the error, making *all* errors effectively moot. Shorthand
     */
    public T orElseGet(Supplier<T> func) {
        try {
            return getError().isPresent() ? func.get() : getValue().orElseThrow(() ->
                runEx("No error or value was found in wrapper. Use Result#nullable.")
            );
        } catch (ClassCastException e) {
            // An error is known to be present because a cast
            // was attempted on it.
            throw wrongErrorFound(getError().get());
        }
    }

    /** Removes some of the boilerplate from above. */
    private Optional<T> getValue() {
        return result.get().result;
    }

    /** Also removes some of the boilerplate form above. */
    private Optional<E> getError() {
        return result.get().err;
    }

    /** Runs the underlying process and converts it into a Result.Value. */
    private static <T, E extends Throwable> Value<T, E> getResult(ThrowingSupplier<T, E> attempt) {
        Optional<T> result = empty();
        Optional<E> err = empty();
        try {
            result = full(attempt.get());
        } catch (Throwable e) {
            err = full(errorFound(e));
        }
        return new Value<>(result, err);
    }

    /** Variant of Result#getResult which never yields a value. */
    private static <E extends Throwable> Value<Void, E> getResult(ThrowingRunnable<E> attempt) {
        return getResult(() -> {
            attempt.run();
            return Void.INSTANCE;
        });
    }

    /** Attempts to cast the error into the appropriate subclass. */
    private static <E extends Throwable> E errorFound(Throwable err) {
        // In some cases--e.g. when not using a method reference--the
        // actual type effectively does not get cast, as `E` is already
        // the type of `err` at runtime. Any ClassCastException will
        // occur when it is first retrieved, i.e. Result#get.
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

    /** Throws an optional error. */
    private static <E extends Throwable> void throwIfErr(Optional<E> err) throws E {
        if (err.isPresent()) {
            throw err.get();
        }
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
            return new Value(full(Void.INSTANCE), empty());
        }
    }

    /**
     * A helper classed used for generating Results with a single closeable resource.
     * Equivalent to using try-with-resources.
     *
     * @param <R> The resource being consumed and closed by this handler.
     * @param <E> The type of error to be caught by the handler.
     */
    public static class WithResource<R extends AutoCloseable, E extends Throwable> {
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
            return new Result(() -> getResult(attempt));
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
            return new WithResources(rGetter, resource);
        }

        /** Constructs a new handler with an addition resource yielded from the first. */
        public <R2 extends AutoCloseable> WithResources<R, R2, E> with(ThrowingFunction<R, R2, E> getter) {
            return new WithResources(rGetter, () -> getter.apply(rGetter.get()));
        }

        /** Variant of Result#getResult based on the standard try-with-resources functionality. */
        private <T, E2 extends Throwable> Value<T, E> getResult(ThrowingFunction<R, T, E2> attempt) {
            Optional<T> result = empty();
            Optional<E> err = empty();
            try (R r = rGetter.get()) {
                result = full(attempt.apply(r));
            } catch (Throwable e) {
                err = full(errorFound(e));
            }
            return new Value<>(result, err);
        }
    }

    /**
     * A helper classed used for generating Results with two closeable resources.
     * Equivalent to using try-with-resources.
     *
     * @param <R1> The resource being consumed and closed by this handler.
     * @param <R2> The second resource being consumed and closed by this handler.
     * @param <E> The type of error to be caught by the handler.
     */
    public static class WithResources<R1 extends AutoCloseable, R2 extends AutoCloseable, E extends Throwable> {
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
            return new Result(() -> getResult(attempt));
        }

        /** Constructs a new Result, consuming the resources without yielding a value. */
        public <E2 extends Throwable> Result<Void, E2> of(ThrowingBiConsumer<R1, R2, E2> attempt) {
            return of((r1, r2) -> {
                attempt.accept(r1, r2);
                return Void.INSTANCE;
            });
        }

        /** Variant of Result#getResult based on the standard try-with-resources functionality. */
        private <T, E2 extends Throwable> Value<T, E> getResult(ThrowingBiFunction<R1, R2, T, E2> attempt) {
            Optional<T> result = empty();
            Optional<E> err = empty();
            try (R1 r1 = r1Getter.get(); R2 r2 = r2Getter.get()) {
                result = full(attempt.apply(r1, r2));
            } catch (Throwable e) {
                err = full(errorFound(e));
            }
            return new Value<>(result, err);
        }
    }
}