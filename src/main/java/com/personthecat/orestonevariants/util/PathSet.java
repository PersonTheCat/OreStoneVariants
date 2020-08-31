package com.personthecat.orestonevariants.util;

import java.util.Iterator;

/**
 * Generates and stores all of the different texture path
 * variants used by OSV.
 */
public class PathSet implements Iterable<String> {
    public final String normal;
    public final String shaded;
    public final String dense;

    public PathSet(String path, String ext) {
        this.normal = PathTools.ensureNormal(path) + ext;
        this.shaded = PathTools.ensureShaded(path) + ext;
        this.dense = PathTools.ensureDense(path) + ext;
    }

    public Iterator<String> iterator() {
        return new Iterator<String>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < 3;
            }

            @Override
            public String next() {
                switch (i++) {
                    case 0: return normal;
                    case 1: return shaded;
                    case 2: return dense;
                    default: return null;
                }
            }
        };
    }
}
