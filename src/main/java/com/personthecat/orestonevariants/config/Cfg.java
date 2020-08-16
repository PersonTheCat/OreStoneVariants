package com.personthecat.orestonevariants.config;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.anyMatches;

public class Cfg {
    /** The builder used for the common config file. */
    private static final Builder common = new Builder();
    /** The builder used for the client config file. */
    private static final Builder client = new Builder();
    /** The name of the primary config file. */
    private static final String fileName = FMLPaths.CONFIGDIR.get() + "/" + Main.MODID;
    /** The actual config used for handling common values. */
    private static final HjsonFileConfig commonCfg = new HjsonFileConfig(fileName + "-common.hjson");
    /** The actual config used for handling client values. */
    private static final HjsonFileConfig clientCfg = new HjsonFileConfig(fileName + "-client.hjson");

    /** Produces the finalized version of this c */
    public static void register(final ModContainer ctx) {
        handleConfigSpec(ctx, common, commonCfg, ModConfig.Type.COMMON);
        handleConfigSpec(ctx, client, clientCfg, ModConfig.Type.CLIENT);
    }

    /** Returns whether the input ResourceLocation should be shaded. */
    public static boolean shade(ResourceLocation location) {
        final String name = location.getPath();
        final boolean override = shadeOverrides.get().contains(name);
        return shadeOverlays.get() != override;
    }

    /** In the future, this will include other supported mods dynamically. */
    public static boolean modEnabled(String mod) {
        if (mod.equals("minecraft")) {
            return vanillaEnabled();
        }
        return ModList.get().isLoaded(mod);
    }

    public static boolean vanillaEnabled() {
        return mcEnabled.get() && !anyMatches(disableVanillaWhen.get(), entry -> ModList.get().isLoaded(entry));
    }

    /** Generates a new ForgeConfigSpec and registers it to a config and with Forge. */
    private static void handleConfigSpec(ModContainer ctx, Builder builder, HjsonFileConfig cfg, ModConfig.Type type) {
        final ForgeConfigSpec spec = builder.build();
        ctx.addConfig(new CustomModConfig(type, spec, ctx, cfg));
    }

