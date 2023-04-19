package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.config.VariantDescriptor;
import xjs.core.JsonObject;

public class SingleLayerModelGenerator implements ModelGenerator {

    public JsonObject generateBlock(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        final JsonObject generated = model.getModel().shallowCopy().asObject();
        final JsonObject newTextures = new JsonObject();
        for (final JsonObject.Member member : generated.getAsserted("textures").asObject()) {
            final String texture = member.getValue().asString();
            if (texture.startsWith("#")) {
                newTextures.add(member.getKey(), texture);
            } else {
                final ResourceLocation bg = new ResourceLocation(texture);
                ResourceLocation id = TextureHandler.generateSingleLayer(bg, overlay);
                if (id != null) {
                    newTextures.add(member.getKey(), id.toString());
                }
            }
        }
        return generated.set("textures", newTextures);
    }
}
