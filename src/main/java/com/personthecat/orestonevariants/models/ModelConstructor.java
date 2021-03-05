package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.ResourceHelper;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.HjsonTools;
import com.personthecat.orestonevariants.util.MultiValueMap;
import com.personthecat.orestonevariants.util.PathTools;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.MultiLayerModel;
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
import static com.personthecat.orestonevariants.util.CommonMethods.nullable;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.CommonMethods.stateHasVariant;
import static com.personthecat.orestonevariants.util.HjsonTools.addToArray;
import static com.personthecat.orestonevariants.util.HjsonTools.asOrToArray;
import static com.personthecat.orestonevariants.util.HjsonTools.getObject;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;

@Log4j2
public class ModelConstructor {

    // Extraneous hardcoded key and value strings.
    private static final String VARIANTS_KEY = "variants";
    private static final String MODEL_KEY = "model";
    private static final String PARENT_KEY = "parent";
    private static final String TEXTURES_KEY = "textures";
    private static final String PARTICLE_KEY = "particle";
    private static final String DENSE_ON = "dense=true";
    private static final String DENSE_OFF = "dense=false";
    private static final String TRANSLUCENT_TYPE = "translucent";
    private static final String CUTOUT_MIPPED_TYPE = "cutout_mipped";

    // Keys and template data for the model template.
    private static final String BG_KEY = "{bg}";
    private static final String FG_KEY = "{fg}";
    private static final String BG_LAYER_KEY = "{bg_layer}";
    private static final String FG_LAYER_KEY = "{fg_layer}";
    private static final String SOLID_LAYER_KEY = "solid";
    private static final String PART_KEY = "{particle}";
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

    /**
     *  Constructs the overlay model to be used by <em>all</em> of the ore variants. The
     * generated file can be edited by the user, but it will come with defaults which are
     * ultimately defined in the config file.
     */
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

    /**
     *  Generates a new set of block models from an ore property type, a background block,
     * and any extra states defined in <code>textures</code>, as these are individually
     * mapped to different textures.
     *
     * <p>This operation will construct concrete models for the following entities:
     *
     * <ul>
     *   <li>Block state files</li>
     *   <li>Block models</li>
     *   <li>Item models</li>
     * </ul>
     *
     *  These models will not be generated if they already exist, and thus the user can
     * edit any individual model component without changes being overwritten.
     *
     * @param variant The block to generate models for. Only {@link BaseOreVariant} types
     *                contain all of the needed information.
     */
    public static void generateModels(BaseOreVariant variant) {
        final OreProperties properties = variant.properties;
        final Block bg = variant.bgState.getBlock();
        final ResourceLocation id = variant.getRegistryName();

        if (properties.texture.originals.isEmpty()) {
            log.error("No texture data defined for {}. Skipping models.", properties.name);
        } else {
            generateModels(id, properties, bg);
        }
    }

    /**
     * Variant of {@link #generateModels(BaseOreVariant)} which accepts the raw information
     * needed for constructing block variants, block states, and item models.
     *
     * @param id The location of the <em>OSV</em> block.
     * @param properties Data containing texture info and clues for generating model names.
     * @param bg The background block which our generated models are based on.
     */
    private static void generateModels(ResourceLocation id, OreProperties properties, Block bg) {
        final JsonObject variants = generateAllVariants(properties, bg);
        generateBlockStateFile(id, variants);
        generateItemModels(id, properties, variants, bg);
    }

    /**
     *  This function is responsible for generating all of the block model variants which
     * will be needed for the current ore. Variants are based on all combinations of the
     * following data:
     *
     * <ul>
     *   <li>Background variants</li>
     *   <li>Texture variants (not foreground variants)</li>
     *   <li>Dense state (true or false)</li>
     * </ul>
     *
     *  <p>This function will record and return data about the models generated in the form of
     * a JSON object which can be used to construct a new block state file.
     *
     * @param properties Data containing texture info and clues for generating model names.
     * @param bg The background block which our generated models are based on.
     * @return The contents of <code>variants</code> to go in a new block state file.
     */
    private static JsonObject generateAllVariants(OreProperties properties, Block bg) {
        final JsonObject variants = new JsonObject();

        for (JsonObject.Member member : loadVariantsOrEmpty(bg)) {
            for (JsonValue variant : asOrToArray(member.getValue())) {
                if (variant.isObject()) { // Ignore unknown data.
                    addVariants(properties, variants, member.getName(), variant.asObject(), bg);
                }
            }
        }
        return variants;
    }

