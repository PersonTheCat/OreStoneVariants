package com.personthecat.orestonevariants.util;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A non-redundant set of objects which can only be written to the first time it is referenced.
 */
public class SafeRegistry<E> extends Lazy<ImmutableSet<E>> implements Set<E> {

    /** Constructs Lazy ImmutableSet of objects which will be filled upon first use. */
    public SafeRegistry(Supplier<Collection<E>> supplier) {
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

    @Override
    public Iterator<E> iterator() {
        return get().iterator();
    }

    @Override
    public Object[] toArray() {
        return get().toArray();
    }

    @Override
    public <E> E[] toArray(E[] other) {
        return get().toArray(other);
    }

    @Override
    public boolean add(E element) {
        return get().add(element);
    }

    @Override
    public boolean remove(Object element) {
        return get().remove(element);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return get().containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return get().addAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return get().retainAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return get().removeAll(collection);
    }

    @Override
    public void clear() {
        get().clear();
    }
}