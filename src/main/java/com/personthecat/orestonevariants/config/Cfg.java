package com.personthecat.orestonevariants.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cfg {
    /** The builder used for the common config file. */
    private static final Builder common = new Builder();
    /** The builder used for the client config file. */
    private static final Builder client = new Builder();
    /** The name of the primary config file. */
    private static final String fileName = FMLPaths.CONFIGDIR.get() + "/ore_stone_variants";
    /** The actual config used for handling common values. */
    private static final HjsonFileConfig commonCfg = new HjsonFileConfig(fileName + "-common.hjson");
    /** The actual config used for handling client values. */
    private static final HjsonFileConfig clientCfg = new HjsonFileConfig(fileName + "-client.hjson");

    /** Produces the finalized version of this c */
    public static void register(final ModContainer ctx) {
        handleConfigSpec(ctx, common, commonCfg, ModConfig.Type.COMMON);
        handleConfigSpec(ctx, client, clientCfg, ModConfig.Type.CLIENT);
    }

    /** Generates a new ForgeConfigSpec and registers it to a config and with Forge. */
    private static void handleConfigSpec(ModContainer ctx, Builder builder, HjsonFileConfig cfg, ModConfig.Type type) {
        final ForgeConfigSpec spec = builder.build();
        ctx.addConfig(new CustomModConfig(type, spec, ctx, cfg));
    }

    /** Sets the current category for all config files. */
    private static void push(String name) {
        common.push(name);
        client.push(name);
    }

    /** Pops the current category up one level; */
    private static void pop() {
        common.pop();
        client.pop();
    }

    /* Init fields in the Blocks category. */
    static { push("blocks"); }

    public static final BooleanValue bgImitation = common
        .comment("Variants will imitate the properties of their background blocks,",
                 "such as the ability to fall like sand or sustain leaves.")
        .define("backgroundBlockImitation", true);

    public static final BooleanValue furnaceRecipes = common
        .define("enableFurnaceRecipes", true);

    public static final BooleanValue oreDictionary = common
        .define("enableOreDictionary", true);

    public static final BooleanValue overlaysFromRp = client
        .comment("Attempts to generate any new ore sprites from the topmost resource",
                 "pack. Not an ideal solution for many resource packs.")
        .define("generateOverlaysFromResourcePack", false);

    public static final BooleanValue shadeOverlays = client
        .comment("Indicates whether to enable shading in generated block models.")
        .define("shadeOverlays", true);

    public static final ConfigValue<List<String>> shadeOverrides = client
        .comment("Any model names listed here will shaded or not, opposite of the",
                 "global setting.")
        .define("shadeOverrides", Collections.emptyList(), Objects::nonNull);

    public static final BooleanValue shadedTextures = client
        .comment("Whether textures should use variable opacity to push and pull the",
                 "background sprite.")
        .define("enableShadedTextures", true);

    public static final ConfigValue<List<String>> disableVanilla = common
        .comment("Vanilla ore variants will not be generated in the presence of any",
                 "mods listed here.")
        .define("disableVanillaVariantsIf", Collections.emptyList(), Objects::nonNull);

    public static final BooleanValue variantsDrop = common
        .define("variantsDrop", false);

    public static final BooleanValue variantsSilkTouch = common
        .define("variantsDropWithSilkTouch", true);

    /* Init fields in the Dense Ores category. */
    static { pop(); push("denseOres"); }

    public static final BooleanValue denseOres = common
        .comment("Adds a dense variant of every ore. Drops 1-3 x each original drop.")
        .define("enable", false);

    public static final DoubleValue denseChance = common
        .comment("The 0-1 chance that dense ores will spawn instead of regular variants.")
        .defineInRange("chance", 0.09, 0.0, 1.0);

    public static final IntValue denseMultiplier = common
        .comment("The number of items to yield when smelting dense ores.")
        .defineInRange("smeltingMultiplier", 2, 0, Integer.MAX_VALUE);

    /* Init fields in the blockRegistry category. */
    static { pop(); push("blockRegistry"); }

    public static final ConfigValue<List<String>> blockEntries = common
        .define("values", Collections.singletonList("default default"), Objects::nonNull);

    public static final BooleanValue testForDuplicates = common
        .define("testForDuplicates", true);

    /* Init fields in blockRegistry.blockGroups. */
    static { push("blockGroups"); }

    /** To-do: convert BlockGroup#GROUP_INFO to enum and use values. */
    public static final ConfigValue<List<String>> mcBlocks = common
        .define("minecraft", Arrays.asList("stone", "andesite", "diorite", "granite"), Objects::nonNull);

    /* Init fields in blockRegistry.oreGroups. */
    static { pop(); push("propertyGroups"); }

    /** To-do: convert PropertyGroup#GROUP_INFO to enum and use values. */
    public static final ConfigValue<List<String>> mcOres = common
        .define("minecraft", Arrays.asList("coal_ore", "diamond_ore", "emerald_ore", "gold_ore", "iron_ore", "lapis_ore", "redstone_ore"), Objects::nonNull);

    /* Init fields in modSupport. */
    static { pop(); pop(); push("modSupport"); }

    /** To-do: dynamic fields again? */
    public static final BooleanValue mcEnabled = common
        .define("minecraft", true);

    /* Init fields in worldGen. */
    static { pop(); push("worldGen"); }

    public static final BooleanValue enableVanillaOres = common
        .define("enableVanillaOres", false);

    public static final BooleanValue enableVanillaStone = common
        .define("enableVanillaStone", true);

    public static final BooleanValue enableOSVOres = common
        .define("enableOSVOres", true);

    public static final BooleanValue enableOSVStone = common
        .define("enableOSVStone", false);
}