package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import personthecat.osv.preset.data.ModelSettings;

public class OverlayModelGenerator implements ModelGenerator {

    public JsonObject generateVariants(final ModelSettings cfg, final ResourceLocation id) {
        throw new UnsupportedOperationException();
    }

    public void generateBlocks(final ModelSettings cfg, final JsonObject variants, final String path) {
        throw new UnsupportedOperationException();
    }

    public void generateItems(final ModelSettings cfg, final JsonObject variants, final String normalPath) {
        throw new UnsupportedOperationException();
    }
}
