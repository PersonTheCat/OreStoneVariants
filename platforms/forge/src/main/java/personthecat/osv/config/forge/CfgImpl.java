package personthecat.osv.config.forge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import personthecat.catlib.config.CustomModConfig;
import personthecat.catlib.config.DynamicCategoryBuilder;
import personthecat.catlib.config.HjsonFileConfig;
import personthecat.catlib.util.McUtils;
import personthecat.osv.config.DefaultOres;
import personthecat.osv.config.DefaultStones;
import personthecat.osv.preset.data.ModelSettings;
import personthecat.osv.util.Group;
import personthecat.osv.util.Reference;

import java.util.*;

public class CfgImpl {

    private static final String FILENAME = McUtils.getConfigDir() + "/" + Reference.MOD_ID;
    private static final CommentedFileConfig COMMON_CFG = new HjsonFileConfig(FILENAME + "-common.hjson");
    private static final CommentedFileConfig CLIENT_CFG = new HjsonFileConfig(FILENAME + "-client.hjson");

    private static final Builder COMMON = new Builder();
    private static final Builder CLIENT = new Builder();

    private static final Map<String, List<String>> BLOCK_GROUPS =
        DynamicCategoryBuilder.withPath("blockRegistry.blockGroups")
            .withListEntries(Group.toFormattedMap(DefaultStones.LISTED))
            .build(COMMON, COMMON_CFG);

    private static final Map<String, List<String>> PROPERTY_GROUPS =
        DynamicCategoryBuilder.withPath("blockRegistry.propertyGroups")
            .withListEntries(Group.toFormattedMap(DefaultOres.LISTED))
            .build(COMMON, COMMON_CFG);

    private static final Map<String, Boolean> ENABLED_MODS =
        DynamicCategoryBuilder.withPath("modSupport")
            .withBooleanEntries(Reference.SUPPORTED_MODS)
            .build(COMMON, COMMON_CFG);

    public static final ConfigValue<List<String>> BLOCK_ENTRIES = COMMON
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

    public static final BooleanValue GENERATE_RESOURCES = COMMON
        .comment("Whether to regenerate resources if config/osv/resources already",
                "exists. You may consider disabling this feature if you already",
                "have resources and want to speed up your game load time.")
        .define("general.generateResources", true);

    public static final BooleanValue BG_IMITATION = COMMON
        .comment("Variants will imitate the properties of their background blocks,",
                "such as the ability to fall like sand or sustain leaves.")
        .define("blocks.bgImitation", true);

    public static final BooleanValue FURNACE_RECIPES = COMMON
        .define("blocks.enableFurnaceRecipes", true);

    public static final BooleanValue OVERLAYS_FROM_RP = CLIENT
        .comment("Attempts to generate any new ore sprites from the topmost resource",
                "pack. Not an ideal solution for many resource packs.")
        .define("blocks.overlaysFromRP", false);

    public static final BooleanValue SHADE_OVERLAYS = CLIENT
        .comment("Indicates whether to enable shading in generated block models.")
        .define("blocks.shadeOverlays", true);

    public static final BooleanValue SHADED_TEXTURES = CLIENT
        .comment("Whether textures should use variable opacity to push and pull the",
                "background sprite.")
        .define("blocks.enableShadedTextures", true);

    public static final BooleanValue TRANSLUCENT_TEXTURES = CLIENT
        .comment("Whether textures should be rendered with support for a full range",
                "of opacity. You may want to disable this if you're using shaders.")
        .define("blocks.enableTransparency", true);

    public static final DoubleValue MODEL_SCALE = CLIENT
        .comment("How much larger the overlay model is than the background model.",
                "Lower values may look better, but cause z-fighting.")
        .defineInRange("blocks.modelScale", 1.001, 1.0, 2.0);

