package com.personthecat.orestonevariants.io;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.util.unsafe.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;

public class JarFiles {
    /** A reference to the actual directory containing the ore presets. */
    private static final File ORES = OreProperties.DIR;
    /** The directory containing the stone generation variables. */
    private static final File STONE = StoneProperties.DIR;

    /** Copies the default presets from the jar to the disk. */
    public static void copyPresetFiles() {
        copyFilesFrom(ORES, PropertyGroup.DefaultInfo.getAllNames());
        copyFilesFrom(STONE, StoneProperties.getDefaultNames());
    }

    /** Copies all of presets in the given directory to the disk. */
    private static void copyFilesFrom(File dir, List<String> names) {
        // Verify the folder's integrity before proceeding.
        ensureDirExists(dir).expect("Error creating the preset directory.");

        for (String name : names) {
            final String from = f("data/{}/{}/{}.hjson", Main.MODID, dir.getName(), name);
            final String to = f("{}/{}.hjson", dir.getPath(), name);
            info("copying from [{}] to [{}]", from, to);
            if (!fileExists(new File(to), "Error validating preset file.")) {
                copyFile(from, to);
            }
        }
    }

    /** Copies any file from the jar to the disk. */
    private static void copyFile(String from, String to) {
        Result.with(() -> new FileOutputStream(to), fos -> {
            InputStream toCopy = getRequiredResource(from);
            copyStream(toCopy, fos, 1024).throwIfErr();
        }).expect("Error copying file from the jar.");
    }
}