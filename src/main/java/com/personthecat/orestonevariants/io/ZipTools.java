package com.personthecat.orestonevariants.io;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.util.unsafe.Result;
import com.personthecat.orestonevariants.util.unsafe.Void;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static com.personthecat.orestonevariants.io.SafeFileIO.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class ZipTools {
    /** The directory containing this mod's zip files. */
    private static final String DIR = f("{}/{}/", FMLPaths.CONFIGDIR.get(), Main.MODID);
    /** The name of this mod's resource pack. */
    private static final String NAME = "resources.zip";
    /** The resource pack containing this mod's textures. */
    public static final File RESOURCE_PACK = new File(DIR, NAME);
    /** The internal path to the resource pack. */
    private static final String RP_JAR_PATH = f("/assets/{}/{}", Main.MODID, NAME);

    /** Tests for the base resource pack and copies it from the jar, if absent. */
    public static void copyResourcePack() {
        if (!RESOURCE_PACK.exists()) {
            mkdirs(RESOURCE_PACK.getParentFile())
                .expectF("Unable to create {}.", RESOURCE_PACK.getParentFile().getPath());
            copyStream(getRequiredResource(RP_JAR_PATH), RESOURCE_PACK.getPath())
                .expect("Unable to copy resource pack from the jar.");
        }
    }

    /** Generates an empty zip file at the location of `zip`. */
    public static Result<Void, IOException> createEmptyZip(File zip) {
        if (!zip.exists()) {
            return Result.with(() -> new ZipOutputStream(new FileOutputStream(zip))).of(zos -> {});
        }
        return Result.ok();
    }

    /** Determines whether the input path is present in this zip. */
    public static boolean fileInZip(File zip, String path) {
        return Result.with(() -> new ZipFile(zip))
            .of(zipFile -> zipFile.getEntry(path) != null)
            .orElseGet(e -> false);
    }

    /** Returns a stream of any paths from `files` already present in the zip. */
    public static Set<String> getExistingPaths(File zip, FileSpec... files) {
        return Result.with(() -> new ZipFile(zip))
            .of(zf -> { return getExistingPaths(zf, files); })
            .orElseGet(e -> Collections.emptySet());
    }

    /** Returns a stream of any paths from `files` already present in the zip. */
    public static Set<String> getExistingPaths(ZipFile zip, FileSpec... files) {
        return getPaths(files).filter(path -> zip.getEntry(path) != null)
            .collect(Collectors.toSet());
    }

    /** Maps the file array to its paths. */
    private static Stream<String> getPaths(FileSpec... files) {
        return Stream.of(files).map(spec -> spec.path);
    }

    /** Copies an array of files into the mod's resource pack. */
    public static Result<Boolean, IOException> copyToResources(FileSpec... files) {
        return copyToZip(RESOURCE_PACK, false, files);
    }

    public static Result<Boolean, IOException> copyToZip(File zip, boolean allowReplace, FileSpec... files) {
        // allowReplace ? don't skip anything : skip any existing file.
        final Set<String> skip = allowReplace
            ? Collections.emptySet()
            : getExistingPaths(zip, files);
        // All files present && no replace -> do nothing.
        if (skip.size() == files.length) {
            return Result.ok(true);
        }
        return doCopy(zip, skip, files);
    }

    private static Result<Boolean, IOException> doCopy(File zip, Set<String> skip, FileSpec... files) {
        return Result.of(() -> {
            // Move the original file to a temporary location.
            File tmp = moveReplace(zip, getTemporaryZip(RESOURCE_PACK.getParentFile())).throwIfErr();
            // Create a new zip in the original location.
            try (ZipFile tmpZip = new ZipFile(tmp); ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
                copyOriginals(tmpZip, zos, skip);
                addFiles(zos, files);
            }
            // Clean up.
            return tmp.delete();
        });
    }

    /** Returns a new temporary file inside of `directory`. */
    private static File getTemporaryZip(File directory) throws IOException {
        return File.createTempFile("osv_", ".zip", directory);
    }

    /** Copies any entry in `from` that does not exist in `skip`. */
    private static void copyOriginals(ZipFile from, ZipOutputStream zos, Set<String> skip) {
        from.stream().filter(entry -> !skip.contains(entry.getName()))
            .forEach(entry -> moveEntry(from, zos, entry)
            .expect("Unrecoverable error when copying file to zip.")); // Must be thrown; avoid mem leak.
    }

    /** Copies from a series of new InputStreams into the zip file. */
    private static void addFiles(ZipOutputStream zos, FileSpec... files) throws IOException {
        for (FileSpec file : files) {
            moveToZip(file.is, zos, new ZipEntry(file.path));
        }
    }

    /** Copies a zip entry between two zip files. */
    private static Result<Void, IOException> moveEntry(ZipFile from, ZipOutputStream to, ZipEntry entry) {
        return Result.of(() -> moveToZip(from.getInputStream(entry), to, entry));
    }

    /** Handles the one-time operation of copying a file into a ZipOutputStream. */
    private static void moveToZip(InputStream is, ZipOutputStream zos, ZipEntry entry) throws IOException {
        zos.putNextEntry(entry);
        copyStream(is, zos, 1024).throwIfErr();
        is.close();
    }
}