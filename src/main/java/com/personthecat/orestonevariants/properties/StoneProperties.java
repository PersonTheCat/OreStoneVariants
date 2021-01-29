package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.util.CommonMethods;
import com.personthecat.orestonevariants.world.BlockListRuleTest;
import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.template.RuleTest;
import org.hjson.JsonObject;

import java.io.File;
import java.util.*;

import static com.personthecat.orestonevariants.io.SafeFileIO.safeListFiles;
import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.CommonMethods.list;
import static com.personthecat.orestonevariants.util.HjsonTools.getArrayOrNew;
import static com.personthecat.orestonevariants.util.HjsonTools.getBoolOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getObject;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;
import static com.personthecat.orestonevariants.util.HjsonTools.readJson;

/** Settings used for spawning optional stone veins in the world. */
@AllArgsConstructor
public class StoneProperties {

    /** Whether to place these blocks in the world. */
    public final boolean enabled;

    /** A reference to the stone block being spawned by the mod. */
    public final BlockState stone;

    /** A destination where this block will be spawned. */
    public final RuleTest source;

    /** All of the settings used for spawning this block in the world. */
    public final List<WorldGenProperties> gen;

    /** The name of the directory containing all of the presets. */
    private static final String FOLDER = "/stone/";

    /** The path leading to the folder. */
    public static final File DIR = new File(getOSVDir() + FOLDER);

    /** All of the additional StoneProperties not pertaining to an in-use background. */
    private static final String[] ADDITIONAL_NAMES = {
        "TUTORIAL", "dirt", "gravel", "magma_block", "blackstone"
    };

    /** All of the stone property names that should exist by default. */
    public static List<String> getDefaultNames() {
        final List<String> names = list(ADDITIONAL_NAMES);
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
        final List<WorldGenProperties> gen = WorldGenProperties.list(getArrayOrNew(root, "gen"));
        final RuleTest source = BlockListRuleTest.from(getArrayOrNew(root, "source"));
        final boolean enabled = getBoolOr(root, "enabled", true);
        return new StoneProperties(enabled, state, source, gen);
    }

    /** Generates properties for all of the presets inside of the directory. */
    public static Set<StoneProperties> setupStoneProperties() {
        final Set<StoneProperties> properties = new HashSet<>();
        for (File f : safeListFiles(DIR)) {
            final StoneProperties stone = fromFile(f);
            if (stone.enabled) {
                properties.add(stone);
            }
        }
        return properties;
    }
}