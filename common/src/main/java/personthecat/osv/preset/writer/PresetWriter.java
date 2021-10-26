package personthecat.osv.preset.writer;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.HjsonUtilsMod;
import personthecat.osv.ModRegistries;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.*;

@Log4j2
public class PresetWriter {

    public static void savePresets() {
        final MutableInt updated = new MutableInt(0);

        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            if (preset.isUpdated()) {
                HjsonUtils.writeJson(updateContents(preset), preset.getFile())
                    .ifErr(e -> log.warn("Could not save {}. Ignoring...", preset.getName()))
                    .ifErr(e -> log.debug("Updating preset", e))
                    .ifOk(t -> updated.increment());
            }
        }
        if (updated.getValue() == 0) {
            log.info("Nothing to save. All presets are up to date.");
        } else {
            log.info("{} presets were dynamically updated.", updated.getValue());
        }
    }

    private static JsonObject updateContents(final OrePreset preset) {
        final JsonValue cfg = HjsonUtilsMod.writeOrThrow(OreSettings.CODEC, generateSettings(preset));
        HjsonUtilsMod.setRecursivelyIfAbsent(preset.getRaw(), cfg.asObject());
        return preset.getRaw();
    }

    private static OreSettings generateSettings(final OrePreset preset) {
        return new OreSettings(
            createVariant(preset),
            BlockSettings.EMPTY,
            StateSettings.EMPTY,
            PlatformBlockSettings.getEmpty(),
            ItemSettings.EMPTY,
            DropSettings.EMPTY,
            createGen(preset),
            createRecipe(preset),
            createTexture(preset),
            ModelSettings.EMPTY
        );
    }

    private static VariantSettings createVariant(final OrePreset preset) {
        return VariantSettings.withOriginal(preset.getOreId());
    }

    private static GenerationSettings createGen(final OrePreset preset) {
        return new GenerationSettings(preset.getFeatures());
    }

    private static RecipeSettings createRecipe(final OrePreset preset) {
        return RecipeSettings.fromChecked(preset.getCheckedRecipe());
    }

    private static TextureSettings createTexture(final OrePreset preset) {
        final TextureSettings cfg = preset.getTexture();
        return new TextureSettings(cfg.getBackground(), cfg.isShade(), cfg.getThreshold(), preset.getOverlayIds(),
            preset.getOverlayIds(), preset.getOverlayModifiers());
    }
}
