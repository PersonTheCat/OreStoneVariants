package personthecat.osv.preset.writer;

import com.mojang.datafixers.util.Either;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.McUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.client.texture.BackgroundSelector;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.*;

import java.util.Collections;

@Log4j2
public class PresetWriter {

    public static void savePresets() {
        final MutableInt updated = new MutableInt(0);

        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            if (preset.isUpdated()) {
                FileIO.mkdirsOrThrow(preset.getFile().getParentFile());
                HjsonUtils.writeJson(updateContents(preset), preset.getFile())
                    .ifErr(e -> log.warn("Could not save {}. Ignoring...", preset.getName()))
                    .ifErr(e -> log.debug("Updating preset", e))
                    .ifOk(t -> preset.onPresetSaved())
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
        final JsonValue cfg = HjsonUtils.writeThrowing(OreSettings.CODEC, generateSettings(preset));
        if (preset.isReloadTextures()) removeTextures(preset.getRaw());
        HjsonUtils.setRecursivelyIfAbsent(preset.getRaw(), cleanup(cfg.asObject()));
        return preset.getRaw();
    }

    private static OreSettings generateSettings(final OrePreset preset) {
        return new OreSettings(
            createVariant(preset),
            BlockSettings.EMPTY,
            StateSettings.EMPTY,
            PlatformBlockSettings.getEmpty(),
            ItemSettings.EMPTY,
            createDrops(preset),
            createGen(preset),
            createRecipe(preset),
            createTexture(preset),
            ModelSettings.EMPTY,
            Collections.emptyList()
        );
    }

    private static VariantSettings createVariant(final OrePreset preset) {
        return VariantSettings.withOriginal(preset.getOreId());
    }

    private static DropSettings createDrops(final OrePreset preset) {
        if (!preset.hasLootId()) return DropSettings.EMPTY;
        return new DropSettings(Either.left(preset.getLootReference()));
    }

    private static GenerationSettings createGen(final OrePreset preset) {
        return new GenerationSettings(preset.getFeatures());
    }

    private static RecipeSettings createRecipe(final OrePreset preset) {
        return RecipeSettings.fromChecked(preset.getCheckedRecipe());
    }

    private static TextureSettings createTexture(final OrePreset preset) {
        if (McUtils.isDedicatedServer()) return TextureSettings.EMPTY;

        final TextureSettings cfg = preset.getTexture();
        return new TextureSettings(cfg.isShade(), cfg.getThreshold(), preset.getBackgroundTexture(),
            preset.getBackgroundIds(), preset.getOverlayIds(), null);
    }

    private static void removeTextures(final JsonObject raw) {
        final JsonValue texture = raw.get(OreSettings.Fields.texture);
        if (texture != null) {
            final JsonObject textureObject = texture.asObject();
            textureObject.remove(TextureSettings.Fields.original);
            textureObject.remove(TextureSettings.Fields.overlay);
        }
    }

    private static JsonObject cleanup(final JsonObject generated) {
        final JsonObject variant = generated.get(OreSettings.Fields.variant).asObject();
        variant.remove(VariantSettings.Fields.bgDuplication);
        variant.remove(VariantSettings.Fields.bgImitation);
        variant.remove(VariantSettings.Fields.canBeDense);
        variant.remove(VariantSettings.Fields.copyTags);

        final JsonObject texture = generated.get(OreSettings.Fields.texture).asObject();
        texture.remove(TextureSettings.Fields.shade);
        final JsonValue background = texture.get(TextureSettings.Fields.background);
        if (background != null && BackgroundSelector.STONE_ID.toString().equals(background.asString())) {
            texture.remove(TextureSettings.Fields.background);
        }
        HjsonUtils.getRegularObjects(generated, OreSettings.Fields.gen).forEach(gen -> {
            gen.remove(DecoratedFeatureSettings.Fields.nested);
            gen.remove(DecoratedFeatureSettings.Fields.denseRatio);
        });

        return generated.remove(OreSettings.Fields.block)
            .remove(OreSettings.Fields.item)
            .remove(McUtils.getPlatform())
            .remove(OreSettings.Fields.state)
            .remove(OreSettings.Fields.model)
            .remove(OreSettings.Fields.nested);
    }
}
