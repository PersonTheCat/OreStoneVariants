package com.personthecat.orestonevariants.config;

/** A helper class for passing around array data from config files. */
public interface ArrayTemplate<T> {
    String getName();
    T[] getValues();
}
