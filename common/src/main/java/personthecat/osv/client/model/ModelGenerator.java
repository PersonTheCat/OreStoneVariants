package personthecat.osv.client.model;

import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import personthecat.osv.preset.data.ModelSettings;

public interface ModelGenerator {
    JsonObject generateVariants(final ModelSettings cfg, final ResourceLocation id);
    void generateBlocks(final ModelSettings cfg, final JsonObject variants, final String path);
    void generateItems(final ModelSettings cfg, final JsonObject variants, final String normalPath);
}
