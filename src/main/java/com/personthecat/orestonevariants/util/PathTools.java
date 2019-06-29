package com.personthecat.orestonevariants.util;

import net.minecraft.util.ResourceLocation;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** A collection of tools used for interacting with OSV texture paths. */
public class PathTools {
    /** Converts the input path into a ResourceLocation */
    public static ResourceLocation getResourceLocation(String path) {
        final String[] split = path
            .split("[/\\\\]");
        final String namespace = startsWithAny(path, "assets", "data") ?
            split[1] : split[0];
        final String result = path
            .replaceAll("assets/|data/", "")
            .replace(namespace + "/", "");
        return new ResourceLocation(namespace, result);
    }

    /** Ensures that the input path does not refer to a special *OSV* texture. */
    public static String ensureNormal(String path) {
        return path.replaceAll("_shaded|dense_", "");
    }

    /** Ensures that the input path refers to a dense texture. */
    public static String ensureDense(String path) {
        final String name = filename(path);
        return name.contains("dense_") ? path : path.replace(name, "dense_" + name);
    }

    /** Ensures that the input path refers to a shaded texture. */
    public static String ensureShaded(String path) {
        final String name = filename(path);
        return name.contains("_shaded") ? path : path.replace(name, name + "_shaded");
    }

    /** Returns the end of the input path. */
    public static String filename(String path) {
        final String[] split = path.split("[/\\\\]");
        return split[split.length - 1];
    }
}