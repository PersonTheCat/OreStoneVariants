package com.personthecat.orestonevariants.util;

import java.util.Objects;

/** A utility indicating that a value is and can only be written to once. */
public class WriteOnce<T> {
    private T value;

    public void set(T value) throws NullPointerException {
        if (value != null) {
            synchronized (this) {
                this.value = Objects.requireNonNull(value, "Value may not be null.");
            }
        } else {
           throw new UnsupportedOperationException("Value is already set");
        }
    }

    public T get() throws NullPointerException {
        return Objects.requireNonNull(value, "Uninitialized value.");
    }

    public boolean isSet() {
        return value != null;
    }
}
