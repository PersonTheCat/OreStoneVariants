package com.personthecat.orestonevariants.util;

import com.personthecat.orestonevariants.config.ArrayTemplate;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.safeGet;

public class Group<T> {

    /** The name of a special group containing all items. */
    public static final String ALL = "all";

    /** The name of a special group containing default items. */
    public static final String DEFAULT = "default";

    /** All of the ore properties represented by this group. */
    public final Set<T> items;

    public Group(Set<T> items) {
        this.items = items;
    }

    public Group(T item) {
        this.items = Collections.singleton(item);
    }

    /** Returns the number of entries in this group. */
    public int size() {
        return items.size();
    }

    /** Determines whether a given key points to a hardcoded group type. */
    public static boolean isSpecial(String key) {
        return ALL.equalsIgnoreCase(key) || DEFAULT.equalsIgnoreCase(key);
    }

    /** Generates a group containing all registered items from all groups of this type. */
    public static <T> Group<T> createAllGroup(Map<String, Group<T>> groups) {
        final Set<T> items = new HashSet<>();
        for (Group<T> group : groups.values()) {
            items.addAll(group.items);
        }
        return new Group<>(items);
    }

    /** Generates a group containing all items from default groups of this type. */
    public static <T> Group<T> createDefaultGroup(ArrayTemplate<String>[] defaults, Map<String, Group<T>> groups) {
        final Set<T> items = new HashSet<>();
        // Find all groups with default values and reuse their items.
        for (ArrayTemplate<String> info : defaults) {
            final Set<T> updated = safeGet(groups, info.getName())
                .map(group -> group.items)
                .orElseGet(Collections::emptySet);
            items.addAll(updated);
        }
        return new Group<>(items);
    }
}