    /**
     * This function generates a new block state file at the given resource location. A new
     * model will not be generated if one already exists.
     *
     * @param id The location where this model will be written (in /blockstates)
     * @param variants The JSON contents of <code>variants</code>.
     */
    private static void generateBlockStateFile(ResourceLocation id, JsonObject variants) {
        final String concretePath = getBlockStatePath(id);

        if (!resourceExists(concretePath)) {
            final JsonObject blockState = new JsonObject()
                .add(VARIANTS_KEY, compressArrays(variants));
            writeJson(concretePath, blockState);
        }
    }

    /**
     *  This function generates normal and dense variant items for the block corresponding to
     * <code>id</code>. An existing block model will be reused as the parent model for these
     * items. The logic works like this:
     *
     * <ol>
     *   <li>Read the item model for <code>bg</code> and get its parent model, if possible.</li>
     *   <li>Look for an equivalent OSV block model for the given <code>properties</code>.</li>
     *   <li>If an OSV model exists, use it. If not, get the first normal model in <code>variants</code>.</li>
     * </ol>
     *
     * @param id The location where this model will be written (in /models/item)
     * @param properties Data containing clues about which model to reuse.
     * @param variants All of the regular block variants, if needed.
     * @param bg The background block which these models are based on.
     */
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

    /**
     * This function places every possible OSV state inside of a single background state.
     * Changes will be recorded inside of <code>variants</code>.
     *
     * @param properties Data used for generating the foreground model name.
     * @param variants All of the regular block variants being written into.
     * @param bgKey The current background state being reused.
     * @param base The base model corresponding to this background state.
     */
    private static void addVariants(OreProperties properties, JsonObject variants, String bgKey, JsonObject base, Block bg) {
        final MultiValueMap<String, ResourceLocation> textures = properties.texture.overlayLocations;

        for (Map.Entry<String, List<ResourceLocation>> fg : textures.entrySet()) {
            final String fgKey = fg.getKey();

            for (ResourceLocation texture : fg.getValue()) {
                // Generate the regular variant model.
                final String normal = generateKey(bgKey, fgKey, DENSE_OFF);
                final String normalTexture = texture.toString();
                addToArray(variants, normal, generateVariant(properties, fgKey, base, normalTexture, false, bg));

                // Followed by the dense variant model.
                final String dense = generateKey(bgKey, fgKey, DENSE_ON);
                final String denseTexture = PathTools.ensureDense(normalTexture);
                addToArray(variants, dense, generateVariant(properties, fgKey, base, denseTexture, true, bg));
            }
        }
    }

    /**
     *  Generates a block state key from multiple states, matching the vanilla syntax in
     * 1.16. This will ignore empty properties, e.g. (<code>"": {...}</code>).
     *
     * <p>For example, given the following state values:
     *
     * <ul>
     *   <li><code>axis=x</code></li>
     *   <li><code>oxidation=0</code></li>
     *   <li><code>dense=false</code></li>
     * </ul>
     *
     * The following key will be generated: <code>axis=x,oxidation=0,dense=false</code>
     *
     * @param values All of the state properties being reused.
     * @return A new key containing these properties.
     */
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

    /**
     * Generates a <em>single</em> block state variant and block model.
     *
     * @param properties Data used for generating model locations.
     * @param base The variant data from the background block state file.
     * @param texture The overlay resource location, as a string.
     * @param dense Whether to generate the dense or regular variant.
     * @param bg The background block, used to determine a render layer.
     * @return All of the generated variant data, which will go in the block state file.
     */
    private static JsonObject generateVariant(OreProperties properties, String key, JsonObject base, String texture, boolean dense, Block bg) {
        final JsonObject variant = new JsonObject();

        for (JsonObject.Member member : base) {
            final String name = member.getName();
            final JsonValue value = member.getValue();

            if (MODEL_KEY.equals(member.getName())) {
                final String bgModel = value.asString();
                variant.add(MODEL_KEY, loadOrGenerateModel(properties, key, bgModel, texture, dense, bg));
            } else {
                variant.add(name, value);
            }
        }
        return variant;
    }

