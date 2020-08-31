package com.personthecat.orestonevariants.io;

import java.io.*;
import java.util.function.Supplier;
import personthecat.fresult.interfaces.ThrowingSupplier;

/** A DTO containing information about a file that needs to be created. */
public class FileSpec {
    public final ThrowingSupplier<InputStream, IOException> is;
    public final String path;

    public FileSpec(Supplier<InputStream> is, String path) {
        this.is = is::get;
        this.path = path;
    }

    public FileSpec(File f, String path) {
        this.is = () -> new FileInputStream(f);
        this.path = path;
    }
}