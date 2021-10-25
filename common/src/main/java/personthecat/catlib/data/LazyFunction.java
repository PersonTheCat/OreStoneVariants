package personthecat.catlib.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;
import java.util.function.Function;

@ThreadSafe
public class LazyFunction<T, R> {
    protected R value = null;
    protected final Function<T, R> function;
    protected volatile boolean set;

    public LazyFunction(final Function<T, R> function) {
        this.function = function;
        this.set = false;
    }

    public static <T, R> LazyFunction<T, R> of(final Function<T, R> function) {
        return new LazyFunction<>(function);
    }

    @NotNull
    public R apply(final T t) {
        if (!this.set) {
            synchronized(this) {
                this.value = this.function.apply(t);
                this.set = true;
            }
        }
        return this.value;
    }

    public Lazy<R> asLazy(final T t) {
        return Lazy.of(() -> this.apply(t));
    }

    public Optional<R> getIfComputed() {
        return Optional.ofNullable(this.value);
    }

    public boolean computed() {
        return this.set;
    }

    public R applyUpdated(final T t) {
        return this.function.apply(t);
    }

    @Nullable
    public R expose() {
        return this.value;
    }

    public String toString() {
        return this.set ? this.value.toString() : "<unavailable>";
    }
}