    /**
     * Generates a new block model, otherwise returns the resource location of the model.
     * The model is guaranteed to exist at this point.
     *
     * @param properties Data used for generating the model location.
     * @param bgModel The resource location of the background model, as a string.
     * @param texture The overlay resource location, as a string.
     * @param dense Whether to generate the dense or regular variant.
     * @param bg The background block, used to determine a render layer.
     * @return The resource location of the generated model, as a string.
     */
    private static String loadOrGenerateModel(OreProperties properties, String key, String bgModel, String texture, boolean dense, Block bg) {
        final String path = foreignModelToOSV(properties, key, bgModel, dense);
        final String concretePath = getModelPath(path);
        if (!resourceExists(concretePath)) {
            writeJson(concretePath, generateModel(bgModel, texture, bg));
        }
        return path;
    }

    /**
     * Attempts to locate an equivalent OSV block model from a foreign item model.
     *
     * @param properties Data used for generating the model location.
     * @param item The parsed item model of the background block.
     * @return The resource location of the <em>OSV</em> block model, if possible.
     */
    private static Optional<String> getEquivalentFromItem(OreProperties properties, JsonObject item) {
        final String parent = item.getString(PARENT_KEY, "");
        final String equivalent = foreignModelToOSV(properties, parent, false);
        final String concrete = getModelPath(equivalent);

        return resourceExists(concrete) ? full(equivalent) : empty();
    }

    /**
     *  Gets the first model from a <code>variants</code> object which is not dense.
     *
     *  Note that this requires the key <code>dense=false</code> and will not work if that
     * is removed. This key <em>should not be removed</em>, as this has been tested and does
     * not work as expected. Thus, its existence here is a lazy assertion that the variant
     * keys are generated correctly.
     *
     * @param variants The <code>variants</code> object from the block state file.
     * @return The first normal model resource location, as a string.
     */
    private static String getFirstNormalModel(JsonObject variants) {
        for (JsonObject.Member variant : variants) {
            JsonValue value = variant.getValue();

            if (variant.getName().contains(DENSE_OFF)) {
                if (value.isArray() && value.asArray().size() > 0) {
                    value = value.asArray().get(0);
                }
                if (value.isObject()) {
                    return getString(value.asObject(), MODEL_KEY)
                        .orElseThrow(() -> runEx("Generated variant does not contain a model"));
                }
            }
        }
        throw runEx("Generated block state contains no variants");
    }

    /**
     * Converts the model path of a <em>foreign</em> block (i.e. not an OSV block) into
     * the equivalent OSV model path.
     *
     * @param properties Data containing foreground info which will be used in the path.
     * @param from The original model resource location, as a string.
     * @param dense Whether to use the dense model path.
     * @return The OSV model resource location, as a string.
     */
    private static String foreignModelToOSV(OreProperties properties, String from, boolean dense) {
        final String path = PathTools.namespaceToSub(from);
        final String prefix = (dense ? "dense_" : "") + properties.name + "_";
        return f("{}:{}", Main.MODID, PathTools.prependFilename(path, prefix));
    }

