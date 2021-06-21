package com.personthecat.orestonevariants.util;

import java.util.function.Supplier;

public class ResettableLazy<T> {

    /** The underlying value being wrapped by this object. */
    private T value = null;

    /** A supplier used for producing the value when it is ready. */
    private final Supplier<T> supplier;

    /** Whether the value has been setup. */
    private volatile boolean set;

    /** The primary constructor with instructions for producing the value. */
    public ResettableLazy(Supplier<T> supplier) {
        this.supplier = supplier;
        this.set = false;
    }

    /** To be used in the event that a value already exists. */
    public ResettableLazy(T value) {
        this.value = value;
        this.supplier = () -> value;
        this.set = true;
    }

    /** The primary method for retrieving the underlying value. */
    public T get() {
        if (!this.set) {
            synchronized(this) {
                value = supplier.get();
                this.set = true;
            }
        }
        return value;
    }

    /** Marks this object as being uninitialized. It will be loaded again on next use. */
    public synchronized ResettableLazy<T> reset() {
        this.set = false;
        this.value = null;
        return this;
    }

    /** Returns an up-to-date value without resetting this value's reference. */
    public T getUpdated() {
        return this.supplier.get();
    }
}
