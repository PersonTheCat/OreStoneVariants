package personthecat.osv.preset.resolver;

import net.minecraft.resources.ResourceLocation;
import personthecat.osv.client.model.ModelLoader;
import personthecat.osv.client.texture.BackgroundSelector;
import personthecat.osv.util.StateMap;
import xjs.core.JsonObject;
import xjs.core.JsonValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TextureResolver {

    public static StateMap<List<ResourceLocation>> resolveOriginals(final ResourceLocation id) {
        return ModelLoader.getModels(id).mapTo(TextureResolver::getAllTextures);
    }

    public static ResourceLocation resolveBackground(final StateMap<List<ResourceLocation>> images) {
        for (final Map.Entry<String, List<ResourceLocation>> entry : images.asRaw().entrySet()) {
            for (final ResourceLocation id : entry.getValue()) {
                return BackgroundSelector.getClosestMatch(id);
            }
        }
        return BackgroundSelector.STONE_ID;
    }

    private static List<ResourceLocation> getAllTextures(final List<JsonObject> models) {
        final Set<ResourceLocation> textures = new HashSet<>();
        for (final JsonObject model : models) {
            addTexturesFromModel(textures, model);
        }
        return new ArrayList<>(textures);
    }

    private static void addTexturesFromModel(final Set<ResourceLocation> textures, final JsonObject model) {
        final JsonValue texturesValue = model.get("textures");
        if (texturesValue != null) {
            for (final JsonObject.Member texture : texturesValue.asObject()) {
                final JsonValue value = texture.getValue();
                if (value.isString() && !value.asString().startsWith("#")) {
                    final ResourceLocation parsed = ResourceLocation.tryParse(value.asString());
                    if (parsed != null) {
                        textures.add(parsed);
                    }
                }
            }
        }
    }
}
