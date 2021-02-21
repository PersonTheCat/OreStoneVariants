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
import static com.personthecat.orestonevariants.util.HjsonTools.getObject;

public class ModelConstructor {

    private static final JsonObject CUBE_ALL = loadModelFile(new ResourceLocation("block/cube_all"))
        .orElseThrow(() -> runEx("Build error: Invalid cube_all path."));

    private static final String MODEL_KEY = "model";

    // Generates a new set of block models from fg and bg with osv state properties.
    private static void generateModels(Block bg, Map<String, String> overlays) {
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

    private static JsonObject expandModels(Block bg, JsonObject variants) {
        for (JsonObject.Member member : variants) {
            if (member.getValue().isObject()) {
                final JsonObject object = member.getValue().asObject();
                final JsonValue model = object.get(MODEL_KEY);
                if (model != null && model.isString()) {
                    final ResourceLocation id = new ResourceLocation(model.asString());
                    object.set(MODEL_KEY, loadFullModel(id));
                } else {
                    error("Missing or unexpected model in {}. Skipping.", bg);
                }
            } else {
                error("Unexpected data in {}. Skipping {}", bg, member.getName());
            }
        }
        return variants;
    }

    private static JsonObject loadFullModel(ResourceLocation id) {
        throw new UnsupportedOperationException(); // Todo
    }

    // This is possible now that all states use the same mapper.
    private static Optional<JsonObject> loadBlockStateFile(Block block) {
        final ResourceLocation id = Objects.requireNonNull(block.getRegistryName(), "Unregistered block");
        return loadJson(getBlockStatePath(id));
    }

    private static Optional<JsonObject> loadModelFile(ResourceLocation id) {
        return loadJson(getModelPath(id));
    }

    private static String getBlockStatePath(ResourceLocation id) {
        return f("assets/{}/blockstates/{}.json", id.getNamespace(), id.getPath());
    }

    private static String getModelPath(ResourceLocation id) {
        return f("assets/{}/models/{}.json", id.getNamespace(), id.getPath());
    }

    private static Optional<JsonObject> getVariants(JsonObject json) {
        return getObject(json, "variants");
    }

    private static Optional<JsonObject> loadJson(String path) {
        return getResource(path).flatMap(HjsonTools::readJson);
    }
}
