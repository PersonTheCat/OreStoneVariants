package com.personthecat.orestonevariants.io;

import com.personthecat.orestonevariants.util.CommonMethods;
import lombok.extern.log4j.Log4j2;
import personthecat.fresult.Result;
import personthecat.fresult.Void;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.nullable;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

/** A few potentially controversial ways for handling errors in file io. */
@Log4j2
public class SafeFileIO {

    private static final File BACKUP_DIR = new File(CommonMethods.getOSVDir(), "backups");

    /** For outputting the correct new line type. */
    private static final String NEW_LINE = System.lineSeparator();

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
        try { // Standard mkdirs() call.
            return Result.ok(file.mkdirs());
        } catch (SecurityException e) {
            // Error found. Return it.
            return Result.err(e);
        }
    }

    /** Checks whether @param file exists, neatly throwing @param error, if needed. */
    public static boolean fileExists(File file, String err) {
        return Result.<Boolean, SecurityException>of(file::exists).expect(err);
    }

    /** Equivalent of calling File#listFiles. Does not return null. */
    public static File[] safeListFiles(File dir) {
        return nullable(dir.listFiles()).orElse(new File[0]);
    }

    @CheckReturnValue
    public static File[] safeListFiles(File dir, FileFilter filter) {
        return Optional.ofNullable(dir.listFiles(filter)).orElse(new File[0]);
    }

    /** Attempts to retrieve the contents of the input file. */
    public static Optional<List<String>> contents(File file) {
        return Result.of(() -> Files.readAllLines(file.toPath())).get(Result::IGNORE);
    }

    /** Moves a file to the backup directory. */
    public static int backup(File file) {
        if (!fileExists(BACKUP_DIR, "Unable to handle backup directory.")) {
            mkdirs(BACKUP_DIR).expect("Creating directory");
        }
        final File backup = new File(BACKUP_DIR, file.getName());
        final BackupHelper helper = new BackupHelper(file);
        final int count = helper.cycle();
        if (fileExists(backup, "Unable to handle existing backup file.")) {
            throw runExF("Could not rename backups: {}", file.getName());
        }
        if (!file.renameTo(backup)) {
            throw runExF("Error moving {} to backups", file.getName());
        }
        return count + 1;
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

    /** Determines whether an asset is present in the jar or resources directory. */
    public static boolean resourceExists(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return internalResourceExists(path) || fileExists(ResourceHelper.file(path), path);
    }

    /** Determines whether an asset is present in the jar. */
    private static boolean internalResourceExists(String path) {
        try (InputStream io = SafeFileIO.class.getResourceAsStream(path)) {
            return io != null;
        } catch (IOException ignored) {
            return false;
        }
    }

    /** Retrieves an asset from the jar file or resources directory. */
    public static Optional<InputStream> getResource(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        final InputStream fromJar = SafeFileIO.class.getResourceAsStream(path);
        if (fromJar != null) {
            return full(fromJar);
        }
        final File inResources = ResourceHelper.file(path);
        if (fileExists(inResources, inResources.getPath())) {
            try {
                return full(new FileInputStream(inResources));
            } catch (IOException ignored) {}
        }
        return empty();
    }

    /** Retrieves an asset from the jar file or resources directory. */
    public static InputStream getRequiredResource(String path) {
        return getResource(path)
            .orElseThrow(() -> runExF("The required file \"{}\" does not exist.", path));
    }

    /** Returns a resource from the jar or resources as a string. */
    public static Optional<String> getResourceAsString(String path) {
        return getResource(path).flatMap(SafeFileIO::readString);
    }

    /** Parses an input stream as a regular string. */
    private static Optional<String> readString(InputStream is) {
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();

        try {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(NEW_LINE);
            }
            return full(sb.toString());
        } catch (IOException e) {
            log.error("Error reading input.");
            return empty();
        } finally {
            try {
                is.close();
                br.close();
            } catch (IOException e) {
                log.error("Error closing streams", e);
            }
        }
    }

    private static class BackupHelper {
        final String base;
        final String ext;
        final Pattern pattern;

        BackupHelper(File file) {
            final String name = file.getName();
            final int dotIndex = name.indexOf(".");
            if (dotIndex > 0) {
                base = name.substring(0, dotIndex);
                ext = name.substring(dotIndex);
            } else {
                base = name;
                ext = "";
            }
            pattern = Pattern.compile(base + "(\\s\\((\\d+)\\))?" + ext);
        }

        int cycle() {
            final File[] arr = BACKUP_DIR.listFiles(this::matches);
            if (arr == null) return 0;
            final List<File> matching = Arrays.asList(arr);
            matching.sort(this::compare);
            final int end = this.getFirstGap(matching);
            for (int i = end - 1; i >= 0; i--) {
                final File f = matching.get(i);
                final int number = i + 1;
                final File newFile = new File(f.getParentFile(), base + " (" + number + ")" + ext);
                if (!f.renameTo(newFile)) {
                    throw runExF("Could not increment backup: {}", f.getName());
                }
            }
            return matching.size();
        }

        boolean matches(File file) {
            return pattern.matcher(file.getName()).matches();
        }

        private int compare(File f1, File f2) {
            return Integer.compare(this.getNumber(f1), this.getNumber(f2));
        }

        int getNumber(File file) {
            final Matcher matcher = pattern.matcher(file.getName());
            if (!matcher.find()) throw runExF("Backup deleted externally: {}", file.getName());
            final String g2 = matcher.group(2);
            return g2 == null ? 0 : Integer.parseInt(g2);
        }

        int getFirstGap(List<File> files) {
            int lastNum = 0;
            for (File f : files) {
                final int num = this.getNumber(f) + 1;
                if (num - lastNum > 1) {
                    return lastNum;
                }
                lastNum = num;
            }
            return files.size();
        }
    }
}