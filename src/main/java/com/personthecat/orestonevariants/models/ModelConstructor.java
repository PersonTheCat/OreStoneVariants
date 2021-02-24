package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.ResourceHelper;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.HjsonTools;
import com.personthecat.orestonevariants.util.MultiValueMap;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import personthecat.fresult.Result;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.personthecat.orestonevariants.io.SafeFileIO.getResource;
import static com.personthecat.orestonevariants.io.SafeFileIO.getResourceAsString;
import static com.personthecat.orestonevariants.io.SafeFileIO.resourceExists;
import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.HjsonTools.addToArray;
import static com.personthecat.orestonevariants.util.HjsonTools.asOrToArray;
import static com.personthecat.orestonevariants.util.HjsonTools.getObject;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;

public class ModelConstructor {

    // Extraneous hardcoded key and value strings.
    private static final String VARIANTS_KEY = "variants";
    private static final String MODEL_KEY = "model";
    private static final String PARENT_KEY = "parent";
    private static final String DENSE_ON = "dense=true";
    private static final String DENSE_OFF = "dense=false";
    private static final String TRANSLUCENT_TYPE = "translucent";
    private static final String CUTOUT_MIPPED_TYPE = "cutout_mipped";

    // Keys and template data for the model template.
    private static final String BG_KEY = "{bg}";
    private static final String FG_KEY = "{fg}";
    private static final String LAYER_KEY = "{layer}";
    private static final String MODEL_TEMPLATE_PATH = "assets/osv/model_template.txt";

    // Keys and template data for the overlay model.
    private static final String MIN_KEY = "{min}";
    private static final String MAX_KEY = "{max}";
    private static final String SHADE_KEY = "{shade}";
    private static final String OVERLAY_TEMPLATE_PATH = "assets/osv/overlay_template.txt";
    private static final String OVERLAY_MODEL_PATH = "assets/osv/models/block/overlay.json";

    /** Contains placeholders for all of the data that changes between models. */
    private static final String MODEL_TEMPLATE = getResourceAsString(MODEL_TEMPLATE_PATH)
        .orElseThrow(() -> runEx("Couldn't find model template"));

    public static void generateOverlayModel() {
        if (!resourceExists(OVERLAY_MODEL_PATH)) {
            final double overlayOffset = ((Cfg.modelScale.get() * 16.0) - 16.0) / 2.0;
            final String overlay = getResourceAsString(OVERLAY_TEMPLATE_PATH)
                .orElseThrow(() -> runEx("Couldn't find overlay template"))
                .replace(MIN_KEY, String.valueOf(0.0F - overlayOffset))
                .replace(MAX_KEY, String.valueOf(16.0F + overlayOffset))
                .replace(SHADE_KEY, String.valueOf(Cfg.shadeOverlays.get()));
            writeJson(OVERLAY_MODEL_PATH, overlay);
        }
    }

    // Generates a new set of block models from fg and bg with osv state properties.
    public static void generateModels(BaseOreVariant variant) {
        final OreProperties properties = variant.properties;
        final Block bg = variant.bgState.getBlock();
        final ResourceLocation id = variant.getRegistryName();

        generateModels(id, properties, bg);
    }

    private static void generateModels(ResourceLocation id, OreProperties properties, Block bg) {
        final JsonObject variants = generateAllVariants(properties, bg);
        generateBlockStateFile(id, variants);
        generateItemModels(id, properties, variants, bg);
    }

    private static JsonObject generateAllVariants(OreProperties properties, Block bg) {
        final JsonObject variants = new JsonObject();

        for (JsonObject.Member member : loadVariantsOrEmpty(bg)) {
            for (JsonValue variant : asOrToArray(member.getValue())) {
                if (variant.isObject()) { // Ignore unknown data.
                    addVariants(properties, variants, member.getName(), variant.asObject());
                }
            }
        }
        return variants;
    }

    private static void generateBlockStateFile(ResourceLocation id, JsonObject variants) {
        final String concretePath = getBlockStatePath(id);

        if (!resourceExists(concretePath)) {
            final String blockState = new JsonObject()
                .add(VARIANTS_KEY, compressArrays(variants))
                .toString(Stringify.FORMATTED);
            ResourceHelper.writeResource(concretePath, blockState).ifErr(Result::WARN);
        }
    }

    private static void generateItemModels(ResourceLocation id, OreProperties properties, JsonObject variants, Block bg) {
        final String normalPath = getItemModelPath(id);
        final String densePath = PathTools.ensureDense(normalPath);
        final String normalModel = loadItemModel(bg)
            .flatMap(item -> getEquivalentFromItem(properties, item))
            .orElseGet(() -> getFirstNormalModel(variants));
        final String denseModel = PathTools.ensureDense(normalModel);

        if (!resourceExists(normalPath)) {
            writeJson(normalPath, new JsonObject().add(PARENT_KEY, normalModel));
        }
        if (!resourceExists(densePath)) {
            writeJson(densePath, new JsonObject().add(PARENT_KEY, denseModel));
        }
    }

