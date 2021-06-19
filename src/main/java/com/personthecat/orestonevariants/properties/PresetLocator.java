package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Reference;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.personthecat.orestonevariants.io.SafeFileIO.safeListFiles;
import static com.personthecat.orestonevariants.util.CommonMethods.extension;

@Log4j2
public class PresetLocator {

    private static final String TUTORIAL_NAME = "TUTORIAL.hjson";

    /** Collects every preset in the given directory and subdirectories into a set. */
    public static <K, V> Map<K, V> collect(File dir, Function<File, Optional<V>> reader, Function<V, K> key) {
        final Map<K, V> map = new HashMap<>();
        for (File f : safeListFiles(dir, PresetLocator::validPresetOrDir)) {
            if (f.isDirectory()) {
                collect(f, reader, key);
            } else if (Cfg.ignoreInvalidPresets.get()) {
                try {
                    reader.apply(f).ifPresent(v -> map.put(key.apply(v), v));
                } catch (RuntimeException e) {
                    log.error("Skipping {} due to error", f.getName(), e);
                }
            } else {
                reader.apply(f).ifPresent(v -> map.put(key.apply(v), v));
            }
        }
        return map;
    }

    private static boolean validPresetOrDir(File f) {
        if (TUTORIAL_NAME.equals(f.getName())) {
            return false;
        }
        return f.isDirectory() || Reference.VALID_EXTENSIONS.contains(extension(f));
    }
}
