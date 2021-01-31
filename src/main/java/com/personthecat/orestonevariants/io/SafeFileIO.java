package com.personthecat.orestonevariants.io;

import personthecat.fresult.Result;
import personthecat.fresult.Void;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.nullable;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

/** A few potentially controversial ways for handling errors in file io. */
public class SafeFileIO {

    /**
     * Ensures that the input @param file refers to a directory,
     * creating one if nothing is found.
     */
    @CheckReturnValue
    public static Result<Boolean, SecurityException> ensureDirExists(File file) {
        return Result.of(() -> file.exists() || file.mkdirs());
    }

    /** Safely calls File#mkdirs without testing. */
    @CheckReturnValue
    public static Result<Boolean, SecurityException> mkdirs(File file) {
        return Result.of(file::mkdirs);
    }

    /** Checks whether @param file exists, neatly throwing @param error, if needed. */
    public static boolean fileExists(File file, String err) {
        return Result.<Boolean, SecurityException>of(file::exists).expect(err);
    }

    /** Equivalent of calling File#listFiles. Does not return null. */
    public static File[] safeListFiles(File dir) {
        return nullable(dir.listFiles()).orElse(new File[0]);
    }

    /** Attempts to retrieve the contents of the input file. */
    public static Optional<List<String>> contents(File file) {
        return Result.of(() -> Files.readAllLines(file.toPath())).get(Result::IGNORE);
    }

    /** Moves a file, replacing the original when present or creating one, if not. */
    @CheckReturnValue
    public static Result<File, IOException> moveReplace(File from, File to) {
        return Result.of(() -> {
            if (to.exists() || to.createNewFile()) {
                return Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();
            }
            throw new IOException("Unable to find or create file: " + to.getPath());
        });
    }

    /** Standard stream copy process. Returns an exception, instead of throwing it. */
    @CheckReturnValue
    public static Result<Void, IOException> copyStream(InputStream input, OutputStream output, int bufferSize) {
        return Result.of(() -> {
            final byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        });
    }

    /** Convenience variant of copyStream(). */
    @CheckReturnValue
    public static Result<Void, IOException> copyStream(InputStream is, String path) {
        return Result.with(() -> new FileOutputStream(path))
            .of(o -> { copyStream(is, o, 1024).throwIfErr(); });
    }

    /** Retrieves an asset from the jar file. */
    public static Optional<InputStream> getResource(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return nullable(SafeFileIO.class.getResourceAsStream(path));
    }

    /** Retrieves an asset from the jar file */
    public static InputStream getRequiredResource(String path) {
        return getResource(path)
            .orElseThrow(() -> runExF("The required file \"{}\" was not present in the jar.", path));
    }
}