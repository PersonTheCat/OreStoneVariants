package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.error.Severity;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.HjsonUtilsMod;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.client.blockstate.BlockStateLoader;
import personthecat.osv.client.blockstate.VariantWrapper;
import personthecat.osv.exception.ModelResolutionException;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.util.ArrayList;
import java.util.List;
import static personthecat.catlib.util.PathUtilsMod.asModelPath;

public class ModelLoader {

    public static StateMap<List<ModelWrapper>> getDefinitions(final ResourceLocation id) {
        return BlockStateLoader.getModel(id).mapTo(wrappers ->
            resolve(wrappers, w -> w.withDefinition(getCompleteModel(w))));
    }

    public static StateMap<List<JsonObject>> getModels(final ResourceLocation id) {
        return BlockStateLoader.getModel(id).mapTo(wrappers -> resolve(wrappers, ModelLoader::getCompleteModel));
    }

    private static <T> List<T> resolve(final List<VariantWrapper> wrappers, final WrapperFunction<T> getter) {
        final List<T> resolved = new ArrayList<>();
        for (final VariantWrapper wrapper : wrappers) {
            try {
                resolved.add(getter.apply(wrapper));
            } catch (final ModelResolutionException e) {
                LibErrorContext.registerSingle(Severity.WARN, Reference.MOD_NAME, e);
            }
        }
        return resolved;
    }

    private static JsonObject getCompleteModel(final VariantWrapper wrapper) throws ModelResolutionException {
        return simplifyTextures(resolveCompletely(getModelDefinition(wrapper.getModel())));
    }

    private static JsonObject resolveCompletely(final JsonObject model) throws ModelResolutionException {
        final JsonValue parentValue = model.get("parent");
        if (parentValue != null) {
            final ResourceLocation id = new ResourceLocation(parentValue.asString());
            final JsonObject parentObject = resolveCompletely(getModelDefinition(id));
            copyRecursively(model, parentObject);
            return parentObject;
        }
        return model;
    }

    private static JsonObject getModelDefinition(final ResourceLocation model) throws ModelResolutionException {
        return ClientResourceHelper.locateResource(asModelPath(model)).flatMap(HjsonUtilsMod::readSuppressing)
            .orElseThrow(() -> new ModelResolutionException(model));
    }

    private static void copyRecursively(final JsonObject from, final JsonObject to) {
        for (final JsonObject.Member member : from) {
            final JsonValue value = member.getValue();
            if (value.isObject()) {
                final JsonValue original = to.get(member.getName());
                if (original != null && original.isObject()) {
                    copyRecursively(value.asObject(), original.asObject());
                    continue;
                }
            }
            to.set(member.getName(), value);
        }
    }

    private static JsonObject simplifyTextures(final JsonObject model) {
        final JsonValue texturesValue = model.get("textures");
        if (texturesValue == null || texturesValue.asObject().isEmpty()) {
            return model;
        }
        final JsonObject textures = texturesValue.asObject();
        for (final JsonObject element : HjsonUtils.getObjectArray(model, "elements")) {
            final JsonValue facesValue = element.get("faces");
            if (facesValue == null) continue;

            for (final JsonObject.Member member : facesValue.asObject()) {
                final JsonObject face = member.getValue().asObject();
                final JsonValue texture = face.get("texture");
                if (texture == null) continue;

                final String key = texture.asString();
                if (key.startsWith("#")) {
                    final String actual = getActualTexture(textures, key.substring(1));
                    if (actual != null) {
                        face.set("texture", actual);
                    }
                }
            }
        }
        return model.remove("parent").remove("textures");
    }

    @Nullable
    private static String getActualTexture(final JsonObject textures, final String key) {
        final JsonValue textureValue = textures.get(key);
        if (textureValue == null) {
            return null;
        }
        final String newKey = textureValue.asString();
        if (newKey.startsWith("#")) {
            return getActualTexture(textures, newKey.substring(1));
        }
        return newKey;
    }

    @FunctionalInterface
    private interface WrapperFunction<T> {
        T apply(final VariantWrapper wrapper) throws ModelResolutionException;
    }
}
