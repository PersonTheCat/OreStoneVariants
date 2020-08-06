package com.personthecat.orestonevariants.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/** A DTO containing information about a file that needs to be created. */
public class FileSpec {
    public final InputStream is;
    public final String path;

    public FileSpec(InputStream is, String path) {
        this.is = is;
        this.path = path;
    }

    public FileSpec(File f, String path) throws FileNotFoundException {
        this.is = new FileInputStream(f);
        this.path = path;
    }
}