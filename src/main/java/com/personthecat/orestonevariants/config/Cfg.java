package com.personthecat.orestonevariants.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Cfg {
    /** The builder used for initializing this class' fields. */
    private static final Builder builder = new Builder();
    /** The name of the primary config file. */
    private static final String fileName = "/ore_stone_variants-common.hjson";
    /** The directory containing the config file.s */
    private static final Path cfgDir = FMLPaths.CONFIGDIR.get();
    /** The actual config used for storing and updating raw values. */
    private static final HjsonFileConfig cfg = new HjsonFileConfig(cfgDir + fileName);

    /** Produces the finalized version of this c */
    public static void register(final ModContainer ctx) {
        final ForgeConfigSpec spec = builder.build();
        spec.setConfig(cfg);
        ctx.addConfig(new ModConfig(ModConfig.Type.COMMON, spec, ctx));
    }

    /* Init fields in the "cat" category. */
    static { builder.push("cat1"); }

    public static final IntValue sample = builder
        .comment("Sample value comment.")
        .defineInRange("sample", 44, 0, 100);

    public static final BooleanValue overlaysFromRp = builder
        .define("overlaysFromRP", false);

    public static final BooleanValue bgImitation = builder
        .define("bgBlockImitation", true);

    public static final BooleanValue denseOres = builder
        .define("denseOres", false);

    static { builder.push("cat2"); }

    public static final ConfigValue<List<String>> test1 = builder
        .define("testArray", Arrays.asList("entry1", "entry2", "test, test"));

    public static final ConfigValue<String> test2 = builder
        .define("testStringArray", "entry1\nentry2");
}