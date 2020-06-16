package com.personthecat.orestonevariants.config;

import com.google.common.collect.ImmutableMap;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import com.personthecat.orestonevariants.util.unsafe.Result;
import javafx.util.Pair;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class ModConfigSupport {

    /** A list of fields to disable for each mod. */
    private static final Map<String, SettingData> DATA = new ImmutableMap.Builder<String, SettingData>()
        .put("iceandfire", new SettingData()
            .set("generation|generateSapphireOre", false)
            .set("generation|generateSilverOre", false))
        .put("simpleores", new SettingData()
            .set("Ores.Copper Ore|enableOreGen", false)
            .set("Ores.Tin Ore|enableOreGen", false)
            .set("Ores.Mythril Ore|enableOreGen", false)
            .set("Ores.Adamantium Ore|enableOreGen", false)
            .set("Ores.Onyx Ore|enableOreGen", false))
        .put("basemetals", new SettingData()
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
        .put("osv", new SettingData()
            .set("world|enableOSVOres", false)
            .set("world|enableOSVStone", false))
        .build();

    /** A method used to obtain Forge's auto-loaded configuration files. */
    private static final Method getConfiguration = ReflectionTools.getMethod(
        ConfigManager.class, "getConfiguration", null, String.class, String.class
    );

    /** Attempts to retrieve an auto-loaded configuration from ConfigManager. Preserves comments. */
    private static Optional<Configuration> getConfig(String mod) {
        return Result.nullable(() -> (Configuration) getConfiguration.invoke(ConfigManager.class, mod, null))
            .expect("Build error: Error invoking ConfigManager#getConfiguration.");
    }

    /** Runs updates for the input mod name. Use `all` to run all updates. */
    public static boolean updateConfig(String mod) {
        mod = mod.toLowerCase();
        if (mod.equals("all")) {
            DATA.forEach((name, data) -> getConfig(name).ifPresent(data::doUpdates));
            return true;
        }
        final Optional<SettingData> settings = safeGet(DATA, mod);
        final Optional<Configuration> config = getConfig(mod);
        settings.ifPresent(s ->
            config.ifPresent(s::doUpdates)
        );
        return settings.isPresent() && config.isPresent();
    }

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