    public static final BooleanValue OPTIFINE_HACK = CLIENT
        .comment("When this is enabled, the value of modelScale will automatically",
                "be doubled when Optifine is installed.")
        .define("blocks.optifineHack", true);

//    public static EnumValue<ModelSettings.Type> MODEL_TYPE = CLIENT
//        .comment("The default type of model to generate for all ores.")
//        .defineEnum("blocks.modelType", ModelSettings.Type.SINGLE);

    public static final BooleanValue AUTO_REFRESH = CLIENT
        .comment("Whether to automatically reload resources after enabling the",
                "generated resource pack. This may result in fewer missing",
                "textures in rare cases.")
        .define("blocks.autoRefresh", false);

    public static final BooleanValue VARIANTS_DROP = COMMON
        .comment("Whether ore variants will drop instead of original counterparts.")
        .define("blocks.variantsDrop", false);

    public static final BooleanValue VARIANTS_SILK_TOUCH = COMMON
        .comment("Whether ore variants will drop when using silk touch.")
        .define("blocks.variantsDropWithSilkTouch", true);

    public static final BooleanValue MAP_INFESTED_VARIANTS = COMMON
        .comment("Whether to allow silverfish to enter into any infested variants.")
        .define("blocks.mapInfestedVariants", true);

    public static final BooleanValue COPY_BLOCK_TAGS = COMMON
        .comment("Whether to copy any block tags at all for ore variants.")
        .define("tags.copyBlockTags", true);

    public static final BooleanValue COPY_ITEM_TAGS = COMMON
        .comment("Whether to copy any item tags at all for ore variants.")
        .define("tags.copyItemTags", true);

    public static final BooleanValue COPY_BG_TAGS = COMMON
        .comment("Whether tags should be copied from background blocks.")
        .define("tags.copyBgTags", false);

    public static final BooleanValue COPY_FG_TAGS = COMMON
        .comment("Whether tags should be copied from foreground blocks.")
        .define("tags.copyFgTags", true);

    public static final BooleanValue COPY_DENSE_TAGS = COMMON
        .comment("Whether regular tags should be copied for dense variant blocks and items.")
        .define("tags.copyDenseTags", true);

    public static final BooleanValue DENSE_ORES = COMMON
        .comment("Adds a dense variant of every ore. Drops 1-3 x each original drop.")
        .define("denseOres.enabled", false);

    public static final DoubleValue DENSE_CHANCE = COMMON
        .comment("The 0-1 chance that dense ores will spawn instead of regular variants.")
        .defineInRange("denseOres.chance", 0.09, 0.0, 1.0);

    public static final IntValue DENSE_SMELT_MULTIPLIER = COMMON
        .comment("The number of items to yield when smelting dense ores.")
        .defineInRange("denseOres.smeltingMultiplier", 2, 0, Integer.MAX_VALUE);

    public static final IntValue DENSE_DROP_MULTIPLIER = COMMON
        .comment("The maximum multiple of items to drop when mining dense ores.")
        .defineInRange("denseOres.dropMultiplier", 3, 1, Integer.MAX_VALUE);

    public static final IntValue DENSE_DROP_MULTIPLIER_MIN = COMMON
        .comment("The minimum multiple of items to drop when mining dense ores.")
        .defineInRange("denseOres.dropMultiplierMin", 1, 1, Integer.MAX_VALUE);

    public static final BooleanValue RANDOM_DROP_COUNT = COMMON
        .comment("If true, dense variants drop a random number between 1 and multiplier.")
        .define("denseOres.randomDropCount", true);

    public static final BooleanValue BIOME_SPECIFIC = COMMON
        .comment("Whether ores should spawn according to specific biomes vs. anywhere.")
        .define("worldGen.biomeSpecific", true);

    public static final BooleanValue ENABLE_VANILLA_ORES = COMMON
        .comment("Whether vanilla spawning of ores should be blocked.")
        .define("worldGen.enableVanillaOres", false);

    public static final BooleanValue ENABLE_VANILLA_STONE = COMMON
        .comment("Whether vanilla spawning of stone variants should be blocked.")
        .define("worldGen.enableVanillaStone", false);

