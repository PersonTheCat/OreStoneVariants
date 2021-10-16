package personthecat.osv.client.model.forge;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import personthecat.osv.client.model.ModelWrapper;
import personthecat.osv.config.VariantDescriptor;

public class OverlayModelGeneratorImpl {
    public static JsonObject platformModel(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        return new JsonObject().add("hello", "multi world");
    }
}
