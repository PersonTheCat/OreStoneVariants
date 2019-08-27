package com.personthecat.orestonevariants.util;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A list of objects which can only be written to the first time it is referenced.
 */
public class LazyImmutableList<E> extends Lazy<ImmutableList<E>> implements Iterable<E> {
    /** Constructs Lazy ImmutableSet of objects which will be filled upon first use. */
    public LazyImmutableList(Supplier<ImmutableList<E>> supplier) {
        super(supplier);
    }

    public static <E> LazyImmutableList<E> of(Supplier<ImmutableList<E>> supplier) {
        return new LazyImmutableList<>(supplier);
    }

    @Override
    public Iterator<E> iterator() {
        return get().iterator();
    }
}