    /** Prevents existing groups from being deleted. Adds default groups, if missing. */
    private static Map<String, List<String>> handleDynamicGroup(String path, ArrayTemplate<String>[] defaults) {
        final Map<String, Object> groups = commonCfg.contains(path)
            ? ((HjsonFileConfig) commonCfg.get(path)).valueMap()
            : new HashMap<>();
        for (ArrayTemplate<String> value : defaults) {
            groups.putIfAbsent(value.getName(), value.getValues());
        }
        groups.forEach(common::define);
        return castMap(groups);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> castMap(Map<String, Object> map) {
        final Map<String, List<String>> cast = new HashMap<>();
        map.forEach((k, v) -> cast.put(k, (List<String>) v));
        return cast;
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
    static { push("blocks");  }

    public static final BooleanValue bgImitation = common
        .comment("Variants will imitate the properties of their background blocks,",
                "such as the ability to fall like sand or sustain leaves.")
        .define("bgImitation", true);

    public static final BooleanValue furnaceRecipes = common
        .define("enableFurnaceRecipes", true);

    public static final BooleanValue overlaysFromRp = client
        .comment("Attempts to generate any new ore sprites from the topmost resource",
                 "pack. Not an ideal solution for many resource packs.")
        .define("overlaysFromRP", false);

    public static final BooleanValue shadeOverlays = client
        .comment("Indicates whether to enable shading in generated block models.")
        .define("shadeOverlays", true);

    public static final ConfigValue<List<String>> shadeOverrides = client
        .comment("Any model names listed here will be shaded or not, opposite of the",
                 "global setting.")
        .define("shadeOverrides", Collections.emptyList(), Objects::nonNull);

    public static final BooleanValue shadedTextures = client
        .comment("Whether textures should use variable opacity to push and pull the",
                 "background sprite.")
        .define("enableShadedTextures", true);

    public static final BooleanValue translucentTextures = client
        .comment("Whether textures should be rendered with support for a full range",
                 "of opacity. You may want to disable this if you're using shaders.")
        .define("enableTransparency", true);

    public static final ConfigValue<List<String>> disableVanillaWhen = common
        .comment("Vanilla ore variants will not be generated in the presence of any",
                 "mods listed here.")
        .define("disableVanillaVariantsIf", Collections.emptyList(), Objects::nonNull);

    public static final BooleanValue variantsDrop = common
        .comment("Whether ore variants will drop instead of original counterparts.")
        .define("variantsDrop", false);

    public static final BooleanValue variantsSilkTouch = common
        .comment("Whether ore variants will drop when using silk touch. WIP.")
        .define("variantsDropWithSilkTouch", true);

    /* Init fields in the Dense Ores category. */
    static { pop(); push("denseOres"); }

    public static final BooleanValue denseOres = common
        .comment("Adds a dense variant of every ore. Drops 1-3 x each original drop.")
        .define("enabled", false);

    public static final DoubleValue denseChance = common
        .comment("The 0-1 chance that dense ores will spawn instead of regular variants.")
        .defineInRange("chance", 0.09, 0.0, 1.0);

    public static final IntValue denseSmeltMultiplier = common
        .comment("The number of items to yield when smelting dense ores.")
        .defineInRange("smeltingMultiplier", 2, 0, Integer.MAX_VALUE);

    public static final IntValue denseDropMultiplier = common
        .comment("The maximum multiple of items to drop when mining dense ores.")
        .defineInRange("dropMultiplier", 3, 1, Integer.MAX_VALUE);

    /* Init fields in the blockRegistry category. */
    static { pop(); push("blockRegistry"); }

    public static final ConfigValue<List<String>> blockEntries = common
        .comment("  You can use this registry to add as many new ore types as you like using any",
                 "background block at all. Block models, textures, world generation, and other",
                 "properties are handled dynamically. These blocks will only spawn in whichever",
                 "block or blockstate is listed as the background block. The ores retain a mixture",
                 "of their original properties + those of their background.",
                 "  The basic syntax is like this: \"<ore> <background>.\" For example, to spawn",
                 "coal ore inside of sand, you would type \"coal_ore sand.\" Alternatively, you",
                 "can list out any number of ore blocks inside of a new or existing PropertyGroup",
                 "below and use its name in the place of \"<ore>.\" Likewise, \"<background>\" can",
                 "be replaced with the name of a BlockGroup, as registered below.",
                 "  Some block and property groups have special names. Those are \"all\" and",
                 "\"default.\" Using \"all\" in the place of either will gather all of the respective",
                 "entries from below and add them to the list dynamically. Using \"default\" in the",
                 "place of either will gather all of the entries that are listed *by default*.",
                 "  Any block listed in the place of \"<ore>\" must be registered using a preset",
                 "located at \"./config/osv/ores/.\" You can add new presets there or modify existing",
                 "presets to customize their properties. In the future, it will be possible to",
                 "generate these presets dynamically, but they must be created manually for now.")
        .define("values", Collections.singletonList("default default"), Objects::nonNull);

    public static final BooleanValue testForDuplicates = common
        .comment("Whether to test the block registry for duplicate combinations.")
        .define("testForDuplicates", true);

    /* Init fields in blockRegistry.blockGroups. */
    static {
        common.comment("The arrays listed in these categories are dynamic. You may create",
                       "new lists here in order to conveniently reference them above.");
        push("blockGroups");
    }

    public static final Map<String, List<String>> blockGroups = handleBlockGroups();

    private static Map<String, List<String>> handleBlockGroups() {
        return handleDynamicGroup("blockRegistry.blockGroups", BlockGroup.DefaultInfo.values());
    }

    /* Init fields in blockRegistry.oreGroups. */
    static { pop(); push("propertyGroups"); }

    public static final Map<String, List<String>> propertyGroups = handlePropertyGroups();

    private static Map<String, List<String>> handlePropertyGroups() {
        return handleDynamicGroup("blockRegistry.propertyGroups", PropertyGroup.DefaultInfo.values());
    }

    /* Init fields in modSupport. */
    static { pop(); pop(); push("modSupport"); }

    /** To-do: dynamic fields again? */
    public static final BooleanValue mcEnabled = common
        .define("minecraft", true);

    /* Init fields in worldGen. */
    static { pop(); push("worldGen"); }

    public static final BooleanValue biomeSpecific = common
        .comment("Whether ores should spawn according to specific biomes vs. anywhere.")
        .define("biomeSpecific", true);

    public static final BooleanValue enableVanillaOres = common
        .comment("Whether vanilla spawning of ores should be blocked.")
        .define("enableVanillaOres", false);

    public static final BooleanValue enableVanillaStone = common
        .comment("Whether vanilla spawning of stone variants should be blocked.")
        .define("enableVanillaStone", false);

    public static final BooleanValue enableOSVOres = common
        .comment("Whether to spawn custom ore variants.")
        .define("enableOSVOres", true);

    public static final BooleanValue enableOSVStone = common
        .comment("Whether to spawn stone types with custom variables.")
        .define("enableOSVStone", true);
}