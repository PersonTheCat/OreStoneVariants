package personthecat.osv.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import personthecat.catlib.util.McUtils;
import personthecat.osv.preset.data.ModelSettings;
import personthecat.osv.util.StateMap;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Cfg {

    @SuppressWarnings("ConstantConditions")
    public static boolean modEnabled(final String id) {
        return McUtils.isModLoaded(id) && enabledMods().get(id);
    }

    public static boolean alwaysUpdatePresets() {
        return updatePresets() == PresetUpdatePreference.ALWAYS;
    }

    public static boolean smartUpdatePresets() {
        return updatePresets() == PresetUpdatePreference.MOD_UPDATED;
    }

    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static File getCommon() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static File getClient() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<String> blockEntries() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setBlockEntries(final List<String> entries) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean checkForDuplicates() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<String, List<String>> propertyGroups() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<String, List<String>> blockGroups() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<String, Boolean> enabledMods() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static double overlayScale() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ModelSettings.Type modelType() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static StateMap<List<Component>> getItemFormatters() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static PresetUpdatePreference updatePresets() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean forceCompatibilityMode() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean bgImitation() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean bgDuplication() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean furnaceRecipes() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean assetsFromRP() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean overlayShade() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean shadeModifier() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean overlayTransparency() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean autoRefresh() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean variantsDrop() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean variantsSilkTouch() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean mapInfestedVariants() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean copyTags() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean copyBlockTags() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean copyItemTags() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean copyBgTags() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean copyFgTags() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean copyDenseTags() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean denseOres() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static double denseChance() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int denseSmeltMultiplier() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int denseDropMultiplier() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int denseDropMultiplierMin() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean randomDropCount() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean biomeSpecific() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean autoDisableOres() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean autoDisableStone() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean enableOSVOres() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean enableOSVStone() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean highAccuracy() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<String> disabledFeatures() {
        throw new AssertionError();
    }
}
