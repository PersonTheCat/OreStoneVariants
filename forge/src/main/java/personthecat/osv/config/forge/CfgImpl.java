package personthecat.osv.config.forge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import personthecat.catlib.config.CustomModConfig;
import personthecat.catlib.config.DynamicCategoryBuilder;
import personthecat.catlib.config.XjsFileConfig;
import personthecat.catlib.data.Lazy;
import personthecat.osv.config.*;
import personthecat.osv.preset.data.ModelSettings;
import personthecat.osv.preset.reader.ComponentReader;
import personthecat.osv.util.Group;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.io.File;
import java.util.*;

public class CfgImpl {

    private static final CommentedFileConfig COMMON_CFG = readConfig(false);
    private static final CommentedFileConfig CLIENT_CFG = readConfig(true);

    private static final Builder COMMON = new Builder();
    private static final Builder CLIENT = new Builder();

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
        .define("blockRegistry.values", Collections.singletonList("all all"), Objects::nonNull);

    private static final Map<String, List<String>> BLOCK_GROUPS =
        DynamicCategoryBuilder.withPath("blockRegistry.blockGroups")
            .withListEntries(Group.toIdMap(DefaultStones.LISTED))
            .build(COMMON, COMMON_CFG);

    private static final Map<String, List<String>> PROPERTY_GROUPS =
        DynamicCategoryBuilder.withPath("blockRegistry.propertyGroups")
            .withListEntries(Group.toIdMap(DefaultOres.LISTED))
            .build(COMMON, COMMON_CFG);

    private static final Map<String, Boolean> ENABLED_MODS =
        DynamicCategoryBuilder.withPath("modSupport")
            .withBooleanEntries(Reference.SUPPORTED_MODS)
            .withDefaultValue(true)
            .build(COMMON, COMMON_CFG);

    public static final BooleanValue CHECK_FOR_DUPLICATES = COMMON
        .comment("Whether to test the block registry for duplicate entries.")
        .define("blockRegistry.checkForDuplicates", true);

    public static final EnumValue<PresetUpdatePreference> UPDATE_PRESETS = COMMON
        .comment("Whether to run transformations on the ore presets for backwards",
                "compatibility.")
        .defineEnum("general.updatePresets", PresetUpdatePreference.MOD_UPDATED);

    public static final BooleanValue FORCE_COMPATIBILITY_MODE = COMMON
        .comment("Whether to forcibly disable dynamic block imitation features to",
                 "provide better compatibility with some platforms. Note that this",
                 "feature should get enabled automatically, if needed.")
        .define("general.forceCompatibilityMode", false);

    public static final BooleanValue BG_IMITATION = COMMON
        .comment("Variants will imitate the properties of their background blocks,",
                "such as the ability to fall like sand or sustain leaves. Can be",
                "Configured on an individual basis in the ore presets.")
        .define("blocks.bgImitation", true);

    public static final BooleanValue BG_DUPLICATION = COMMON
        .comment("Whether to suppress the ability for background blocks to duplicate",
                "themselves. Enabling this may improve gameplay balance by not letting",
                "ores spread in the world unexpectedly. Can be configured on an",
                "individual basis in the ore presets.")
        .define("blocks.bgDuplication", true);

    public static final BooleanValue FURNACE_RECIPES = COMMON
        .define("blocks.enableFurnaceRecipes", true);

    public static final BooleanValue ASSETS_FROM_RP = CLIENT
        .comment("Attempts to generate any new ore sprites from the topmost resource",
                "pack. Not an ideal solution for many resource packs.")
        .define("resources.assetsFromRP", true);

    public static final BooleanValue OVERLAY_MODEL_SHADE = CLIENT
        .comment("Indicates whether to enable shading in generated block models.")
        .define("models.overlayShade", true);

    public static final BooleanValue SHADE_MODIFIER = CLIENT
        .comment("Whether textures should use variable opacity to push and pull the",
                "background sprite.")
        .define("textures.shadeModifier", true);

    public static final BooleanValue OVERLAY_TRANSPARENCY = CLIENT
        .comment("Whether textures should be rendered with support for a full range",
                "of opacity. You may want to disable this if you're using shaders.")
        .define("models.overlayTransparency", true);

    public static final DoubleValue OVERLAY_SCALE = CLIENT
        .comment("How much larger the overlay model is than the background model.",
                "Lower values may look better, but cause z-fighting.")
        .defineInRange("models.overlayScale", 1.001, 1.0, 2.0);

    public static EnumValue<ModelSettings.Type> MODEL_TYPE = CLIENT
        .comment("The default type of model to generate for all ores.")
        .defineEnum("models.type", ModelSettings.Type.SINGLE);

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

    public static final BooleanValue COPY_TAGS = COMMON
        .comment("Whether to copy any tags at all. Globally toggles the copy feature.")
        .define("tags.copyTags", true);

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

    public static final BooleanValue AUTO_DISABLE_ORES = COMMON
        .comment("Whether vanilla spawning of ores should be blocked.")
        .define("worldGen.autoDisableOres", true);

    public static final BooleanValue AUTO_DISABLE_STONE = COMMON
        .comment("Whether vanilla spawning of stone variants should be blocked.")
        .define("worldGen.autoDisableStone", true);

    public static final BooleanValue ENABLE_OSV_ORES = COMMON
        .comment("Whether to spawn custom ore variants.")
        .define("worldGen.enableOSVOres", true);

