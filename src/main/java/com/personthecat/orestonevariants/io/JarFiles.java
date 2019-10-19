package com.personthecat.orestonevariants.io;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.util.unsafe.Result;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;

public class JarFiles {
    /** A setting indicating where all presets will be kept. */
    private static final String FOLDER = "/" + Main.MODID + "/presets";
    /** A reference to the actual directory containing the presets. */
    private static final File PRESET_DIR = new File(FMLPaths.CONFIGDIR.get() + FOLDER);

    /** Copies the default presets from the jar to the disk. */
    public static void copyPresetFiles() {
        // Verify the folder's integrity before proceeding.
        ensureDirExists(PRESET_DIR).expect("Error creating the preset directory.");

        for (String name : PropertyGroup.DefaultInfo.getAllNames()) {
            final String from = f("data/{}/presets/{}.hjson", Main.MODID, name);
            final String to = f("{}/{}.hjson", PRESET_DIR.getPath(), name);
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