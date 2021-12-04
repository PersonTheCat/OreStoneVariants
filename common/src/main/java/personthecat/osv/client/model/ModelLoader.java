package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.error.Severity;
import personthecat.catlib.util.HjsonUtils;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.client.blockstate.BlockStateLoader;
import personthecat.osv.client.blockstate.VariantWrapper;
import personthecat.osv.exception.ModelResolutionException;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.util.ArrayList;
import java.util.List;
import static personthecat.catlib.util.PathUtils.asModelPath;

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
                LibErrorContext.registerSingle(Severity.WARN, Reference.MOD_DESCRIPTOR, e);
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
        return ClientResourceHelper.locateResource(asModelPath(model)).flatMap(HjsonUtils::readSuppressing)
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
            return model.remove("parent").add("textures", new JsonObject());
        }
        final JsonObject newTextures = new JsonObject();
        for (final JsonObject.Member texture : texturesValue.asObject()) {
            if ("particle".equals(texture.getName())) {
                newTextures.add(texture.getName(), texture.getValue());
            }
            final String value = texture.getValue().asString();
            if (value.startsWith("#")) {
                replaceTexture(model, "#" + texture.getName(), value);
            } else {
                newTextures.add(texture.getName(), texture.getValue());
            }
        }
        return model.remove("parent").set("textures", newTextures);
    }

    private static void replaceTexture(final JsonObject model, final String from, final String to) {
        for (final JsonObject element : HjsonUtils.getObjectArray(model, "elements")) {
            final JsonValue facesValue = element.get("faces");
            if (facesValue == null) continue;

            for (final JsonObject.Member member : facesValue.asObject()) {
                final JsonObject face = member.getValue().asObject();
                final JsonValue texture = face.get("texture");
                if (texture == null) continue;

                final String key = texture.asString();
                if (from.equals(key)) {
                    face.set("texture", to);
                }
            }
        }
    }

    @FunctionalInterface
    private interface WrapperFunction<T> {
        T apply(final VariantWrapper wrapper) throws ModelResolutionException;
    }
}
