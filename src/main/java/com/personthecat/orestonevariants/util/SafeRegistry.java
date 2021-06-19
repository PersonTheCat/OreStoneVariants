package com.personthecat.orestonevariants.util;

import com.google.common.collect.ImmutableMap;
import com.personthecat.orestonevariants.exception.MissingElementException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.f;

/**
 * A non-redundant set of objects which can only be written to the first time it is referenced.
 * In OSV 6.2, this type was modified to support registry keys and be resettable. Resetting this
 * registry is expected to be thread-safe, but may need further testing.
 *
 * <p>
 *   This is the primary type of registry used in this mod. This registry is lazily initialized
 * and cannot be modified. It is constructed with a data supplier which is then treated as an
 * event. This event will fire at the first time any of this class' methods are called. It is
 * equipped with all of the necessary overrides to be treated as a regular {@link Set}.
 *
 *   Many of these methods are simply stubs which will throw an {@link UnsupportedOperationException}
 * when called. They are not intended for use by external implementors.
 * </p>
 */
public class SafeRegistry<K, V> extends ResettableLazy<ImmutableMap<K, V>> implements Map<K, V>, Iterable<V> {

    /** The default error to throw on {@link SafeRegistry#getAsserted} */
    private static final Function<Object, String> DEFAULT_ERROR = k -> f("No value for key: {}", k);

    /** An error to throw on {@link SafeRegistry#getAsserted} */
    final Function<K, String> errorMessage;

    /** Constructs Lazy ImmutableSet of objects which will be filled upon first use. */
    private SafeRegistry(Supplier<Map<K, V>> supplier, Function<K, String> errorMessage) {
        super(() -> ImmutableMap.copyOf(supplier.get()));
        this.errorMessage = errorMessage;
    }

    /** A much cleaner way of constructing the registry. */
    public static <K, V> SafeRegistry<K, V> of(Supplier<Map<K, V>> supplier) {
        return new SafeRegistry<>(supplier, DEFAULT_ERROR::apply);
    }

    public static <K, V> SafeRegistry<K, V> of(Supplier<Map<K, V>> supplier, Function<K, String> errorMessage) {
        return new SafeRegistry<>(supplier, errorMessage);
    }

    public static <V> SafeRegistry<Integer, V> enumerated(Supplier<Collection<V>> supplier) {
        return new SafeRegistry<>(() -> enumerate(supplier.get()), DEFAULT_ERROR::apply);
    }

    private static <V> Map<Integer, V> enumerate(Collection<V> values) {
        final Map<Integer, V> enumerated = new HashMap<>();
        int i = 0;
        for (V value : values) {
            enumerated.put(i++, value);
        }
        return enumerated;
    }

    public Optional<V> getOptional(K k) {
        return Optional.ofNullable(this.get(k));
    }

    public V getAsserted(K k) {
        final V v = this.get(k);
        if (v == null) {
            throw new MissingElementException(this.errorMessage.apply(k));
        }
        return v;
    }

    public Set<V> getAll(Collection<K> ks) {
        return ks.stream().map(this::getAsserted).collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return this.get().size();
    }

    @Override
    public boolean isEmpty() {
        return this.get().isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.get().containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return this.get().containsValue(o);
    }

    @Override
    public V get(Object o) {
        return this.get().get(o);
    }

    @Nullable
    @Override
    @Deprecated
    public V put(K k, V v) {
        return this.get().put(k, v);
    }

    @Override
    @Deprecated
    public V remove(Object o) {
        return this.get().remove(o);
    }

    @Override
    @Deprecated
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        this.get().putAll(map);
    }

    @Override
    @Deprecated
    public void clear() {
        this.get().clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return this.get().keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.get().values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.get().entrySet();
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return this.get().values().iterator();
    }
}