package personthecat.osv.preset.resolver;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.PathUtils;
import personthecat.osv.client.model.ModelLoader;
import personthecat.osv.client.texture.BackgroundSelector;
import personthecat.osv.util.StateMap;

import java.util.*;

import static personthecat.catlib.util.Shorthand.f;

public class TextureResolver {

    private static final String[] COMMON_TEXTURE_PATHS = {
        "block/{}", "block/ore/{}", "block/ores/{}", "block/{}_vanilla", "blocks/{}", "blocks/ore/{}",
        "blocks/ores/{}", "blocks/{}_vanilla", "/ore/{}", "/ores/{}", "{}"
    };

    public static StateMap<List<ResourceLocation>> resolveOriginals(final ResourceLocation id) {
        final ResourceLocation guess = guessTexture(id);
        if (guess != null) {
            return new StateMap<>(Collections.singletonMap("", Collections.singletonList(guess)));
        }
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

    @Nullable
    private static ResourceLocation guessTexture(final ResourceLocation id) {
        for (final String template : COMMON_TEXTURE_PATHS) {
            final String key = f(template, id.getPath());
            final String path = PathUtils.asTexturePath(id.getNamespace(), key);
            if (FileIO.resourceExists(path)) {
                return new ResourceLocation(id.getNamespace(), key);
            }
        }
        return null;
    }

    private static List<ResourceLocation> getAllTextures(final List<JsonObject> models) {
        final Set<ResourceLocation> textures = new HashSet<>();
        for (final JsonObject model : models) {
            addTexturesFromModel(textures, model);
        }
        return new ArrayList<>(textures);
    }

    private static void addTexturesFromModel(final Set<ResourceLocation> textures, final JsonObject model) {
        for (final JsonObject element : HjsonUtils.getObjectArray(model, "elements")) {
            final JsonValue faces = element.get("faces");
            if (faces == null) continue;

            for (final JsonObject.Member member : faces.asObject()) {
                final JsonObject face = member.getValue().asObject();
                final JsonValue texture = face.get("texture");

                if (texture != null) {
                    final ResourceLocation parsed = ResourceLocation.tryParse(texture.asString());
                    if (parsed != null) {
                        textures.add(parsed);
                    }
                }
            }
        }
    }
}
