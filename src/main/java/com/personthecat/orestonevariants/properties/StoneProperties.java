package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.blocks.BlockGroups;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.world.BlockListRuleTest;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.RuleTest;
import org.hjson.JsonArray;
import org.hjson.JsonObject;

import java.io.File;
import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.getBlockState;
import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.CommonMethods.list;
import static com.personthecat.orestonevariants.util.HjsonTools.getArrayOrNew;
import static com.personthecat.orestonevariants.util.HjsonTools.getBoolOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getObject;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;
import static com.personthecat.orestonevariants.util.HjsonTools.readJson;

/** Settings used for spawning optional stone veins in the world. */
@Log4j2
@AllArgsConstructor
public class StoneProperties {

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
        "TUTORIAL", "minecraft/dirt", "minecraft/gravel", "minecraft/magma_block",
        "minecraft/blackstone", "quark/quark_myalite", "quark/quark_voidstone"
    };

    /** All of the stone property names that should exist by default. */
    public static List<String> getDefaultNames() {
        final List<String> names = list(ADDITIONAL_NAMES);
        for (BlockGroups.DefaultInfo info : BlockGroups.DefaultInfo.values()) {
            for (String formatted : info.getFormatted()) {
                names.add(f("{}/{}", info.getName(), formatted));
            }
        }
        names.remove("minecraft/stone"); // No need to spawn stone inside of stone.
        return names;
    }

    /** Generates a new StoneProperties object from the input file. */
    private static Optional<StoneProperties> fromFile(File f) {
        final JsonObject root = readJson(f).orElseThrow(() -> runExF("Invalid hjson file: {}.", f.getPath()));
        if (!getBoolOr(root, "enabled", true)) {
            log.info("Skipping {}. There is a preset, but it is disabled", f.getName());
            return empty();
        }
        log.info("Loading new stone properties: {}", f.getName());
        final String id = getObject(root, "block")
            .flatMap(block -> getString(block, "location"))
            .orElseThrow(() -> runExF("Missing block @[{}].block.location.", f));
        if (!modCheck(id)) {
            return empty();
        }
        final BlockState state = getBlockState(id)
            .orElseThrow(() -> runExF("Invalid block @[{}].block.location.", f));
        log.info("Loaded stone with state: {}", state);
        final List<WorldGenProperties> gen = WorldGenProperties.list(getArrayOrNew(root, "gen"), null);
        final RuleTest source = BlockListRuleTest.from(getArrayOrNew(root, "source"));
        return full(new StoneProperties(state, source, gen));
    }

    /** Determines whether to load these properties according to their mod. */
    private static boolean modCheck(String id) {
        // Extract the namespace manually so that variant
        // data can be used without loading the block.
        if (id.contains(":")) {
            final String mod = id.split(":")[0];
            return !Cfg.modFamiliar(mod) || Cfg.modEnabled(mod);
        }
        return true;
    }

    /** Generates properties for all of the presets inside of the directory. */
    public static Map<ResourceLocation, StoneProperties> setupStoneProperties() {
        return PresetLocator.collect(DIR, StoneProperties::fromFile, p -> p.stone.getBlock().getRegistryName());
    }
}