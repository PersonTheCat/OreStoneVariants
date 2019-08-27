package com.personthecat.orestonevariants.util;

import com.google.common.collect.ImmutableSet;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A set of objects which can only be written to the first time it is referenced.
 */
public class LazyImmutableSet<E> extends Lazy<ImmutableSet<E>> implements Iterable<E> {
    /** Constructs Lazy ImmutableSet of objects which will be filled upon first use. */
    public LazyImmutableSet(Supplier<ImmutableSet<E>> supplier) {
        super(supplier);
    }

    public static <E> LazyImmutableSet<E> of(Supplier<ImmutableSet<E>> supplier) {
        return new LazyImmutableSet<>(supplier);
    }

    @Override
    public Iterator<E> iterator() {
        return get().iterator();
    }
}