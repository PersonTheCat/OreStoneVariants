package com.personthecat.orestonevariants.util;

import com.google.common.collect.ImmutableMap;
import java.util.function.Supplier;

/**
 * A list of objects which can only be written to the first time it is referenced.
 */
public class LazyImmutableMap<K, V> extends Lazy<ImmutableMap<K, V>> {
    /** Constructs Lazy ImmutableSet of objects which will be filled upon first use. */
    public LazyImmutableMap(Supplier<ImmutableMap<K, V>> supplier) {
        super(supplier);
    }

    public static <K, V> LazyImmutableMap<K, V> of(Supplier<ImmutableMap<K, V>> supplier) {
        return new LazyImmutableMap<>(supplier);
    }
}