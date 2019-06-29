package com.personthecat.orestonevariants.util;

import com.personthecat.orestonevariants.Main;
import net.minecraftforge.fml.loading.FMLLoader;
import org.hjson.JsonObject;

import java.io.File;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class PropertyReader {
    /** The name of the directory containing all of the presets. */
    private static final String FOLDER = "/config/" + Main.MODID + "/presets/";
    /** The path leading to the folder. */
    private static final File DIR = new File(FMLLoader.getGamePath() + FOLDER);

    /** Attempts to load a JsonObject from the preset directory. */
    public static Optional<JsonObject> get(String name) {
        return empty();
    }
}