    public static final BooleanValue ENABLE_OSV_ORES = COMMON
        .comment("Whether to spawn custom ore variants.")
        .define("worldGen.enableOSVOres", true);

    public static final BooleanValue ENABLE_OSV_STONE = COMMON
        .comment("Whether to spawn stone types with custom variables.")
        .define("worldGen.enableOSVStone", true);

    public static void register() {
        final ModContainer ctx = ModLoadingContext.get().getActiveContainer();
        ctx.addConfig(new CustomModConfig(ModConfig.Type.COMMON, COMMON.build(), ctx, COMMON_CFG));
        ctx.addConfig(new CustomModConfig(ModConfig.Type.CLIENT, CLIENT.build(), ctx, CLIENT_CFG));
    }

    public static List<String> blockEntries() {
        return BLOCK_ENTRIES.get();
    }

    public static Map<String, List<String>> propertyGroups() {
        return PROPERTY_GROUPS;
    }

    public static Map<String, List<String>> blockGroups() {
        return BLOCK_GROUPS;
    }

    public static Map<String, Boolean> enabledMods() {
        return ENABLED_MODS;
    }

    public static double getModelScale() {
        return MODEL_SCALE.get();
    }

    public static boolean useOptifineHack() {
        return OPTIFINE_HACK.get();
    }

    public static ModelSettings.Type modelType() {
//        return MODEL_TYPE.get();
        return ModelSettings.Type.SINGLE;
    }

    public static boolean generateResources() {
        return GENERATE_RESOURCES.get();
    }

    public static boolean bgImitation() {
        return BG_IMITATION.get();
    }

    public static boolean furnaceRecipes() {
        return FURNACE_RECIPES.get();
    }

    public static boolean overlaysFromRp() {
        return OVERLAYS_FROM_RP.get();
    }

    public static boolean shadeOverlays() {
        return SHADE_OVERLAYS.get();
    }

    public static boolean shadedTextures() {
        return SHADED_TEXTURES.get();
    }

    public static boolean translucentTextures() {
        return TRANSLUCENT_TEXTURES.get();
    }

    public static boolean autoRefresh() {
        return AUTO_REFRESH.get();
    }

    public static boolean variantsDrop() {
        return VARIANTS_DROP.get();
    }

    public static boolean variantsSilkTouch() {
        return VARIANTS_SILK_TOUCH.get();
    }

    public static boolean mapInfestedVariants() {
        return MAP_INFESTED_VARIANTS.get();
    }

    public static boolean copyBlockTags() {
        return COPY_BLOCK_TAGS.get();
    }

    public static boolean copyItemTags() {
        return COPY_ITEM_TAGS.get();
    }

    public static boolean copyBgTags() {
        return COPY_BG_TAGS.get();
    }

    public static boolean copyFgTags() {
        return COPY_FG_TAGS.get();
    }

    public static boolean copyDenseTags() {
        return COPY_DENSE_TAGS.get();
    }

    public static boolean denseOres() {
        return DENSE_ORES.get();
    }

    public static double denseChance() {
        return DENSE_CHANCE.get();
    }

    public static int denseSmeltMultiplier() {
        return DENSE_SMELT_MULTIPLIER.get();
    }

    public static int denseDropMultiplier() {
        return DENSE_DROP_MULTIPLIER.get();
    }

    public static int denseDropMultiplierMin() {
        return DENSE_DROP_MULTIPLIER_MIN.get();
    }

    public static boolean randomDropCount() {
        return RANDOM_DROP_COUNT.get();
    }

    public static boolean biomeSpecific() {
        return BIOME_SPECIFIC.get();
    }

    public static boolean enableVanillaOres() {
        return ENABLE_VANILLA_ORES.get();
    }

    public static boolean enableVanillaStone() {
        return ENABLE_VANILLA_STONE.get();
    }

    public static boolean enableOSVOres() {
        return ENABLE_OSV_ORES.get();
    }

    public static boolean enableOSVStone() {
        return ENABLE_OSV_STONE.get();
    }
}
