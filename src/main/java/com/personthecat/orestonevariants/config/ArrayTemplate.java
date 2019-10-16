package com.personthecat.orestonevariants.config;

import java.util.List;

/** A helper class for passing around array data from config files. */
public interface ArrayTemplate<T> {
    String getName();
    List<T> getValues();
}