    private static void addVariants(OreProperties properties, JsonObject variants, String bgKey, JsonObject base) {
        final MultiValueMap<String, ResourceLocation> textures = properties.texture.overlayLocations;

        for (Map.Entry<String, List<ResourceLocation>> fg : textures.entrySet()) {
            for (ResourceLocation texture : fg.getValue()) {
                // Generate the regular variant model.
                final String normal = generateKey(bgKey, fg.getKey(), DENSE_OFF);
                final String normalTexture = texture.toString();
                addToArray(variants, normal, generateVariant(properties, base, normalTexture, false));

                // Followed by the dense variant model.
                final String dense = generateKey(bgKey, fg.getKey(), DENSE_ON);
                final String denseTexture = PathTools.ensureDense(normalTexture);
                addToArray(variants, dense, generateVariant(properties, base, denseTexture, true));
            }
        }
    }

    // Generates block state keys, ignoring empty properties ("": {...}).
    private static String generateKey(String... values) {
        final StringBuilder sb = new StringBuilder();
        for (String property : values) {
            if (property.length() > 0) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(property);
            }
        }
        return sb.toString();
    }

    private static JsonObject generateVariant(OreProperties properties, JsonObject base, String texture, boolean dense) {
        final JsonObject variant = new JsonObject();

        for (JsonObject.Member member : base) {
            final String name = member.getName();
            final JsonValue value = member.getValue();

            if (MODEL_KEY.equals(member.getName())) {
                final String bgModel = value.asString();
                variant.add(MODEL_KEY, loadOrGenerateModel(properties, bgModel, texture, dense));
            } else {
                variant.add(name, value);
            }
        }
        return variant;
    }

    private static String loadOrGenerateModel(OreProperties properties, String bgModel, String texture, boolean dense) {
        final String path = foreignModelToOSV(properties, bgModel, dense);
        final String concretePath = getModelPath(path);
        if (!resourceExists(concretePath)) {
            final String model = generateModel(bgModel, texture);
            writeJson(concretePath, model);
        }
        return path;
    }

    private static Optional<String> getEquivalentFromItem(OreProperties properties, JsonObject item) {
        final String parent = item.getString(PARENT_KEY, "");
        final String equivalent = foreignModelToOSV(properties, parent, false);
        final String concrete = getModelPath(equivalent);

        return resourceExists(concrete) ? full(equivalent) : empty();
    }

    private static String getFirstNormalModel(JsonObject variants) {
        for (JsonObject.Member variant : variants) {
            final JsonValue value = variant.getValue();
            if (variant.getName().contains(DENSE_OFF) && value.isObject()) {
                return getString(value.asObject(), PARENT_KEY)
                    .orElseThrow(() -> runEx("Generated variant is not a string"));
            }
        }
        throw runEx("Generated block state contains no variants");
    }

    private static String foreignModelToOSV(OreProperties properties, String from, boolean dense) {
        final String path = PathTools.namespaceToSub(from);
        final String prefix = (dense ? "dense_" : "") + properties.name + "_";
        return f("{}:{}", Main.MODID, PathTools.prependFilename(path, prefix));
    }

    private static String generateModel(String bgModel, String texture) {
        return MODEL_TEMPLATE.replace(BG_KEY, bgModel)
            .replace(FG_KEY, texture)
            .replace(LAYER_KEY, getFgType());
    }

    private static String getFgType() {
        return Cfg.translucentTextures.get() ? TRANSLUCENT_TYPE : CUTOUT_MIPPED_TYPE;
    }

    /** Replaces any single element arrays at the top level with regular values. */
    private static JsonObject compressArrays(JsonObject json) {
        final JsonObject clone = new JsonObject();
        for (JsonObject.Member member : json) {
            final String name = member.getName();
            final JsonValue value = member.getValue();

            if (value.isArray() && value.asArray().size() == 1) {
                clone.add(name, value.asArray().get(0));
            } else {
                clone.add(name, value);
            }
        }
        return clone;
    }

    private static void writeJson(String concretePath, JsonObject json) {
        writeJson(concretePath, json.toString(Stringify.FORMATTED));
    }

    private static void writeJson(String concretePath, String json) {
        ResourceHelper.writeResource(concretePath, json).ifErr(Result::WARN);
    }

    private static JsonObject loadVariantsOrEmpty(Block block) {
        return loadBlockStateFile(block)
            .flatMap(json -> getObject(json, VARIANTS_KEY))
            .orElseGet(JsonObject::new);
    }

    // This is possible now that all states use the same mapper.
    private static Optional<JsonObject> loadBlockStateFile(Block block) {
        return loadJson(getBlockStatePath(block));
    }

    private static Optional<JsonObject> loadItemModel(Block block) {
        return loadJson(getItemModelPath(block));
    }

    private static String getBlockStatePath(Block block) {
        return getBlockStatePath(Objects.requireNonNull(block.getRegistryName(), "Unregistered block"));
    }

    private static String getBlockStatePath(ResourceLocation id) {
        return f("assets/{}/blockstates/{}.json", id.getNamespace(), id.getPath());
    }

    private static String getModelPath(String location) {
        final ResourceLocation id = new ResourceLocation(location);
        return f("assets/{}/models/{}.json", id.getNamespace(), id.getPath());
    }

    private static String getItemModelPath(Block block) {
        return getItemModelPath(Objects.requireNonNull(block.getRegistryName(), "Unregistered block"));
    }

    private static String getItemModelPath(ResourceLocation id) {
        return f("assets/{}/models/item/{}.json", id.getNamespace(), id.getPath());
    }

    private static Optional<JsonObject> loadJson(String path) {
        return getResource(path).flatMap(HjsonTools::readJson);
    }
}
