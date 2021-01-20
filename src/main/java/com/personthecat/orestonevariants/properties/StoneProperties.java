package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.util.CommonMethods;
import net.minecraft.block.BlockState;
import org.hjson.JsonObject;

import java.io.File;
import java.util.*;

import static com.personthecat.orestonevariants.io.SafeFileIO.safeListFiles;
import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

/** Settings used for spawning optional stone veins in the world. */
public class StoneProperties {
    /** A reference to the stone block being spawned by the mod. */
    public final BlockState stone;
    /** All of the settings used for spawning this block in the world. */
    public final List<WorldGenProperties> gen;

    /** The name of the directory containing all of the presets. */
    private static final String FOLDER = "/stone/";
    /** The path leading to the folder. */
    public static final File DIR = new File(getOSVDir() + FOLDER);
    /** All of the additional StoneProperties not pertaining to an in-use background. */
    private static final List<String> ADDITIONAL_NAMES = Arrays.asList("dirt", "gravel", "TUTORIAL");

    public StoneProperties(BlockState stone, List<WorldGenProperties> gen) {
        this.stone = stone;
        this.gen = gen;
    }

    /** All of the stone property names that should exist by default. */
    public static List<String> getDefaultNames() {
        final List<String> names = new ArrayList<>(ADDITIONAL_NAMES);
        for (BlockGroup.DefaultInfo info : BlockGroup.DefaultInfo.values()) {
            names.addAll(info.getValues());
        }
        names.remove("stone"); // No need to spawn stone inside of stone.
        return names;
    }

    /** Generates a new StoneProperties object from the input file. */
    private static StoneProperties fromFile(File f) {
        final JsonObject root = readJson(f).orElseThrow(() -> runExF("Invalid hjson file: {}.", f.getPath()));
        final BlockState state = getObject(root, "block")
            .flatMap(block -> getString(block, "location"))
            .flatMap(CommonMethods::getBlockState)
            .orElseThrow(() -> runExF("Invalid or missing block @[{}].block.location.", f));
        return new StoneProperties(state, WorldGenProperties.list(getArrayOrNew(root, "gen")));
    }

    /** Generates properties for all of the presets inside of the directory. */
    public static Set<StoneProperties> setupStoneProperties() {
        final Set<StoneProperties> properties = new HashSet<>();
        for (File f : safeListFiles(DIR)) {
            properties.add(fromFile(f));
        }
        return properties;
    }
}