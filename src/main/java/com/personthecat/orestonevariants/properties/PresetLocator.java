package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Reference;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.personthecat.orestonevariants.io.SafeFileIO.safeListFiles;
import static com.personthecat.orestonevariants.util.CommonMethods.extension;

@Log4j2
public class PresetLocator {

    private static final String TUTORIAL_NAME = "TUTORIAL.hjson";

    /** Collects every preset in the given directory and subdirectories into a set. */
    public static <T> Set<T> collect(File dir, Function<File, Optional<T>> reader) {
        final Set<T> set = new HashSet<>();
        for (File f : safeListFiles(dir, PresetLocator::validPresetOrDir)) {
            if (f.isDirectory()) {
                collect(f, reader);
            } else if (Cfg.ignoreInvalidPresets.get()) {
                try {
                    reader.apply(f).ifPresent(set::add);
                } catch (RuntimeException e) {
                    log.error("Skipping {} due to error", f.getName(), e);
                }
            } else {
                reader.apply(f).ifPresent(set::add);
            }
        }
        return set;
    }

    private static boolean validPresetOrDir(File f) {
        if (TUTORIAL_NAME.equals(f.getName())) {
            return false;
        }
        return f.isDirectory() || Reference.VALID_EXTENSIONS.contains(extension(f));
    }
}
