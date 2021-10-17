package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.HjsonUtilsMod;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.config.VariantDescriptor;

public class SingleLayerModelGenerator implements ModelGenerator {

    public JsonObject generateBlock(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        final JsonObject generated = HjsonUtilsMod.shallowCopy(model.getModel()).asObject();
        for (final JsonObject element : HjsonUtils.getObjectArray(generated, "elements")) {
            final JsonValue facesValue = element.get("faces");
            if (facesValue == null) continue;

            for (final JsonObject.Member member : facesValue.asObject()) {
                final JsonObject face = member.getValue().asObject();
                final JsonValue textureValue = face.get("texture");
                if (textureValue == null) continue;

                final ResourceLocation bg = new ResourceLocation(textureValue.asString());
                final ResourceLocation id = TextureHandler.generateSingleLayer(bg, overlay);
                if (id != null) face.set("texture", id.toString());
            }
        }
        return this.setParticle(generated, model.getId(), overlay);
    }

    private JsonObject setParticle(final JsonObject generated, final ResourceLocation model, final ResourceLocation overlay) {
        final JsonObject textures = HjsonUtils.getObjectOrNew(generated, "textures");

        ResourceLocation texture = TextureHandler.generateSingleLayer(model, overlay);
        if (texture == null) texture = getFirstTexture(generated);
        if (texture == null) return generated;

        textures.set("particle", texture.toString());

        return generated;
    }

    @Nullable
    private static ResourceLocation getFirstTexture(final JsonObject generated) {
        for (final JsonObject element : HjsonUtils.getObjectArray(generated, "elements")) {
            final JsonValue facesValue = element.get("faces");
            if (facesValue == null) continue;

            for (final JsonObject.Member member : facesValue.asObject()) {
                final JsonObject face = member.getValue().asObject();
                final JsonValue textureValue = face.get("texture");
                if (textureValue != null) {
                    return new ResourceLocation(textureValue.asString());
                }
            }
        }
        return null;
    }
}
