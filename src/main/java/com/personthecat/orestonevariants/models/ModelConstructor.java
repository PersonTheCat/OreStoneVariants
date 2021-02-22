package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.util.HjsonTools;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.personthecat.orestonevariants.io.SafeFileIO.getResource;
import static com.personthecat.orestonevariants.util.CommonMethods.error;
import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.HjsonTools.getObject;

public class ModelConstructor {

    private static final JsonObject CUBE_ALL = loadModelFile(new ResourceLocation("block/cube_all"));

    private static final String MODEL_KEY = "model";
    private static final String ELEMENTS_KEY = "elements";
    private static final String PARENT_KEY = "parent";
    private static final String VARIANTS_KEY = "variants";

    // Generates a new set of block models from fg and bg with osv state properties.
    public static void generateModels(Block bg, Map<String, String> overlays) {
        final JsonObject bgVariants = loadBlockStateFile(bg)
            .flatMap(ModelConstructor::getVariants)
            .orElseGet(JsonObject::new);
        generateModelsFromVariants(expandModels(bg, bgVariants), overlays);
    }

    private static void generateModelsFromVariants(JsonObject bgExpanded, Map<String, String> overlays) {
        final JsonObject blockState = new JsonObject();

        for (JsonObject.Member bg : bgExpanded) {
            for (Map.Entry<String, String> fg : overlays.entrySet()) {
                // Todo
            }
        }
    }

    // Expands all of the models in a block state definition with concrete models.
    private static JsonObject expandModels(Block bg, JsonObject variants) {
        for (JsonObject.Member member : variants) {
            final JsonValue value = member.getValue();
            if (value.isArray()) {
                for (JsonValue v2 : value.asArray()) {
                    if (v2.isObject()) {
                        expandVariant(bg, v2.asObject());
                    } else {
                        error("Unexpected data in variant array of {}. Skipping.", bg);
                    }
                }
            } else if (value.isObject()) {
                expandVariant(bg, value.asObject());
            } else {
                error("Unexpected data in {}. Skipping {}", bg, member.getName());
            }
        }
        return variants;
    }

    // Replaces a model path in a block state variant with a concrete model.
    private static void expandVariant(Block bg, JsonObject variant) {
        final JsonValue model = variant.get(MODEL_KEY);
        if (model != null && model.isString()) {
            final ResourceLocation id = new ResourceLocation(model.asString());
            variant.set(MODEL_KEY, loadFullModel(id));
        } else {
            error("Missing or unexpected model in {}. Skipping.", bg);
        }
    }

    // Loads parent models recursively until "elements" is present.
    private static JsonObject loadFullModel(ResourceLocation id) {
        final JsonObject model = loadModelFile(id);
        if (!model.has(ELEMENTS_KEY)) {
            final JsonValue parent = model.get(PARENT_KEY);
            if (parent == null || !parent.isString()) {
                throw runExF("Invalid model: {}", id);
            }
            return applyOverrides(model, loadFullModel(new ResourceLocation(parent.asString())));
        }
        return model;
    }

    // Recursively applies all possible overrides from json into parent.
    private static JsonObject applyOverrides(JsonObject json, JsonObject parent) {
        for (JsonObject.Member member : json) {
            final String name = member.getName();
            final JsonValue value = member.getValue();
            if (value.isObject()) {
                final JsonValue parentValue = parent.get(name);
                if (parentValue == null ) {
                    parent.add(name, value);
                } else if (!parentValue.isObject()) {
                    throw runEx("Parent model doesn't match child.");
                } else {
                    applyOverrides(value.asObject(), parentValue.asObject());
                }
            } else {
                parent.set(name, value);
            }
        }
        return parent;
    }

    // This is possible now that all states use the same mapper.
    private static Optional<JsonObject> loadBlockStateFile(Block block) {
        final ResourceLocation id = Objects.requireNonNull(block.getRegistryName(), "Unregistered block");
        return loadJson(getBlockStatePath(id));
    }

    private static JsonObject loadModelFile(ResourceLocation id) {
        return loadJson(getModelPath(id)).orElseThrow(() -> runExF("Expected model @ {}", id));
    }

    private static String getBlockStatePath(ResourceLocation id) {
        return f("assets/{}/blockstates/{}.json", id.getNamespace(), id.getPath());
    }

    private static String getModelPath(ResourceLocation id) {
        return f("assets/{}/models/{}.json", id.getNamespace(), id.getPath());
    }

    private static Optional<JsonObject> getVariants(JsonObject json) {
        return getObject(json, VARIANTS_KEY);
    }

    private static Optional<JsonObject> loadJson(String path) {
        return getResource(path).flatMap(HjsonTools::readJson);
    }
}
