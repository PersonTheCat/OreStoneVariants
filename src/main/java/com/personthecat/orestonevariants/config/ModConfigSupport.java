package com.personthecat.orestonevariants.config;

import com.google.common.collect.ImmutableMap;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import javafx.util.Pair;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import org.hjson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;

public class ModConfigSupport {

    /** A list of fields to disable for each mod. */
    private static final Map<String, SettingData> DATA = new ImmutableMap.Builder<String, SettingData>()
        .put("iceandfire", new SettingData()
            .set("generation|generateSapphireOre", false)
            .set("generation|generateSilverOre", false))
        .put("AleXndr/simpleores", new SettingData()
            .set("Ores.Copper Ore|enableOreGen", false)
            .set("Ores.Tin Ore|enableOreGen", false)
            .set("Ores.Mythril Ore|enableOreGen", false)
            .set("Ores.Adamantium Ore|enableOreGen", false)
            .set("Ores.Onyx Ore|enableOreGen", false))
        .put("MMDLib", new SettingData()
            .set("general|using_orespawn", false)
            .set("general|fallback_orespawn", false))
        .put("glasshearts", new SettingData()
            .set("world|generateAgateOre", false)
            .set("world|generateAmethystOre", false)
            .set("world|generateOnyxOre", false)
            .set("world|generateOpalOre", false)
            .set("world|generateRubyOre", false)
            .set("world|generateSapphireOre", false)
            .set("world|generateTopazOre", false))
        .put("embers", new SettingData()
            .set("compat|aluminumVeinsPerChunk", 0)
            .set("ores|copperVeinsPerChunk", 0)
            .set("ores|leadVeinsPerChunk", 0)
            .set("compat|nickelVeinsPerChunk", 0)
            .set("ores|quartzVeinsPerChunk", 0)
            .set("ores|silverVeinsPerChunk", 0)
            .set("compat|tinVeinsPerChunk", 0))
        .put("immersiveengineering", new SettingData()
            .set("general.ores|ore_bauxite", 4, 40, 85, 0, 0)
            .set("general.ores|ore_copper", 8, 40, 72, 0, 0)
            .set("general.ores|ore_lead", 6, 8, 36, 0, 0)
            .set("general.ores|ore_nickel", 6, 8, 24, 0, 0)
            .set("general.ores|ore_uranium", 4, 8, 24, 0, 0))
        .put("thaumcraft_world", new SettingData()
            .set("general|generateAmber", false)
            .set("general|generateCinnabar", false)
            .set("general|generateQuartz", false))
        .put("metallurgy_reforged/worldgen", new SettingData()
            .set("ore_generation.adamantine|veinSize", 0)
            .set("ore_generation.astralsilver|veinSize", 0)
            .set("ore_generation.atlarus|veinSize", 0)
            .set("ore_generation.carmot|veinSize", 0)
            .set("ore_generation.copper|veinSize", 0)
            .set("ore_generation.deepiron|veinSize", 0)
            .set("ore_generation.infuscolium|veinSize", 0)
            .set("ore_generation.lutetium|veinSize", 0)
            .set("ore_generation.manganese|veinSize", 0)
            .set("ore_generation.mithril|veinSize", 0)
            .set("ore_generation.oureclase|veinSize", 0)
            .set("ore_generation.orichalcum|veinSize", 0)
            .set("ore_generation.osmium|veinSize", 0)
            .set("ore_generation.phosphorite|veinSize", 0)
            .set("ore_generation.platinum|veinSize", 0)
            .set("ore_generation.potash|veinSize", 0)
            .set("ore_generation.prometheum|veinSize", 0)
            .set("ore_generation.rubracium|veinSize", 0)
            .set("ore_generation.silver|veinSize", 0)
            .set("ore_generation.sulfur|veinSize", 0)
            .set("ore_generation.tin|veinSize", 0)
            .set("ore_generation.zinc|veinSize", 0)
            .set("ore_generation.alduorite|veinSize", 0)
            .set("ore_generation.ceruclase|veinSize", 0)
            .set("ore_generation.ignatius|veinSize", 0)
            .set("ore_generation.kalendrite|veinSize", 0)
            .set("ore_generation.lemurite|veinSize", 0)
            .set("ore_generation.midasium|veinSize", 0)
            .set("ore_generation.sanguinite|veinSize", 0)
            .set("ore_generation.shadowiron|veinSize", 0)
            .set("ore_generation.vulcanite|veinSize", 0)
            .set("ore_generation.vyroxeres|veinSize", 0)
            .set("ore_generation.eximite|veinSize", 0)
            .set("ore_generation.meutoite|veinSize", 0))
//        .put("osv", new SettingData() // Test
//            .set("world|enableOSVOres", false)
//            .set("world|enableOSVStone", false))
        .build();

    /** A map of aliases for each config file. */
    private static final Map<String, String> DATA_ALIASES = new ImmutableMap.Builder<String, String>()
        .put("simpleores", "AleXndr/simpleores")
        .put("basemetals", "MMDLib")
        .put("mmdlib", "MMDLib")
        .put("thaumcraft", "thaumcraft_world")
        .put("metallurgy", "metallurgy_reforged/worldgen")
        .build();

