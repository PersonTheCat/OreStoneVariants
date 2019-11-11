package com.personthecat.orestonevariants.util.unsafe;

import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.interfaces.*;

import java.util.*;
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
 * Edit Nov. 10, 2019: This class has spawned an entirely new project. It will
 * eventually be molded into its own library, at which point I'm not sure what
 * I'll do with it.
 *
 * ¯\_(ツ)_/¯
 *
 * @param <T> The type of value to be returned.
 * @param <E> The type of error to be caught.
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class Result<T, E extends Throwable> {
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
    Result(Supplier<Value<T, E>> attempt) {
        result = new Lazy<>(attempt);
    }

    /**
     * Variant of the primary constructor, used when a Value object has already been
     * created. This is an internal method which typically should not be needed by
     * external libraries. Use Result#manual or Result#ok for manual instantiation.
     */
    Result(Value<T, E> value) {
        result = new Lazy<>(value);
    }

    /**
     * Returns a generic result containing no value or error. Use this method whenever
     * no operation needs to occur, but a Result value is being returned.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Throwable> Result<Void, E> ok() {
        return (Result<Void, E>) Handled.OK;
    }

    /**
     * Returns a new result with a non-null value and no errors. Use this method whenever
     * no operation needs to occur, but a value exists and must be wrapped in a Result.
     */
    public static <T, E extends Throwable> Result<T, E> ok(T val) {
        return new Handled<>(Value.ok(val));
    }

    /**
     * Returns a new result with an error and no value present. Use this method to enable
     * callers to respond functionally even though error handling has already occurred.
     */
    public static <T, E extends Throwable> Result<T, E> err(E e) {
        return new Handled<>(Value.err(e));
    }

    /**
     * This is the primary method for constructing a new Result object using any operation
     * which will either yield a value or produce an error.
     *
     * e.g. ```
     *   File f = getFile();
     *   // Get the result of calling f#mkdirs,
     *   // handling a potential SecurityException.d
     *   boolean success = Result.of(f::mkdirs)
     *     .orElseGet(e -> false); // ignore SecurityExceptions, return default.
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
     *     .ifErr(e -> {...});
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
    public static <R extends AutoCloseable, E extends Throwable>
    WithResource<R, E> with(ThrowingSupplier<R, E> resource) {
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
     *   }).expect("Tried to write. It failed.");
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
     * Creates a new protocol for handling the specified error type. Can be chained
     * with additional definitions to handle multiple different error types as needed.
     */
    public static <E extends Exception> Protocol define(Class<E> clazz, Consumer<E> func) {
        return new Protocol().define(clazz, func);
    }

    /**
     * Wraps any exceptions from the array into a new Result object. Unfortunately,
     * type erasure implies Results with different error types may be joined. As a
     * result, this is even less safe than it needs to be. I may create an additional
     * handler for joint Results, but not yet.
     */
    @SafeVarargs // Erasure can be ignored; all errors are thrown regardless. (?)
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
        return getErr().isPresent();
    }

    /**
     * Accepts an expression for what to do in the event of an error
     * being present. Use this whenever you want to functionally handle
     * both code paths (i.e. error vs. value).
     */
    public Result<T, E> ifErr(Consumer<E> func) {
        try {
            getErr().ifPresent(func);
        } catch (ClassCastException e) {
            // An error is known to be present because a cast
            // was attempted on it.
            throw wrongErrorFound(unwrapErr());
        }
        return handled();
    }

    /**
     * Returns whether a value was yielded or no error occurred.
     *
     * e.g. ```
     *   Result<Void, RuntimeException> result = getResult();
     *   // Compute the result and proceed only if it does not err.
     *   if (result.isOk()) {
     *       ...
     *   }
     * ```
     */
    public boolean isOk() {
        return getVal().isPresent();
    }

    /**
     * Accepts an expression for what to do in the event of no error
     * being present. Use this whenever you want to functionally handle
     * both code paths (i.e. error vs. value).
     */
    public Result<T, E> ifOk(Consumer<T> func) {
        getVal().ifPresent(func);
        // ok ? handled by default : type check needed.
        return isOk() ? handled() : this;
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
        return ifErr(func).getVal();
    }

    /**
     * Effectively casts this object into a standard Optional instance.
     * Prefer calling Result#get(Consumer), as this removes the need for
     * any implicit error checking.
     */
    public Optional<T> get() {
        errorCheck();
        return getVal();
    }

    /**
     * Private shorthand for retrieving the wrapped value. Implementors
     * should default to Result#get(Consumer).
     */
    private Optional<T> getVal() {
        return result.get().result;
    }

    /** Retrieves the underlying error, wrapped in Optional. */
    public Optional<E> getErr() {
        return result.get().err;
    }

    /**
     * Attempts to retrieve the underlying value, asserting that one must
     * exist.
     */
    public T unwrap() {
        return expect("Attempted to unwrap a result with no value.");
    }

    /**
     * Attempts to retrieve the underlying error, asserting that one must
     * exist.
     */
    public E unwrapErr() {
        return expectErr("Attempted to unwrap a result with no error.");
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
        return ifErr(e -> { throw runEx(message, e); }).getVal().orElse(null);
    }

    /** Formatted variant of #expect. */
    public T expectF(String message, Object... args) {
        return expect(f(message, args));
    }

    /**
     * Yields the underlying error, throwing a generic exception if no error
     * is present.
     */
    public E expectErr(String message) {
        return ifOk(t -> { throw runEx(message); }).getErr().orElse(null);
    }

    /** Formatted variant of #expectErr. */
    public E expectErrF(String message, Object... args) {
        return expectErr(f(message, args));
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
     *      .orElse(0); // Didn't work. Call Optional#orElse().
     * ```
     */
    public <M> Result<M, E> map(Function<T, M> func) {
        final Optional<M> mapped = getVal().map(func);
        final Optional<E> err = getErr();
        return new Result<>(new Value<>(mapped, err));
    }

    /**
     * Replaces the underlying error with the result of func.apply.
     * Use this whenever you need to map a potential error to another
     * error.
     */
    public <E2 extends Throwable> Result<T, E2> mapErr(Function<E, E2> func) {
        final Optional<T> val = getVal();
        final Optional<E2> mapped = getErr().map(func);
        return new Handled<>(new Value<>(val, mapped));
    }

    /**
     * Maps to a new Result if an error is present. Use this whenever
     * your first and second attempt at retrieving a value may fail.
     */
    public Result<T, E> orElseTry(ThrowingFunction<E, T, E> func) {
        // Error ? new result : handled by default
        return getErr().map(e -> Result.of(() -> func.apply(e))).orElse(handled());
    }

    /**
     * Replaces the entire value with a new result, if present.
     * Use this whenever you need to map a potential value to
     * another function which yields a Result.
     */
    public Result<T, E> andThen(Function<T, Result<T, E>> func) {
        // ok ? no error to check -> new Result : error not handled -> this
        return getVal().map(func).orElse(this);
    }

    /** Variant of Result#unwrap which throws the original error, if applicable.*/
    public T throwIfErr() throws E {
        throwIfPresent(getErr());
        return unwrap();
    }

    /**
     * Yields the underlying value or else the input. This is equivalent to
     * running `getResult().get(e -> {...}).orElse();`
     */
    public T orElse(T val) {
        // Just because an alt is supplied doesn't mean we should
        // ignore *any* possible errors that get thrown.
        errorCheck();
        return getVal().orElse(val);
    }

    /**
     * Variant of Result#get which simultaneously handles a potential
     * error and yields a substitute value. Equivalent to following a
     * Result#get call with a call to Optional#orElseGet.
     */
    public T orElseGet(Function<E, T> func) {
        try {
            // Error ? map to new value : value must exist
            return getErr().map(func).orElse(unwrap());
        } catch (ClassCastException e) {
            // An error is known to be present because a cast
            // was attempted on it.
            throw wrongErrorFound(unwrapErr());
        }
    }

    /**
     * Variant of Result#orElseGet(Function) which does not specifically
     * consume the error, making *all* errors effectively moot.
     */
    public T orElseGet(Supplier<T> func) {
        errorCheck(); // Never return a value without checking the error's type.
        return isErr() ? func.get() : unwrap();
    }

    /** Transposes a result with an optional value into an optional result. */
    @SuppressWarnings("unchecked")
    private <U> Optional<Result<U, E>> transpose() {
        if (isErr()) {
            return full(Result.err(unwrapErr()));
        }
        if (unwrap() instanceof Optional) {
            return ((Optional<U>) unwrap()).map(Result::ok);
        }
        throw runEx("Underlying value not wrapped in Optional<U>.");
    }

    /**
     * This function is used to determine whether an error has been handled
     * before a value is retrieved. If an error does exist and has not been
     * handled, it is thrown and reported as unhandled.
     */
    protected void errorCheck() {
        if (isErr()) {
            throw runExF("Unhandled error in wrapper: {}", unwrapErr());
        }
    }

    /**
     * Called whenever the state of the error has been handled to return a new
     * Result$Handled.
     */
    private Handled<T, E> handled() {
        return new Handled<>(result.get());
    }

    /** Runs the underlying process and converts it into a Result.Value. */
    private static <T, E extends Throwable> Value<T, E> getResult(ThrowingSupplier<T, E> attempt) {
        try {
            return Value.ok(attempt.get());
        } catch (Throwable e) {
            return Value.err(errorFound(e));
        }
    }

    /** Variant of Result#getResult which never yields a value. */
    private static <E extends Throwable> Value<Void, E> getResult(ThrowingRunnable<E> attempt) {
        return getResult(() -> {
            attempt.run();
            return Void.INSTANCE;
        });
    }

    /** Attempts to cast the error into the appropriate subclass. */
    @SuppressWarnings("unchecked")
    protected static <E extends Throwable> E errorFound(Throwable err) {
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
        error("Unable to handle error in wrapper: {}", err);
        return runEx("Wrong type of error caught by wrapper.");
    }

    /** Throws an optional error. */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <E extends Throwable> void throwIfPresent(Optional<E> err) throws E {
        if (err.isPresent()) {
            throw err.get();
        }
    }

    /**
     * An extension of the Result wrapper which aims to guarantee that potential errors
     * have already been handled. Thus, no unhandled error nag is necessary.
     */
    public static class Handled<T, E extends Throwable> extends Result<T, E> {
        /**
         * A singleton Result object used to avoid unnecessary instantiation
         * for Results that yield no value and produce no errors.
         */
        private static final Handled<Void, ?> OK = new Handled<>(Value.ok());

        /**
         * Constructs a new Result object after an error has been handled, removing some
         * safety checks and keeping various convenience functions intact.
         */
        Handled(Value<T, E> result) {
            super(result);
        }

        /** Because this wrapper has been handled, there is no need for error checking. */
        @Override protected void errorCheck() {/* Do nothing. */}
    }
}