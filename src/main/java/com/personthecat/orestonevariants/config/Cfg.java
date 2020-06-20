package com.personthecat.orestonevariants.config;


import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.util.CommonMethods;
import com.personthecat.orestonevariants.util.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.*;
import net.minecraftforge.common.config.Config.*;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.personthecat.orestonevariants.util.unsafe.ReflectionTools.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

@EventBusSubscriber
@SuppressWarnings("WeakerAccess")
@Config(modid = Main.MODID, category = "")
public class Cfg {

    /** Stores a reference to the actual file containing the data. */
    @Ignore private static final File configFile = new File(getConfigDir(), Main.MODID + ".cfg");

    /** Contains all of the config data loaded from the disk. */
    @Ignore private static final Configuration config = loadConfig(configFile);

    /** Indicates whether the config has been updated. Prevents unnecessary write operations. */
    @Ignore private static AtomicBoolean configChanged = new AtomicBoolean(false);

    @LangKey("cfg.blocks")
    public static BlocksCat blocks;

    @LangKey("cfg.dense")
    public static DenseCat dense;

    @Comment({"  You can use this registry to add as many new ore types as you like using any",
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
            "generate these presets dynamically, but they must be created manually for now."})
    @LangKey("cfg.blockRegistry")
    public static BlockRegistryCat blockRegistry;

    @LangKey("cfg.modSupport")
    public static ModSupport modSupport;

    @LangKey("cfg.world")
    public static WorldCat world;

