package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import personthecat.osv.config.VariantDescriptor;

public class SingleLayerModelGenerator implements ModelGenerator {

    public JsonObject generateBlock(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        return new JsonObject().add("hello", "single world");
//        throw new UnsupportedOperationException();
    }
}
