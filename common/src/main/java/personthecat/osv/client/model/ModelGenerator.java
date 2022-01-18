package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.PathUtils;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.client.texture.Modifier;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.util.*;
import java.util.function.BiConsumer;

public interface ModelGenerator {

    ResourceLocation ITEM_GENERATED = new ResourceLocation("item/generated");

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
        final JsonObject item = this.loadModel(this.getItemPath(cfg.getBackground())).orElse(null);
        if (item != null && this.isSpecialItem(item)) {
            this.generateLayeredItems(cfg, item, writer);
        } else {
            String model = null;
            if (item != null) {
                model = this.getMatchingModel(cfg, item);
            }
            if (model == null) {
                model = this.getFirstModel(variants);
            }
            if (model != null) {
                this.generateItemsFor(cfg, variants, model, writer);
            }
        }
    }

    default String getItemPath(ResourceLocation id) {
        return PathUtils.asModelPath(id.getNamespace(), "item/" + id.getPath());
    }

    default Optional<JsonObject> loadModel(String path) {
        return ClientResourceHelper.locateResource(path).flatMap(HjsonUtils::readSuppressing);
    }

    default boolean isSpecialItem(JsonObject item) {
        final String parent = item.getString("parent", null);
        if (parent != null) {
            return ITEM_GENERATED.equals(new ResourceLocation(parent));
        }
        return false;
    }

    default void generateLayeredItems(VariantDescriptor cfg, JsonObject item, BiConsumer<String, JsonObject> writer) {
        final String path = getItemPath(cfg.getId());
        final Map<String, String> layers = this.extractLayers(item);

        final ResourceLocation primary = cfg.getForeground().getPrimaryTexture();
        writer.accept(path, this.generateLayeredItem(layers, primary));

        cfg.getForeground().getOverlayModifiers().forEach((variant, modifiers) -> {
            final String affix = cfg.getForeground().getItemVariants().getExactly(variant);
            if (affix != null && !affix.isEmpty()) {
                final String texturePath = PathUtils.appendFilename(primary.getPath(), "_" + Modifier.format(modifiers));
                final JsonObject model = this.generateLayeredItem(layers, new ResourceLocation(Reference.MOD_ID, texturePath));
                writer.accept(PathUtils.prependFilename(path, affix + "_"), model);
            }
        });
    }

    default Map<String, String> extractLayers(JsonObject item) {
        final JsonValue textures = item.get("textures");
        if (textures == null || !textures.isObject()) {
            return Collections.emptyMap();
        }
        final Map<String, String> layers = new HashMap<>();
        for (final JsonObject.Member texture : textures.asObject()) {
            if (texture.getName().startsWith("layer")) {
                layers.put(texture.getName(), texture.getValue().asString());
            }
        }
        return layers;
    }

    default JsonObject generateLayeredItem(Map<String, String> layers, ResourceLocation fg) {
        final JsonObject textures = new JsonObject();
        int layerNumber = 0;

        for (final Map.Entry<String, String> layer : layers.entrySet()) {
            final ResourceLocation bg = new ResourceLocation(layer.getValue());
            final ResourceLocation single = TextureHandler.generateSingleLayer(bg, fg);
            if (single != null) {
                textures.add(this.getLayerKey(layer.getKey(), layerNumber), single.toString());
                layerNumber++;
            }
        }
        return new JsonObject()
            .add("parent", "item/generated")
            .add("textures", textures);
    }

    default String getLayerKey(final String key, final int number) {
        return key.startsWith("layer") ? "layer" + number : key;
    }

    default void generateItemsFor(
            VariantDescriptor cfg, JsonObject variants, String model, BiConsumer<String, JsonObject> writer) {

        final String path = getItemPath(cfg.getId());
        writer.accept(path, this.generateItem(model));

        this.forEachVariant(cfg, variants, model, (normal, variant, affix) -> {
            final String matching = this.getFirstModelMatching(variants, normal, variant);
            if (matching != null) {
                writer.accept(PathUtils.prependFilename(path, affix + "_"), this.generateItem(matching));
            }
        });
    }

    default void forEachVariant(VariantDescriptor cfg, JsonObject variants, String model, TriConsumer<String, String, String> fn) {
        final StateMap<String> itemVariants = cfg.getForeground().getItemVariants();
        if (itemVariants.isEmpty()) return;

        final String normalVariant = this.getVariantOf(variants, model);
        if (normalVariant == null) return;

        itemVariants.forEach((variant, affix) -> {
            if (!affix.isEmpty()) {
                fn.accept(normalVariant, variant, affix);
            }
        });
    }

    @Nullable
    default String getMatchingModel(VariantDescriptor cfg, JsonObject item) {
        final String parent = item.getString("parent", null);
        if (parent == null) return null;

        final String prefix = cfg.getForeground().getPrimaryModel().getPath();
        final String equivalent = prefix + "_" + this.createPrefix(new ResourceLocation(parent));
        final ResourceLocation id = new ResourceLocation(Reference.MOD_ID, equivalent);
        final String path = PathUtils.asModelPath(id);

        if (ClientResourceHelper.hasResource(path)) {
            return id.toString();
        }
        return null;
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

    default JsonObject generateItem(String id) {
        return new JsonObject().add("parent", id);
    }

    default String createPrefix(ResourceLocation id) {
        final String end = PathUtils.endOfPath(id);
        if ("minecraft".equals(id.getNamespace()) || "osv".equals(id.getNamespace())) {
            return end;
        }
        return id.getNamespace() + "_" + end;
    }
}