    private static Configuration loadConfig(File file) {
        final Configuration config = new Configuration(file);
        config.load();
        return config;
    }

    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(Main.MODID)) {
            sync();
        }
    }

    public static synchronized void sync() {
        ConfigManager.sync(Main.MODID, Config.Type.INSTANCE);
        configChanged.set(false);
    }

    public static synchronized void trySync() {
        if (configChanged.get()) {
            sync();
        }
    }

    public static synchronized void configChanged() {
        configChanged.set(true);
    }

    /** Returns whether the input ResourceLocation should be shaded. */
    public static boolean shade(ResourceLocation location) {
        final String name = location.getPath();
        final boolean override = ArrayUtils.contains(BlocksCat.shadeOverrides, name);
        return BlocksCat.shadeOverlays != override;
    }

    /** In the future, this will include other supported mods dynamically. */
    public static boolean modEnabled(String mod) {
        if (mod.equals("minecraft")) {
            return vanillaEnabled();
        }
        return isModLoaded(mod) && ModSupport.enabledMods.get(mod);
    }

    public static boolean modFamiliar(String mod) {
        return ModSupport.enabledMods.containsKey(mod);
    }

    public static boolean vanillaEnabled() {
        return !anyMatches(BlocksCat.disableVanillaWhen, CommonMethods::isModLoaded);
    }

    private static Map<String, Boolean> getModSupport() {
        final Map<String, Boolean> modSupport = new LinkedHashMap<>();

        for (String mod : Reference.SUPPORTED_MODS) {
            if (!mod.equals("basemetals")) {
                final Property prop = config.get("modSupport", mod, true);
                modSupport.put(mod, prop.getBoolean());
            }
        }
        final Property propBaseMetals = config.get("modSupport", "basemetals", true);
        propBaseMetals.setComment(
            "For easiest compatibility with Base Metals, set both using_orespawn and\n"
            + "fallback_orespawn to false in BaseMetals.cfg. Subsequently, disable\n"
            + "OreSpawn itself. This is because both mods when combined will otherwise\n"
            + "spawn twice as many ores as necessary."
        );
        modSupport.put("basemetals", propBaseMetals.getBoolean());
        return modSupport;
    }

    /** Prevents existing groups from being deleted. Adds default groups, if missing. */
    private static Map<String, String[]> handleDynamicGroup(String cat, ArrayTemplate<String>... defaults) {
        final ConfigCategory groups = config.hasCategory(cat)
            ? config.getCategory(cat)
            : new ConfigCategory(cat);
        for (ArrayTemplate<String> value : defaults) {
            Property p = new Property(value.getName(), value.getValues(), Property.Type.STRING);
            groups.putIfAbsent(value.getName(), p);
        }
        return castMap(groups);
    }

    private static Map<String, String[]> castMap(Map<String, Property> map) {
        final Map<String, String[]> cast = new HashMap<>();
        map.forEach((k, v) -> cast.put(k, v.getStringList()));
        return cast;
    }

    public static class BlocksCat {
        @Comment({"Variants will imitate the properties of their background blocks,",
                "such as the ability to fall like sand or sustain leaves."})
        @LangKey("cfg.blocks.misc.bgImitation")
        @RequiresMcRestart
        public static boolean bgImitation = true;

        @Name("enableFurnaceRecipes")
        @LangKey("cfg.blocks.enableFurnaceRecipes")
        @RequiresMcRestart
        public static boolean enableFurnaceRecipes = true;

        @LangKey("cfg.blocks.enableOreDictionary")
        @RequiresMcRestart
        public static boolean enableOreDictionary = true;

        @Comment("Indicates whether to enable shading in the generated block models.")
        @LangKey("cfg.blocks.shadeOverlays")
        public static boolean shadeOverlays = true;

        @Comment({"Any model names listed here will be shaded or not, opposite of the",
                "global setting."})
        @LangKey("cfg.blocks.shadeOverrides")
        public static String[] shadeOverrides = new String[0];

        @Comment({"Whether textures should be rendered with support for a full range",
                "of opacity. You may want to disable this if you're using shaders."})
        @LangKey("cfg.blocks.transparency")
        public static boolean translucentTextures = true;

        @Comment("Whether to attempt generating textures form the current resource packs.")
        @LangKey("cfg.blocks.overlaysFromRP")
        public static boolean overlaysFromRP = true;

        @LangKey("cfg.blocks.advancements")
        public static boolean enableAdvancements = true;

        @Comment({"Vanilla ore variants will not be generated in the presence of any",
                "mods listed here."})
        @LangKey("cfg.blocks.autoVanilla")
        @RequiresMcRestart
        public static String[] disableVanillaWhen = new String[] {"mineralogy", "undergroundbiomes"};

        @Comment("Whether ore variants will drop instead of original counterparts.")
        @LangKey("cfg.blocks.variantsDrop")
        public static boolean variantsDrop = false;

        @Comment("Whether ore variants will drop when using silk touch.")
        @LangKey("cfg.blocks.variantsDropSilkTouch")
        public static boolean variantsSilktouch = true;
    }

    public static class DenseCat {
        @Comment("Adds a dense variant of every ore. Drops 1-3 x each original drop.")
        @LangKey("cfg.dense.enable")
        @RequiresMcRestart
        public static boolean enabled = false;

        @Comment("The 0-1 chance that dense ores will spawn instead of regular variants.")
        @LangKey("cfg.dense.frequency")
        @RangeDouble(min = 0.0, max = 1.0)
        @RequiresWorldRestart
        public static double chance = 0.09;

        @LangKey("cfg.dense.smeltMultiplier")
        @RequiresMcRestart
        public static int smeltMultiplier = 2;

        @LangKey("cfg.dense.dropMultiplier")
        public static int dropMultiplier = 3;
    }

    public static class BlockRegistryCat {
        @LangKey("cfg.registry.values")
        public static String[] values = new String[] {"default default"};

        @Comment("Whether to test the block registry for duplicate combinations.")
        @LangKey("cfg.registry.testForDuplicates")
        public static boolean testForDuplicates = true;

        @Comment({"These categories are dynamic. You may create new lists here in order to conveniently",
                "reference them above."})
        @LangKey("cfg.registry.blockGroups")
        public static Map<String, String[]> blockGroups = handleDynamicGroup("blockRegistry.blockGroups", BlockGroup.DefaultInfo.values());

        @LangKey("cfg.registry.propertyGroups")
        public static Map<String, String[]> propertyGroups = handleDynamicGroup("blockRegistry.propertyGroups", PropertyGroup.DefaultInfo.values());
    }

    public static class ModSupport {
        @Comment("Set any of these to false to disable creation / spawning of new variants.")
        @LangKey("cfg.modSupport.enabledMods")
        public static Map<String, Boolean> enabledMods = getModSupport();
    }

    public static class WorldCat {
        @Comment("Whether to enable all OSV world gen features.")
        @LangKey("cfg.world.generate")
        @RequiresWorldRestart
        public static boolean enabled = true;

        @Comment("Whether ores should spawn according to specific biomes.")
        @LangKey("cfg.world.biomeSpecific")
        @RequiresWorldRestart
        public static boolean biomeSpecific = true;

        @Comment("Whether vanilla ores should spawn.")
        @LangKey("cfg.world.enableVanillaOres")
        @RequiresWorldRestart
        public static boolean enableVanillaOres = false;

        @Comment("Whether vanilla should spawn its own stone types.")
        @LangKey("cfg.world.enableVanillaStone")
        @RequiresWorldRestart
        public static boolean enableVanillaStone = false;

        @Comment("Whether OSV should spawn custom ore variants.")
        @LangKey("cfg.world.enableOSVOres")
        @RequiresWorldRestart
        public static boolean enableOSVOres = true;

        @Comment("Whether OSV should spawn custom stone veins.")
        @LangKey("cfg.world.enableOSVStone")
        @RequiresWorldRestart
        public static boolean enableOSVStone = true;

        @Comment({"Ores will generate in larger veins, more often near other",
                "veins of the same type."})
        @LangKey("cfg.world.largeClusters")
        public static boolean largeClusters = false;

        @Comment({"The probability that any ore will spawn outside of its selected",
                "regions."})
        @LangKey("cfg.world.largeClusterDefaultProbability")
        @RequiresMcRestart
        public static double largeClusterDefaultProbability = 0.01;

        @Comment("The maximum chance for ores to spawn in selected regions.")
        @LangKey("cfg.world.largeClusterMaxProbability")
        @RequiresMcRestart
        public static double largeClusterMaxProbability = 80.0;

        @Comment({"Setting this to true will prevent OSV from spawning variants",
                "already generated by undergroundbiomes."})
        @LangKey("cfg.world.avoidDuplicateUBCVariants")
        @RequiresMcRestart
        public static boolean avoidDuplicateUBCVariants = true;
    }
}