package com.personthecat.orestonevariants.util;

import com.personthecat.orestonevariants.util.unsafe.Result;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** A few potentially controversial ways for handling errors in file io. */
public class SafeFileIO {
    /**
     * Ensures that the input @param file refers to a directory,
     * creating one if nothing is found.
     */
    public static Result<Boolean, SecurityException> ensureDirExists(File file) {
        return Result.of(() -> file.exists() || file.mkdirs());
    }

    /** Safely calls File#mkdirs without testing. */
    public static Result<Boolean, SecurityException> mkdirs(File file) {
        return Result.of(file::mkdirs);
    }

    /** Checks whether @param file exists, neatly throwing @param error, if needed. */
    public static boolean fileExists(File file, String err) {
        return Result.<Boolean, SecurityException>of(file::exists).expectOrElse(err, false);
    }

    /** Copies a file to the specified directory. May look clean more than it is actually safe. */
    public static Result<Path, IOException> copy(File file, File toDir) {
        return Result.of(() -> Files.copy(file.toPath(), new File(toDir, file.getName()).toPath()));
    }

    /** Equivalent of calling File#listFiles. Does not return null(?). */
    public static Result<File[], SecurityException> listFiles(File dir) {
        return Result.of(() -> dir.listFiles());
    }

    /** Equivalent of calling File#listFiles. Does not return null. */
    public static Optional<File[]> safeListFiles(File dir) {
        return nullable(dir.listFiles());
    }

    /** Attempts to retrieve the contents of the input file. */
    public static Optional<List<String>> contents(File file) {
        return Result.of(() -> Files.readAllLines(file.toPath())).get(Result::IGNORE);
    }

    /** Writes `contents` to `file`, returning an IOException, if present. */
    public static Result<Void, IOException> write(File file, String contents) {
        return Result.of(() -> {
            final Writer tw = new FileWriter(file);
            tw.write(contents);
            tw.close();
        });
    }

    /** Standard stream copy process. Returns an exception, instead of throwing it. */
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
    public static Result<Void, IOException> copyStream(InputStream is, String path) {
        return Result.of(() -> {
            final FileOutputStream o = new FileOutputStream(path);
            final Result<Void, IOException> result = copyStream(is, o, 1024);
            o.close();
            result.throwIfErr();
        });
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