    public static final BooleanValue ENABLE_OSV_STONE = COMMON
        .comment("Whether to spawn stone types with custom variables.")
        .define("worldGen.enableOSVStone", true);

    public static final BooleanValue HIGH_ACCURACY = COMMON
        .comment("Whether to use the vanilla datapack structure for distribution control in the",
                "generated presets. When this setting is turned off, OSV will attempt to convert",
                "foreign generator settings into a simpler, proprietary format, which may not",
                "have perfect accuracy in terms of behavior imitation.")
        .define("worldGen.highAccuracy", false);

    public static final ConfigValue<List<String>> DISABLED_FEATURES = COMMON
        .comment("Add the IDs of any configured features you wish to disable here. Regardless",
                "of which mod provides the feature, it will be disabled and not spawn in the",
                "world. For a list of which features can be disabled by the mod, run",
                "`/osv debug features` in game.")
        .define("worldGen.disabledFeatures", new ArrayList<>(), Objects::nonNull);

    static {
        CLIENT.comment(
            "Use this category to customize the default item display formatters.",
            "",
            "Additionally, ores can be configured on an individual basis through the",
            "ore presets inside of config/osv/ores.",
            "",
            "Possible settings: text, underlined, bold, italic, font, and color.",
            "",
            "Note that color may be either a common color name (e.g. red) or a hex ID",
            "(e.g. #123456)"
        ).push("items.formatters").pop(2);
    }

    private static final Map<String, List<Map<String, Object>>> FORMATTERS =
        DynamicCategoryBuilder.withPath("items.formatters")
            .withList("dense=true", ComponentReader.DEFAULT_DENSE)
            .withList("", ComponentReader.DEFAULT_NORMAL)
            .build(CLIENT, CLIENT_CFG);

    private static final Lazy<StateMap<List<Component>>> DEFAULT_COMPONENTS = Lazy.of(() -> {
        try {
            final StateMap<List<Component>> map = new StateMap<>();
            for (final Map.Entry<String, List<Map<String, Object>>> formatters : FORMATTERS.entrySet()) {
                final List<Component> components = new ArrayList<>();
                for (final Map<String, Object> formatter : formatters.getValue()) {
                    components.add(ComponentReader.fromRaw(formatter));
                }
                map.put(formatters.getKey(), components);
            }
            return map;
        } catch (final RuntimeException e) {
            return StateMap.singletonList("", new TextComponent("{bg} (Invalid Config)"));
        }
    });

    public static void register() {
        final ModContainer ctx = ModLoadingContext.get().getActiveContainer();
        ctx.addConfig(new CustomModConfig(ModConfig.Type.COMMON, COMMON.build(), ctx, COMMON_CFG));
        ctx.addConfig(new CustomModConfig(ModConfig.Type.CLIENT, CLIENT.build(), ctx, CLIENT_CFG));
    }

    private static CommentedFileConfig readConfig(final boolean client) {
        final ConfigFile cfg = ConfigProvider.loadFile(client);
        return new XjsFileConfig(cfg.file, cfg.json);
    }

    public static File getCommon() {
        return COMMON_CFG.getFile();
    }

    public static File getClient() {
        return CLIENT_CFG.getFile();
    }

    public static List<String> blockEntries() {
        return BLOCK_ENTRIES.get();
    }

    public static void setBlockEntries(final List<String> entries) {
        BLOCK_ENTRIES.set(entries);
    }

    public static boolean checkForDuplicates() {
        return CHECK_FOR_DUPLICATES.get();
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

    public static double overlayScale() {
        return OVERLAY_SCALE.get();
    }

    public static ModelSettings.Type modelType() {
        return MODEL_TYPE.get();
    }

    public static StateMap<List<Component>> getItemFormatters() {
        return DEFAULT_COMPONENTS.get();
    }

    public static PresetUpdatePreference updatePresets() {
        return UPDATE_PRESETS.get();
    }

    public static boolean forceCompatibilityMode() {
        return FORCE_COMPATIBILITY_MODE.get();
    }

    public static boolean bgImitation() {
        return BG_IMITATION.get();
    }

    public static boolean bgDuplication() {
        return BG_DUPLICATION.get();
    }

    public static boolean furnaceRecipes() {
        return FURNACE_RECIPES.get();
    }

    public static boolean assetsFromRP() {
        return ASSETS_FROM_RP.get();
    }

    public static boolean overlayShade() {
        return OVERLAY_MODEL_SHADE.get();
    }

    public static boolean shadeModifier() {
        return SHADE_MODIFIER.get();
    }

    public static boolean overlayTransparency() {
        return OVERLAY_TRANSPARENCY.get();
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

    public static boolean copyTags() {
        return COPY_TAGS.get();
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

    public static boolean autoDisableOres() {
        return AUTO_DISABLE_ORES.get();
    }

    public static boolean autoDisableStone() {
        return AUTO_DISABLE_STONE.get();
    }

    public static boolean enableOSVOres() {
        return ENABLE_OSV_ORES.get();
    }

    public static boolean enableOSVStone() {
        return ENABLE_OSV_STONE.get();
    }

    public static boolean highAccuracy() {
        return HIGH_ACCURACY.get();
    }

    public static List<String> disabledFeatures() {
        return DISABLED_FEATURES.get();
    }
}