    /**
     * Variant of {@link #foreignModelToOSV(OreProperties, String, boolean)} which is
     * intended for a specific foreground variant.
     *
     * @param properties Data containing foreground info which will be used in the path.
     * @param key Any additional variant data from <code>textures</code>.
     * @param from The original model resource location, as a string.
     * @param dense Whether to use the dense model path.
     * @return The OSV model resource location, as a string.
     */
    private static String foreignModelToOSV(OreProperties properties, String key, String from, boolean dense) {
        final StringBuilder sb = new StringBuilder(foreignModelToOSV(properties, from, dense));
        if (key.isEmpty()) {
            return sb.toString();
        }
        for (String variant : key.split(",")) {
            final String[] kv = variant.split("=");
            if (kv.length != 2) {
                throw runExF("Invalid variant string: {}", variant);
            }
            final String k = kv[0];
            final String v = kv[1];

            if ("true".equals(v)) {
                sb.append("_");
                sb.append(kv[0]);
            } else if (!"false".equals(v)) {
                // In other words, if this is not the default variant.
                if (!stateHasVariant(properties.ore.get(), k, v)) {
                    sb.append("_");
                    sb.append(v);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Converts the <code>MODEL_TEMPLATE</code> from the jar into a new block model.
     *
     * @param bgModel The original model's resource location, as a string.
     * @param texture The overlay texture's resource location, as a string.
     * @param bg The background block, used to determine its render layer.
     * @return The raw JSON model data, as a string.
     */
    private static String generateModel(String bgModel, String texture, Block bg) {
        return MODEL_TEMPLATE.replace(BG_KEY, bgModel)
            .replace(PART_KEY, resolveParticleTexture(bgModel).orElse(texture))
            .replace(FG_KEY, texture)
            .replace(BG_LAYER_KEY, getBgType(bg))
            .replace(FG_LAYER_KEY, getFgType());
    }

    /**
     * Begins resolving a particle texture when given model's resource location.
     *
     * @param model The model's resource location, as a string.
     * @return This model's particle texture location, as a string.
     */
    private static Optional<String> resolveParticleTexture(String model) {
        return resolveTexture(PARTICLE_KEY, model, new JsonObject());
    }

    /**
     * Resolves any texture location from a standard block model. This most likely
     * only works with vanilla models, but this coverage is considered good enough.
     *
     * @param key The key of the texture being resolved.
     * @param model The model's resource location, as a string.
     * @param storage JSON data containing all of the textures up to this point.
     * @return The given texture's resource location, as a string.
     */
    private static Optional<String> resolveTexture(String key, String model, JsonObject storage) {
        return loadModel(model).flatMap(json -> resolveTexture(key, json, storage));
    }

    /**
     * Resolves the particle texture location from a standard block model using raw
     * JSON data.
     *
     * @param key The key of the texture being resolved.
     * @param model The raw JSON model data.
     * @param storage JSON data containing all of the textures up to this point.
     * @return The given texture's resource location, as a string.
     */
    private static Optional<String> resolveTexture(String key, JsonObject model, JsonObject storage) {
        getObject(model, TEXTURES_KEY).ifPresent(textures -> copyJsonData(textures, storage));
        final Optional<String> texture = getString(storage, key);

        if (texture.isPresent()) {
            if (texture.get().startsWith("#")) {
                return resolveTexture(texture.get().substring(1), model, storage);
            }
            return texture;
        }
        return getString(model, PARENT_KEY).flatMap(parent -> resolveTexture(key, parent, storage));
    }

    /**
     * Copies any data at the top level from one JSON into another, skipping any values that
     * already exist in <code>to</code>.
     *
     * @param from The JSON being copied out of.
     * @param to The JSON being copied into.
     */
    private static void copyJsonData(JsonObject from, JsonObject to) {
        for (JsonObject.Member member : from) {
            if (!to.has(member.getName())) {
                to.add(member.getName(), member.getValue());
            }
        }
    }

    /**
     * Determines which render layer to use for the background layer.
     *
     * @param bg The block this layer is based on.
     * @return The name of the layer, as a string.
     */
    @SuppressWarnings("deprecation")
    private static String getBgType(Block bg) {
        final RenderType layer = RenderTypeLookup.func_239221_b_(bg.getDefaultState());
        return nullable(MultiLayerModel.Loader.BLOCK_LAYERS.inverse().get(layer)).orElse(SOLID_LAYER_KEY);
    }

    /**
     * Determines whether to use the translucent render layer or the cutout mipped layer.
     *
     * @return The key of the desired layer, as indicated in the config file.
     */
    private static String getFgType() {
        return Cfg.translucentTextures.get() ? TRANSLUCENT_TYPE : CUTOUT_MIPPED_TYPE;
    }

    /**
     * Replaces any single element arrays at the top level with regular values.
     *
     * @param json Any regular JSON file.
     * @return The updated JSON data.
     */
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

    /**
     * Writes a new JSON object to the resources directory as a formatted string.
     *
     * @param concretePath The raw, relative path to this model.
     * @param json The JSON object being serialized.
     */
    private static void writeJson(String concretePath, JsonObject json) {
        writeJson(concretePath, json.toString(Stringify.FORMATTED));
    }

    /**
     * Writes a string to the resources directory.
     *
     * @param concretePath The raw, relative path to this file.
     * @param json Any string data (which will always be a JSON).
     */
    private static void writeJson(String concretePath, String json) {
        ResourceHelper.writeResource(concretePath, json).ifErr(Result::WARN);
    }

    /**
     * Attempts to load the <code>variants</code> object from a block state file.
     *
     * Note that there is a design flaw here: multipart blocks are <em>not</em> supported.
     *
     * @param block The block being researched.
     * @return The <code>variants</code> data, or an empty JSON object.
     */
    private static JsonObject loadVariantsOrEmpty(Block block) {
        return loadBlockStateFile(block)
            .flatMap(json -> getObject(json, VARIANTS_KEY))
            .orElseGet(JsonObject::new);
    }

    /**
     * Attempts to locate the block state corresponding to the given block.
     *
     * @param block The background block being researched.
     * @return The entire block state file as a JSON object, if possible.
     */
    private static Optional<JsonObject> loadBlockStateFile(Block block) {
        return loadJson(getBlockStatePath(block));
    }

    /**
     * Attempts to locate the main item model corresponding to the given block.
     *
     * @param block The background block being researched.
     * @return The entire item model as a JSON object, if possible.
     */
    private static Optional<JsonObject> loadItemModel(Block block) {
        return loadJson(getItemModelPath(block));
    }

    /**
     * Attempts to load the model corresponding to this location.
     *
     * @param id The resource location pointing to the model, as a string.
     * @return The deserialized JSON data.
     */
    private static Optional<JsonObject> loadModel(String id) {
        return loadJson(getModelPath(id));
    }

    /**
     * Attempts to locate the block state file corresponding to the given block.
     *
     * This is only possible now that all states use the same mapper.
     *
     * @param block The background block being researched.
     * @return The path to the block state file.
     */
    private static String getBlockStatePath(Block block) {
        return getBlockStatePath(Objects.requireNonNull(block.getRegistryName(), "Unregistered block"));
    }

    /**
     * Converts a resource location into a raw block state path.
     *
     * @param id The location of the desired block state file.
     * @return The concrete location of this file.
     */
    private static String getBlockStatePath(ResourceLocation id) {
        return f("assets/{}/blockstates/{}.json", id.getNamespace(), id.getPath());
    }

    /**
     * Converts a resource location into a raw model path.
     *
     * @param location The location of the desired model file.
     * @return The concrete location of this file.
     */
    private static String getModelPath(String location) {
        final ResourceLocation id = new ResourceLocation(location);
        return f("assets/{}/models/{}.json", id.getNamespace(), id.getPath());
    }

    /**
     * Attempts to locate the item model file corresponding to the given block.
     *
     * @param block The background block being researched.
     * @return The path to the item model file.
     */
    private static String getItemModelPath(Block block) {
        return getItemModelPath(Objects.requireNonNull(block.getRegistryName(), "Unregistered block"));
    }

    /**
     * converts a resource location into an <em>item</em> model file.
     *
     * @param id The location of the desired item model.
     * @return The concrete location of this file.
     */
    private static String getItemModelPath(ResourceLocation id) {
        return f("assets/{}/models/item/{}.json", id.getNamespace(), id.getPath());
    }

    /**
     * Attempts to load a file from the jar <b>or</b> resources directory as a JSON object.
     *
     * @param path The <em>concrete</em> path to this file.
     * @return The deserialized JSON data from the disk, if possible.
     */
    private static Optional<JsonObject> loadJson(String path) {
        return getResource(path).flatMap(HjsonTools::readJson);
    }
}
