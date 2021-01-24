package com.personthecat.orestonevariants.util;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A non-redundant set of objects which can only be written to the first time it is referenced.
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
public class SafeRegistry<E> extends Lazy<ImmutableSet<E>> implements Set<E> {

    /** Constructs Lazy ImmutableSet of objects which will be filled upon first use. */
    private SafeRegistry(Supplier<Collection<E>> supplier) {
        super(() -> ImmutableSet.copyOf(supplier.get()));
    }

    /** A much cleaner way of constructing the registry. */
    public static <E> SafeRegistry<E> of(Supplier<Collection<E>> supplier) {
        return new SafeRegistry<>(supplier);
    }

    @Override
    public int size() {
        return get().size();
    }

    @Override
    public boolean isEmpty() {
        return get().isEmpty();
    }

    @Override
    public boolean contains(Object element) {
        return get().contains(element);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return get().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return get().toArray();
    }

    @NotNull
    @Override
    public <E> E[] toArray(@NotNull E[] other) {
        return get().toArray(other);
    }

    @Override
    @Deprecated
    public boolean add(E element) {
        return get().add(element);
    }

    @Override
    @Deprecated
    public boolean remove(Object element) {
        return get().remove(element);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return get().containsAll(collection);
    }

    @Override
    @Deprecated
    public boolean addAll(@NotNull Collection<? extends E> collection) {
        return get().addAll(collection);
    }

    @Override
    @Deprecated
    public boolean retainAll(@NotNull Collection<?> collection) {
        return get().retainAll(collection);
    }

    @Override
    @Deprecated
    public boolean removeAll(@NotNull Collection<?> collection) {
        return get().removeAll(collection);
    }

    @Override
    @Deprecated
    public void clear() {
        get().clear();
    }
}