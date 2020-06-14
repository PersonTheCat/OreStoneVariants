package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.util.CommonMethods;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.state.IBlockState;
import org.hjson.JsonObject;

import java.io.File;
import java.util.*;

import static com.personthecat.orestonevariants.io.SafeFileIO.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

/** Settings used for spawning optional stone veins in the world. */
public class StoneProperties {
    /** A reference to the stone block being spawned by the mod. */
    public final Lazy<IBlockState> stone;
    /** All of the settings used for spawning this block in the world. */
    public final List<WorldGenProperties> gen;

    /** The name of the directory containing all of the presets. */
    public static final String FOLDER = "/" + Main.MODID + "/stone/";
    /** The path leading to the folder. */
    public static final File DIR = new File(getConfigDir() + FOLDER);
    /** All of the additional StoneProperties not pertaining to an in-use background. */
    private static final List<String> ADDITIONAL_NAMES = Arrays.asList("dirt", "gravel", "TUTORIAL");

    public StoneProperties(String stoneLookup, List<WorldGenProperties> gen) {
        this.stone = new Lazy<>(() -> getBlockState(stoneLookup).orElseThrow(() -> noBlockNamed(stoneLookup)));
        this.gen = gen;
    }

    /** All of the stone property names that should exist by default. */
    public static List<String> getDefaultNames() {
        final List<String> names = new ArrayList<>(ADDITIONAL_NAMES);
        for (BlockGroup.DefaultInfo info : BlockGroup.DefaultInfo.values()) {
            names.addAll(list(info.getNames()));
        }
        names.remove("stone"); // No need to spawn stone inside of stone.
        return names;
    }

    /** Generates a new StoneProperties object from the input file. */
    private static StoneProperties fromFile(File f) {
        final JsonObject root = readJson(f).orElseThrow(() -> runExF("Invalid hjson file: {}.", f.getPath()));
        final IBlockState state = getObject(root, "block")
            .flatMap(block -> getString(block, "location"))
            .flatMap(CommonMethods::getBlockState)
            .orElseThrow(() -> runExF("Invalid or missing block @[{}].block.location.", f));
        final String stoneLookup = getObject(root, "block")
            .flatMap(block -> getString(block, "location"))
            .orElseThrow(() -> runExF("Missing block @[{}].block.location.", f));
        return new StoneProperties(stoneLookup, WorldGenProperties.list(stoneLookup, getArrayOrNew(root, "gen")));
    }

    /** Generates properties for all of the presets inside of the directory. */
    public static Set<StoneProperties> setupStoneProperties() {
        final Set<StoneProperties> properties = new HashSet<>();
        for (File f : safeListFiles(DIR)) {
            if (!f.getName().equals("TUTORIAL.hjson")) {
                properties.add(fromFile(f));
            }
        }
        return properties;
    }
}