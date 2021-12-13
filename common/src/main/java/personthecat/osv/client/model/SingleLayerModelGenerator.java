package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import personthecat.catlib.util.HjsonUtils;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.config.VariantDescriptor;

public class SingleLayerModelGenerator implements ModelGenerator {

    public JsonObject generateBlock(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        final JsonObject generated = HjsonUtils.shallowCopy(model.getModel()).asObject();
        final JsonObject newTextures = new JsonObject();
        for (final JsonObject.Member member : generated.get("textures").asObject()) {
            final String texture = member.getValue().asString();
            if (texture.startsWith("#")) {
                newTextures.add(member.getName(), texture);
            } else {
                final ResourceLocation bg = new ResourceLocation(texture);
                ResourceLocation id = TextureHandler.generateSingleLayer(bg, overlay);
                if (id != null) {
                    newTextures.add(member.getName(), id.toString());
                }
            }
        }
        return generated.set("textures", newTextures);
    }
}
