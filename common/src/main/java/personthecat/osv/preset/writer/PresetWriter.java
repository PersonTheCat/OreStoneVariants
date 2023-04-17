package personthecat.osv.preset.writer;

import com.mojang.datafixers.util.Either;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.mutable.MutableInt;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.serialization.json.JsonTransformer;
import personthecat.catlib.serialization.json.JsonTransformer.ObjectResolver;
import personthecat.osv.world.providers.OffsetHeightProvider;
import xjs.core.Json;
import xjs.core.JsonArray;
import xjs.core.JsonObject;
import xjs.core.JsonValue;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.serialization.json.XjsUtils;
import personthecat.catlib.util.McUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.client.texture.BackgroundSelector;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.BlockSettings;
import personthecat.osv.preset.data.ClusterSettings;
import personthecat.osv.preset.data.DropSettings;
import personthecat.osv.preset.data.FlexiblePlacementSettings;
import personthecat.osv.preset.data.GenerationSettings;
import personthecat.osv.preset.data.ItemSettings;
import personthecat.osv.preset.data.ModelSettings;
import personthecat.osv.preset.data.OreSettings;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.preset.data.RecipeSettings;
import personthecat.osv.preset.data.StateSettings;
import personthecat.osv.preset.data.TextureSettings;
import personthecat.osv.preset.data.VariantSettings;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Log4j2
public class PresetWriter {

    private static final ObjectResolver PRESET_FIELD_SORTER =
        JsonTransformer.root()
            .reorder(
                List.of(OreSettings.Fields.variant, OreSettings.Fields.recipe, OreSettings.Fields.loot),
                List.of(OreSettings.Fields.gen))
            .freeze();

    public static void savePresets() {
        final MutableInt updated = new MutableInt(0);

        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            if (preset.isUpdated()) {
                FileIO.mkdirsOrThrow(preset.getFile().getParentFile());
                XjsUtils.writeJson(updateContents(preset), preset.getFile())
                    .ifErr(e -> log.warn("Could not save {}. Ignoring...", preset.getName()))
                    .ifErr(e -> log.debug("Updating preset", e))
                    .ifOk(t -> preset.onPresetSaved())
                    .ifOk(t -> updated.increment());
            }
        }
        if (updated.getValue() == 0) {
            log.info("Nothing to save. All presets are up to date.");
        } else if (updated.getValue() == 1) {
            log.info("1 preset was dynamically updated.");
        } else {
            log.info("{} presets were dynamically updated.", updated.getValue());
        }
    }

    private static JsonObject updateContents(final OrePreset preset) {
        final JsonValue cfg = XjsUtils.writeThrowing(OreSettings.CODEC, generateSettings(preset));
        if (preset.isReloadTextures()) removeTextures(preset.getRaw());
        preset.getRaw().setDefaults(format(cfg.asObject()));
        return preset.getRaw();
    }

    private static OreSettings generateSettings(final OrePreset preset) {
        return new OreSettings(
            createVariant(preset),
            BlockSettings.EMPTY,
            StateSettings.EMPTY,
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

    private static JsonObject format(final JsonObject generated) {
        final JsonObject variant = generated.getAsserted(OreSettings.Fields.variant).asObject();
        variant.remove(VariantSettings.Fields.bgDuplication);
        variant.remove(VariantSettings.Fields.bgImitation);
        variant.remove(VariantSettings.Fields.canBeDense);
        variant.remove(VariantSettings.Fields.copyTags);

        final JsonObject texture = generated.getAsserted(OreSettings.Fields.texture).asObject();
        texture.remove(TextureSettings.Fields.shade);
        final JsonValue background = texture.get(TextureSettings.Fields.background);
        if (background != null && BackgroundSelector.STONE_ID.toString().equals(background.asString())) {
            texture.remove(TextureSettings.Fields.background);
        }
        generated.getOptional(OreSettings.Fields.recipe, JsonValue::asObject).ifPresent(recipe -> {
            removeIf(recipe, RecipeSettings.Fields.count, v -> v.matches(Json.value(1)));
            removeIf(recipe, RecipeSettings.Fields.time, v -> v.matches(Json.value(200)));
        });
        removeIf(generated, OreSettings.Fields.loot, JsonValue::isNull);
        XjsUtils.getRegularObjects(generated, OreSettings.Fields.gen).forEach(gen -> {
            gen.setLineLength(1);
            gen.forEachRecursive(ref -> {
               if (ref.getOnly().isContainer()) {
                   ref.getOnly().asContainer().setLineLength(1);
               }
            });
            gen.remove(PlacedFeatureSettings.Fields.nested);
            gen.remove(PlacedFeatureSettings.Fields.denseRatio);
            removeIf(gen, PlacedFeatureSettings.Fields.dimensions, v -> v.isArray() && v.asArray().isEmpty());
            removeIf(gen, PlacedFeatureSettings.Fields.type,
                v -> v.isString() && v.asString().equalsIgnoreCase(PlacedFeatureSettings.Type.CLUSTER.name()));
            removeIf(gen, FlexiblePlacementSettings.Fields.bias, v -> v.matches(Json.value(0)));
            removeIf(gen, FlexiblePlacementSettings.Fields.chance, v -> v.matches(Json.value(1.0)));
            removeIf(gen, FlexiblePlacementSettings.Fields.spread, v -> v.matches(Json.value(0)));
            removeIf(gen, FlexiblePlacementSettings.Fields.modifiers, v -> v.isArray() && v.asArray().isEmpty());
            removeIf(gen, FlexiblePlacementSettings.Fields.count, v -> v.matches(Json.value(2)));
            gen.getOptional(FlexiblePlacementSettings.Fields.count, JsonValue::asArray).ifPresent(JsonArray::condense);
            removeIf(gen, FlexiblePlacementSettings.Fields.height, v ->
                v.matches(Json.object().add(OffsetHeightProvider.FIELD, Json.array(0, 128))));
            gen.getOptional(FlexiblePlacementSettings.Fields.height).ifPresent(height -> {
                if (height.isArray()) {
                    boolean anyCondensed = false;
                    for (final JsonValue value : height.asArray()) {
                        if (value.isContainer()) {
                            value.asContainer().condense();
                            anyCondensed = true;
                        }
                    }
                    if (!anyCondensed) {
                        height.asArray().condense();
                    }
                } else if (height.isObject()) {
                    height.asObject().condense();
                }
            });
            removeIf(gen, ClusterSettings.Fields.size, v -> v.matches(Json.value(8)));
            gen.getOptional(PlacedFeatureSettings.Fields.biomes, JsonValue::asObject).ifPresent(biomes -> {
                removeIf(biomes, BiomePredicate.Fields.mods, v -> v.isArray() && v.asArray().isEmpty());
                removeIf(biomes, BiomePredicate.Fields.names, v -> v.isArray() && v.asArray().isEmpty());
                removeIf(biomes, BiomePredicate.Fields.types, v -> v.isArray() && v.asArray().isEmpty());
                removeIf(biomes, BiomePredicate.Fields.blacklist, JsonValue::isFalse);
            });
        });
        generated.remove(OreSettings.Fields.block)
            .remove(OreSettings.Fields.item)
            .remove(McUtils.getPlatform())
            .remove(OreSettings.Fields.state)
            .remove(OreSettings.Fields.model)
            .remove(OreSettings.Fields.nested);
        PRESET_FIELD_SORTER.updateAll(generated);
        return generated;
    }

    private static void removeIf(final JsonObject o, final String key, final Predicate<JsonValue> predicate) {
        if (o.getOptional(key).filter(predicate).isPresent()) {
            o.remove(key);
        }
    }
}