    /** The directory containing custom BOP presets. */
    private static final File BOP_PARENT = new File(getConfigDir() + "/biomesoplenty/biomes");
    /** The directory containing default BOP presets. */
    private static final File BOP_MAIN = new File(BOP_PARENT + "/defaults/biomesoplenty");
    /** The directory containing default vanilla presets. */
    private static final File BOP_VANILLA = new File(BOP_PARENT + "/defaults/vanilla");

    /** A list of json keys used to spawn ores in BOP configs. */
    private static final List<String> BOP_NAMES = list(
        "emeralds", "amber", "malachite", "peridot", "ruby", "sapphire", "tanzanite", "topaz", "amethyst"
    );

    /** A map storing Forge's auto-loaded configuration files. */
    private static final Map<String, Configuration> CONFIGS = ReflectionTools.getValue(
        ConfigManager.class, "CONFIGS", null
    );

    /** Searches for an alias to the input mod name. */
    private static String getCfgName(String mod) {
        mod = mod.toLowerCase();
        final Optional<String> alias = safeGet(DATA_ALIASES, mod);
        return alias.orElse(mod);
    }

    /** Attempts to retrieve an auto-loaded Configuration, else generates a new one. */
    private static Configuration getConfig(String mod) {
        final File cfg = new File(f("{}/{}.cfg", getConfigDir(), mod));
        return safeGet(CONFIGS, cfg.getAbsolutePath()).orElse(new Configuration(cfg));
    }

    /** Runs updates for the input mod name. Use `all` to run all updates. */
    public static boolean updateConfig(String mod) {
        final String cfgName = getCfgName(mod);
        if (cfgName.equals("all")) {
            DATA.forEach((name, data) -> data.doUpdates(getConfig(name)));
            return doBOP();
        } else if (cfgName.equals("biomesoplenty")) {
            return doBOP();
        }
        final Optional<SettingData> settings = safeGet(DATA, cfgName);
        final Configuration config = getConfig(cfgName);
        settings.ifPresent(s -> s.doUpdates(config));
        return settings.isPresent();
    }

    /** Attempts to edit all of the Biomes O' Plenty biome configs. */
    private static boolean doBOP() {
        if (!BOP_PARENT.exists()) {
            return false;
        }
        for (File preset : getBopFiles()) {
            final JsonObject json = readJson(preset)
                .orElseThrow(() -> runExF("Error parsing BOP preset: {}", preset.getName()));
            final JsonObject generators = getObject(json, "generators")
                .orElseThrow(() -> runExF("Invalid BOP preset: {}. No generators specified.", preset.getName()));
            for (JsonObject ore : getOreGenerators(generators)) {
                ore.set("enable", false);
            }
            final File custom = new File(BOP_PARENT, preset.getName());
            writeJson(json, custom)
                .expectF("Unable to write custom preset: {}", custom);
        }
        return true;
    }

    /** Gets all of the presets used by BOP. */
    private static List<File> getBopFiles() {
        // Get default presets.
        final List<File> defaults = list(safeListFiles(BOP_MAIN));
        defaults.addAll(list(safeListFiles(BOP_VANILLA)));
        // Look for and return custom presets, where possible.
        final List<File> files = new ArrayList<>();
        for (File f : defaults) {
            final File custom = new File(BOP_PARENT, f.getName());
            files.add(custom.exists() ? custom : f);
        }
        return files;
    }

    /** Attempts to retrieve any of the known BOP ore generator settings from the preset. */
    private static List<JsonObject> getOreGenerators(JsonObject generators) {
        final List<JsonObject> ores = new ArrayList<>();
        for (String key : BOP_NAMES) {
            getObject(generators, key).ifPresent(ores::add);
        }
        return ores;
    }

    /** Contains a list of fields to be altered and the values to set them to. */
    private static class SettingData {
        /** A list of booleans to be set. */
        final List<Pair<String, Boolean>> bools = new ArrayList<>();
        /** A list of integers to be set. */
        final List<Pair<String, Integer>> ints = new ArrayList<>();
        /** A list of integer arrays to be set. */
        final List<Pair<String, int[]>> intArrays = new ArrayList<>();

        SettingData set(String field, boolean val) {
            bools.add(new Pair<>(field, val));
            return this;
        }

        SettingData set(String field, int val) {
            ints.add(new Pair<>(field, val));
            return this;
        }

        SettingData set(String field, int... val) {
            intArrays.add(new Pair<>(field, val));
            return this;
        }

        void doUpdates(Configuration config) {
            for (Pair<String, Boolean> val : bools) {
                final String[] split = val.getKey().split("\\|");
                final boolean b = val.getValue();
                config.get(split[0], split[1], b).set(b);
            }
            for (Pair<String, Integer> val : ints) {
                final String[] split = val.getKey().split("\\|");
                final int i = val.getValue();
                config.get(split[0], split[1], i).set(i);
            }
            for (Pair<String, int[]> val : intArrays) {
                final String[] split = val.getKey().split("\\|");
                final int[] ints = val.getValue();
                config.get(split[0], split[1], ints).set(ints);
            }
            config.save();
        }
    }
}