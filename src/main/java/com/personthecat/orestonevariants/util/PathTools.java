package com.personthecat.orestonevariants.util;

import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;

/** A collection of tools used for interacting with OSV texture paths. */
public class PathTools {

    /**
     * Converts the input path into a fully-qualified ResourceLocation.
     *
     * @param path The raw file URI being decoded.
     */
    public static ResourceLocation getResourceLocation(String path) {
        final String namespace = getNamespaceFromPath(path);
        final String result = path
            .replaceAll("(assets|data)[/\\\\]", "")
            .replaceAll(namespace + "[/\\\\]", "")
            .replaceAll("^[/\\\\]", "");
        return new ResourceLocation(namespace, result);
    }

    /**
     * Looks for the first path component after one of the root resource folders.
     *
     * @param path The raw file URI being decoded.
     * @return The namespace of the mod this refers to, if possible, else itself.
     */
    private static String getNamespaceFromPath(String path) {
        boolean rootFound = false;
        for (String s : path.split("[/\\\\]")) {
            if (rootFound && !s.isEmpty()) {
                return s;
            }
            rootFound |= "assets".equals(s) || "data".equals(s);
        }
        return path;
    }

    /**
     * Reuses a foreign path as a sub path. This function will specifically
     * place the namespace after <code>block/</code> or <code>item/</code>
     *
     * For example, the resource ID <code>minecraft:block/id</code> will be
     * converted into <code>block/minecraft/id</code>
     *
     * Note: this is unable to output backslashes instead of forward slashes.
     */
    public static String namespaceToSub(ResourceLocation id) {
        return id.getPath().replaceFirst("(block|item|^)[/\\\\]?", "$1/" + id.getNamespace() + "/");
    }

    /** Shorthand for {@link #namespaceToSub(ResourceLocation)} using a regular string. */
    public static String namespaceToSub(String id) {
        return namespaceToSub(new ResourceLocation(id));
    }

    /**
     * Prepends a new string of text before the last part of a file path.
     *
     * Note: empty strings would probably produce invalid results.
     */
    public static String prependFilename(String path, String prefix) {
        return path.replaceFirst("^(.*[/\\\\])*([^/\\\\]+)$", "$1" + prefix + "$2");
    }

    /** Ensures that the input path does not refer to a special *OSV* texture. */
    public static String ensureNormal(String path) {
        return path.replaceAll("_shaded|dense_", "");
    }

    /** Ensures that the input path refers to a dense texture. */
    public static String ensureDense(String path) {
        final String name = filename(path = ensureNormal(path));
        return path.replace(name, "dense_" + name);
    }

    /** Variant of #ensureDense which accepts a ResourceLocation. */
    public static ResourceLocation ensureDense(ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), ensureDense(location.getPath()));
    }

    /** Ensures that the input path refers to a shaded texture. */
    public static String ensureShaded(String path) {
        final String name = filename(path = ensureNormal(path));
        final String noExt = removeExtension(name);
        return path.replace(noExt, noExt + "_shaded");
    }

    /** Returns the end of the input path. */
    public static String filename(String path) {
        final String[] split = path.split("[/\\\\]");
        return split[split.length - 1];
    }

    /** Returns the full contents of `s` up to the last dot. */
    public static String removeExtension(String s) {
        final int extIndex = s.lastIndexOf(".");
        if (extIndex < 0) {
            return s;
        }
        return s.substring(0, extIndex);
    }

    public static Stream<String> getSimpleContents(File current) {
        return getSimpleContents(current, current);
    }

    public static Stream<String> getSimpleContents(File root, File current) {
        final File dir = current.isDirectory() ? current : current.getParentFile();
        return list(safeListFiles(dir)).stream()
            .map(f -> formatContents(root, f));
    }

    private static String formatContents(File root, File f) {
        final String edit = f.getAbsolutePath()
            .replace(root.getAbsolutePath(), "")
            .replace("\\", "/")
            .substring(1);
        return removeExtension(edit);
    }
}