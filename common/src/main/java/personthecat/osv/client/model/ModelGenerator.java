package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.PathUtils;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.io.OsvPaths;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface ModelGenerator {

    default void generateModels(VariantDescriptor cfg, String path, BiConsumer<String, JsonObject> writer) {
        final JsonObject variants = new JsonObject();

        ModelLoader.getDefinitions(cfg.getBackground()).forEach((key, wrappers) ->
            cfg.getForeground().getVariantModels().forEach((variant, overlays) -> {
                final List<ModelWrapper> blocks = this.generateBlocks(cfg, wrappers, overlays);
                blocks.forEach(block -> writer.accept(PathUtils.asModelPath(block.getId()), block.getModel()));

                final JsonArray array = this.generateVariants(blocks);
                variants.add(StateMap.addVariant(key, variant), array.size() == 1 ? array.get(0) : array);
            })
        );
        this.generateItems(cfg, variants, writer);
        writer.accept(path, new JsonObject().add("variants", variants));
    }

    default List<ModelWrapper> generateBlocks(VariantDescriptor cfg, List<ModelWrapper> wrappers, List<ResourceLocation> overlays) {
        final List<ModelWrapper> generated = new ArrayList<>();
        for (final ModelWrapper wrapper : wrappers) {
            for (final ResourceLocation overlay : overlays) {
                final ResourceLocation id = this.generateBlockId(wrapper, overlay);
                final JsonObject block = this.generateBlock(cfg, wrapper, overlay);
                generated.add(new ModelWrapper(id, block, wrapper.getSettings()));
            }
        }
        return generated;
    }

    default ResourceLocation generateBlockId(ModelWrapper original, ResourceLocation overlay) {
        final String path = overlay.getPath() + "_" + this.createPrefix(original.getId());
        return new ResourceLocation(Reference.MOD_ID, path);
    }

    default JsonArray generateVariants(List<ModelWrapper> generated) {
        final JsonArray array = new JsonArray();
        for (final ModelWrapper wrapper : generated) {
            final JsonObject variant = new JsonObject();
            variant.addAll(wrapper.getSettings());
            variant.add("model", wrapper.getId().toString());
            array.add(variant);
        }
        return array;
    }

    JsonObject generateBlock(VariantDescriptor cfg, ModelWrapper model, ResourceLocation overlay);

    default void generateItems(VariantDescriptor cfg, JsonObject variants, BiConsumer<String, JsonObject> writer) {
        final String path = getItemPath(cfg.getId());
        final String model = this.loadModel(this.getItemPath(cfg.getBackground()))
            .flatMap(item -> this.getMatchingModel(cfg, item))
            .orElseGet(() -> this.getFirstModel(variants));

        if (model != null) {
            writer.accept(path, this.generateItem(model));

            final StateMap<String> itemVariants = cfg.getForeground().getItemVariants();
            if (itemVariants.isEmpty()) return;

            final String normalVariant = this.getVariantOf(variants, model);
            if (normalVariant == null) return;

            itemVariants.forEach((variant, affix) -> {
                if (!affix.isEmpty()) {
                    final String matching = this.getFirstModelMatching(variants, normalVariant, variant);
                    if (matching != null) {
                        writer.accept(PathUtils.prependFilename(path, affix + "_"), this.generateItem(matching));
                    }
                }
            });
        }
    }

    default String getItemPath(ResourceLocation id) {
        return PathUtils.asModelPath(id.getNamespace(), "item/" + id.getPath());
    }

    default Optional<JsonObject> loadModel(String path) {
        return ClientResourceHelper.locateResource(path).flatMap(HjsonUtils::readSuppressing);
    }

    default Optional<String> getMatchingModel(VariantDescriptor cfg, JsonObject item) {
        final String parent = item.getString("parent", null);
        if (parent == null) return Optional.empty();

        final String prefix = this.createPrefix(cfg.getForeground().getPrimaryModel());
        final String equivalent = OsvPaths.fromForeign(new ResourceLocation(parent), prefix);
        final ResourceLocation id = new ResourceLocation(Reference.MOD_ID, equivalent);
        final String path = PathUtils.asModelPath(id);

        if (ClientResourceHelper.hasResource(path)) {
            return Optional.of(id.toString());
        }
        return Optional.empty();
    }

    @Nullable
    default String getFirstModel(JsonObject variants) {
        for (final JsonObject.Member variant : variants) {
            JsonValue value = variant.getValue();
            if (variant.getName().isEmpty() || variant.getName().contains("dense=false")) {
                if (value.isArray() && value.asArray().size() > 0) {
                    value = value.asArray().get(0);
                }
                if (value.isObject()) {
                    final JsonValue model = value.asObject().get("model");
                    if (model != null && model.isString()) {
                        return model.asString();
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    default String getVariantOf(JsonObject variants, String model) {
        for (final JsonObject.Member variant : variants) {
            for (final JsonValue config : HjsonUtils.asOrToArray(variant.getValue())) {
                if (config.isObject()) {
                    final JsonValue m = config.asObject().get("model");
                    if (m.isString() && m.asString().equals(model)) {
                        return variant.getName();
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    default String getFirstModelMatching(JsonObject variants, String normalVariant, String kv) {
        final String newVariant = StateMap.setVariant(normalVariant, kv);
        if (newVariant == null) return null;

        for (final JsonObject.Member variant : variants) {
            if (StateMap.matches(variant.getName(), newVariant)) {
                for (final JsonValue value : HjsonUtils.asOrToArray(variant.getValue())) {
                    if (value.isObject()) {
                        final JsonValue model = value.asObject().get("model");
                        if (model != null && model.isString()) {
                            return model.asString();
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }

    default String createPrefix(ResourceLocation id) {
        final String end = PathUtils.endOfPath(id);
        if ("minecraft".equals(id.getNamespace()) || "osv".equals(id.getNamespace())) {
            return end;
        }
        return id.getNamespace() + "_" + end;
    }

    default JsonObject generateItem(String id) {
        return new JsonObject().add("parent", id);
    }
}
