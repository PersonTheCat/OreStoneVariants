package com.personthecat.orestonevariants.util;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** A few potentially controversial ways for handling errors in file io. */
public class SafeFileIO {
    /**
     * Ensures that the input @param file refers to a directory,
     * creating one if nothing is found.
     */
    public static Result<SecurityException> ensureDirExists(File file) {
        try { // Test for and create all directories.
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (SecurityException e) {
            // Error found. Return it.
            return Result.of(e);
        } // No error found. Return OK.
        return Result.ok();
    }

    /** Safely calls File#mkdirs without testing. */
    public static Result<SecurityException> safeMkdirs(File file) {
        try { // Standard mkdirs() call.
            file.mkdirs();
        } catch (SecurityException e) {
            // Error found. Return it.
            return Result.of(e);
        } // No error found. Return OK.
        return Result.ok();
    }

    /** Checks whether @param file exists, neatly throwing @param error, if needed. */
    public static boolean safeFileExists(File file, String error) {
        boolean ret;
        try {
            ret = file.exists();
        } catch (SecurityException e) {
            throw runEx(error);
        }
        return ret;
    }

    /** Copies a file to the specified directory. May look clean more than it is actually safe. */
    public static Result<IOException> safeCopy(File file, File toDir) {
        try {
            Files.copy(file.toPath(), new File(toDir, file.getName()).toPath());
        } catch (IOException e) {
            return Result.of(e);
        }
        return Result.ok();
    }

    /** Equivalent of calling File#listFiles. Does not return null. */
    public static Optional<File[]> safeListFiles(File dir) {
        return Optional.ofNullable(dir.listFiles());
    }

    /** Attempts to retrieve the contents of the input file. */
    public static Optional<List<String>> safeContents(File file) {
        try {
            return full(Files.readAllLines(file.toPath()));
        } catch (IOException ignored) {
            return empty();
        }
    }

    public static Result<IOException> safeWrite(File file, String contents) {
        try {
            Writer tw = new FileWriter(file);
            tw.write(contents);
            tw.close();
            return Result.ok();
        } catch (IOException e) {
            return Result.of(e);
        }
    }

    /** Standard stream copy process. Returns an exception, instead of throwing it. */
    public static Result<IOException> copyStream(InputStream input, OutputStream output, int bufferSize) {
        byte[] buffer = new byte[bufferSize];
        int length;
        try {
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } catch (IOException e) {
            return Result.of(e);
        }
        return Result.ok();
    }

    /** Convenience variant of copyStream(). */
    public static Result<IOException> copyStream(InputStream is, String path) {
        try {
            final FileOutputStream o = new FileOutputStream(path);
            final Result<IOException> result = copyStream(is, o, 1024);
            o.close();
            return result;
        } catch (IOException e) {
            return Result.of(e);
        }
    }

    /** Retrieves an asset from the jar file. */
    public static Optional<InputStream> getResource(String path) {
        return Optional.ofNullable(CommonMethods.class.getResourceAsStream(path));
    }

    /** Retrieves an asset from the jar file */
    public static InputStream getRequiredResource(String path) {
        return Optional.ofNullable(SafeFileIO.class.getResourceAsStream(path))
            .orElseThrow(() -> runExF("The required file \"{}\" was not present in the jar.", path));
    }
}