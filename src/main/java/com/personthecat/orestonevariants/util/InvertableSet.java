package com.personthecat.orestonevariants.util;

import java.util.*;

/**
 * This object is a wrapper for any other List implementor. It provides
 * an additional function, `check` which returns the opposite of `contains`
 * provided that `blacklist` is enabled. All other functions (add, iterator,
 * etc.) remain the same.
 */
public class InvertableSet<T> implements Set<T> {
    private boolean blacklist = false;
    private final Set<T> set;

    public InvertableSet(Set<T> set) {
        this.set = set;
    }

    public static <T> InvertableSet<T> wrap(Set<T> set) {
        return new InvertableSet<>(set);
    }

    public InvertableSet<T> setBlacklist(boolean b) {
        blacklist = b;
        return this;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public InvertableSet<T> setWhitelist(boolean b) {
        blacklist = !b;
        return this;
    }

    public boolean isWhitelist() {
        return !blacklist;
    }

    public boolean check(Object o) {
        return blacklist != set.contains(o);
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return set.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return set.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public void clear() {
        set.clear();
    }
}
