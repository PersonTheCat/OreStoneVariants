package com.personthecat.orestonevariants.util;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** A simple (incomplete) class for quickly retrieving values given two keys. */
public class DualMap<K1, K2, V> {
    private final Map<K1, Map<K2, V>> map;

    /** Primary constructor. Use builder. */
    private DualMap(Map<K1, Map<K2, V>> map) {
        this.map = map;
    }

    /** Retrieves a value given two keys. */
    @Nullable
    public V get(K1 k1, K2 k2) {
        final Map<K2, V> k2Get = map.get(k1);
        if (k2Get != null) {
            return k2Get.get(k2);
        }
        return null;
    }

    /** A builder used for adding all values to the map. */
    public static class Builder<K1, K2, V> {
        private final Map<K1, Map<K2, V>> map = new HashMap<>();

        public Builder() {}

        /** Add a new value to the map. */
        public Builder put(K1 k1, K2 k2, V v) {
            map.putIfAbsent(k1, new HashMap<>());
            map.get(k1).put(k2, v);
            return this;
        }

        /** Converts the normal HashMap version of this class into an ImmutableMap. */
        public DualMap<K1, K2, V> build() {
            // The top level is not yet immutable. Handle contents first.
            final Map<K1, Map<K2, V>> immutable = new HashMap<>();
            for (Map.Entry<K1, Map<K2, V>> entry : map.entrySet()) {
                immutable.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
            }
            return new DualMap<>(ImmutableMap.copyOf(immutable));
        }
    }
}