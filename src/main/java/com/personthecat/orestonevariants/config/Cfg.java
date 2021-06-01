package com.personthecat.orestonevariants.config;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.util.CommonMethods;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Reference;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

@Log4j2
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

    /** A list of enabled ore properties by name at startup.  */
    private static final Lazy<Set<String>> PROPERTIES = new Lazy<>(Cfg::getOreProperties);

    /** All of the enabled property groups by name at startup. */
    private static final Lazy<Set<String>> GROUPS = new Lazy<>(Cfg::getPropertyGroups);

    /** Determines whether to defer ores by one stage based on current mods. */
    private static final Lazy<Boolean> DEFER_ORES = new Lazy<>(Cfg::checkDeferOres);

    /** Produces the finalized version of this c */
    public static void register(final ModContainer ctx) {
        handleConfigSpec(ctx, common, commonCfg, ModConfig.Type.COMMON);
        handleConfigSpec(ctx, client, clientCfg, ModConfig.Type.CLIENT);
    }

    /** Retrieves the file storing the current common configuration. */
    public static File getCommon() {
        return commonCfg.getFile();
    }

    /** Provides a way to check whether a property type is enabled without loading it. */
    public static boolean oreEnabled(String name) {
        // These entries need to be manually traversed, as the OreProperties have not
        // yet loaded and cannot be accessed at this time.
        return PROPERTIES.get().contains(name);
    }

    /** Indicates whether the supplied group has been added to the block list. */
    public static boolean groupListed(String name) {
        // Again, what I need is a way to access these groups *before* they are created.
        return GROUPS.get().contains(name);
    }

    /** In the future, this will include other supported mods dynamically. */
    public static boolean modEnabled(String mod) {
        if (mod.equals("minecraft")) {
            return vanillaEnabled();
        }
        return isModLoaded(mod) && enabledMods.get(mod).get();
    }

    /** Determines whether this string is a supported mod name. */
    public static boolean modFamiliar(String mod) {
        return enabledMods.containsKey(mod);
    }

    /** Determines whether vanilla ores have been automtically disabled. */
    public static boolean vanillaEnabled() {
        return !anyMatches(disableVanillaWhen.get(), CommonMethods::isModLoaded);
    }

    /** Indicates whether ores should be moved back one stage. */
    public static boolean deferOreGeneration() {
        return DEFER_ORES.get();
    }

    public static void forEachEntry(BiConsumer<OreProperties, ResourceLocation> fn) {
        for (BlockEntry entry : LazyRegistries.BLOCK_ENTRIES) {
            for (OreProperties props : entry.properties.properties) {
                for (ResourceLocation id : entry.blocks.blocks) {
                    fn.accept(props, id);
                }
            }
        }
    }

    public static void forEachVariant(BiConsumer<OreProperties, BlockState> fn) {
        for (BlockEntry entry : LazyRegistries.BLOCK_ENTRIES) {
            for (ResourceLocation id : entry.blocks.blocks) {
                final Block block = ForgeRegistries.BLOCKS.getValue(id);
                if (block == null) {
                    if (Cfg.ignoreInvalidEntries.get()) {
                        log.error("Skipping invalid block entry due to invalid registry ID: {}", id);
                        continue;
                    } else {
                        throw runExF("There is no block named \"{}.\" Fix your block group.", id);
                    }
                }
                for (OreProperties props : entry.properties.properties) {
                    fn.accept(props, block.getDefaultState());
                }
            }
        }
    }

    // This was a band-aid fix to avoid unknown block errors with BaseMetals.
    // Yes, I do hate it very, very much.
    /** Generates an expanded list of enabled ore properties at startup. */
    private static Set<String> getOreProperties() {
        final Set<String> properties = new HashSet<>();
        for (String group : GROUPS.get()) {
            final Optional<List<String>> found = safeGet(propertyGroups, group);
            // If the key is found, this is a group.
            found.ifPresent(properties::addAll);
            // else, this is just a property.
            if (!found.isPresent()) {
                properties.add(group);
            }
        }
        return properties;
    }

    /** Generates a list of enabled (listed) property groups at startup. */
    private static Set<String> getPropertyGroups() {
        final Set<String> listed = blockEntries.get().stream()
            .map(entry -> BlockEntry.split(entry)[0])
            .collect(Collectors.toSet());
        // Add implied groups.
        if (listed.contains("all")) {
            listed.addAll(propertyGroups.keySet());
        } else if (listed.contains("default")) {
            propertyGroups.keySet().stream()
                .filter(name -> modFamiliar(name) && modEnabled(name))
                .forEach(listed::add);
        }
        return listed;
    }

    /** Checks to see if any mod on the deferred ores list is loaded. */
    private static boolean checkDeferOres() {
        return deferOresWhen.get().stream().anyMatch(CommonMethods::isModLoaded);
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

    public static final BooleanValue enableAdvancements = common
        .comment("Whether to inject custom variants into existing advancements.")
        .define("enableAdvancements", true);

    public static final BooleanValue shadeOverlays = client
        .comment("Indicates whether to enable shading in generated block models.")
        .define("shadeOverlays", true);

    public static final BooleanValue shadedTextures = client
        .comment("Whether textures should use variable opacity to push and pull the",
                 "background sprite.")
        .define("enableShadedTextures", true);

    public static final BooleanValue translucentTextures = client
        .comment("Whether textures should be rendered with support for a full range",
                 "of opacity. You may want to disable this if you're using shaders.")
        .define("enableTransparency", true);

    public static final DoubleValue modelScale = client
        .comment("How much larger the overlay model is than the background model.",
                "Lower values may look better, but cause z-fighting.")
        .defineInRange("modelScale", 1.001, 1.0, 2.0);

    public static final BooleanValue autoRefresh = client
        .comment("Whether to automatically reload resources after enabling the",
                 "generated resource pack. This may result in fewer missing",
                 "textures in rare cases.")
        .define("autoRefresh", false);

    public static final ConfigValue<List<String>> disableVanillaWhen = common
        .comment("Vanilla ore variants will not be generated in the presence of any",
                 "mods listed here.")
        .define("disableVanillaVariantsIf", Collections.emptyList(), Objects::nonNull);

    public static final BooleanValue variantsDrop = common
        .comment("Whether ore variants will drop instead of original counterparts.")
        .define("variantsDrop", false);

    public static final BooleanValue variantsSilkTouch = common
        .comment("Whether ore variants will drop when using silk touch.")
        .define("variantsDropWithSilkTouch", true);

    public static final BooleanValue mapInfestedVariants = common
        .comment("Whether to allow silverfish to enter into any infested variants.")
        .define("mapInfestedVariants", true);

    public static final BooleanValue ignoreInvalidPresets = common
        .comment("Whether to skip over any invalid presets and simply not load them.")
        .define("ignoreInvalidPresets", false);

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

    public static final IntValue denseDropMultiplierMin = common
        .comment("The minimum multiple of items to drop when mining dense ores.")
        .defineInRange("dropMultiplierMin", 1, 1, Integer.MAX_VALUE);

    public static final BooleanValue randomDropCount = common
        .comment("If true, dense variants drop a random number between 1 and multiplier.")
        .define("randomDropCount", true);

    /* Init fields in the blockRegistry category. */
    static { pop(); push("blockRegistry"); }

    public static final ConfigValue<List<String>> blockEntries = common
        .comment("  You can use this registry to add as many new ore types as you like using any",
                 "background block at all. Block models, textures, world generation, and other",
                 "properties are handled dynamically. These blocks will only spawn in whichever",
                 "block or block state is listed as the background block. The ores retain a mixture",
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
        .define("values", Collections.singletonList("all all"), Objects::nonNull);

    public static final BooleanValue testForDuplicates = common
        .comment("Whether to test the block registry for duplicate combinations.")
        .define("testForDuplicates", true);

    public static final BooleanValue ignoreInvalidEntries = common
        .comment("Whether to skip over and not load an entries that are simply invalid.",
                 "Please consider this carefully. If you encounter any issues in game",
                 "such as missing blocks and other unexpected behaviors, it may be because",
                 "you have this value enabled.")
        .define("ignoreInvalidEntries", false);

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

    public static final Map<String, BooleanValue> enabledMods = getModSupport();

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

    public static final ConfigValue<List<String>> deferOresWhen = common
        .comment("Will automatically defer ore generation by one stage",
                 "in the presence of any of these mods. This is needed",
                 "to fully support Quark at the moment.")
        .define("deferOresWhen", Collections.singletonList("quark"));

    private static Map<String, BooleanValue> getModSupport() {
        final Map<String, BooleanValue> modSupport = new LinkedHashMap<>();
        Reference.SUPPORTED_MODS.forEach(mod -> modSupport.put(mod, common.define(mod, true)));
        return modSupport;
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
}