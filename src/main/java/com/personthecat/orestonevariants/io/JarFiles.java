package com.personthecat.orestonevariants.io;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.properties.StoneProperties;
import lombok.extern.log4j.Log4j2;
import personthecat.fresult.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import static com.personthecat.orestonevariants.io.SafeFileIO.copyStream;
import static com.personthecat.orestonevariants.io.SafeFileIO.ensureDirExists;
import static com.personthecat.orestonevariants.io.SafeFileIO.fileExists;
import static com.personthecat.orestonevariants.io.SafeFileIO.getRequiredResource;
import static com.personthecat.orestonevariants.util.CommonMethods.f;

@Log4j2
public class JarFiles {

    /** A reference to the actual directory containing the ore presets. */
    private static final File ORES = OreProperties.DIR;

    /** The directory containing the stone generation variables. */
    private static final File STONE = StoneProperties.DIR;

    /** The directory containing this mod's generated resources. */
    private static final File RESOURCES = ResourceHelper.DIR;

    /** The mcmeta file used for this mod's resource pack. */
    private static final String PACK_MCMETA = "pack.mcmeta";

    /** The internal path to the pack.mcmeta file. */
    private static final String PACK_MCMETA_PATH = "assets/" + Main.MODID + "/" + PACK_MCMETA;

    /** Copies the default presets from the jar to the disk. */
    public static void copyFiles() {
        copyPresetsTo(ORES, PropertyGroup.DefaultInfo.getAllNames());
        copyPresetsTo(STONE, StoneProperties.getDefaultNames());
        copyMcMeta();
    }

    /** Copies all of presets in the given directory to the disk. */
    private static void copyPresetsTo(File dir, List<String> names) {
        // Verify the folder's integrity before proceeding.
        ensureDirExists(dir).expect("Error creating the preset directory.");

        for (String name : names) {
            final String from = f("data/{}/{}/{}.hjson", Main.MODID, dir.getName(), name);
            final String to = f("{}/{}.hjson", dir.getPath(), name);
            log.info("copying from [{}] to [{}]", from, to);
            if (!fileExists(new File(to), "Error validating preset file.")) {
                copyFile(from, to);
            }
        }
    }

    /** Copies the MC meta file from the jar to the resources directory. */
    private static void copyMcMeta() {
        final File file = ResourceHelper.file(PACK_MCMETA);
        ensureDirExists(RESOURCES).expect("Error creating resources directory.");

        if (!fileExists(file, "Error validating mcmeta file")) {
            copyFile(PACK_MCMETA_PATH, file.getPath());
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