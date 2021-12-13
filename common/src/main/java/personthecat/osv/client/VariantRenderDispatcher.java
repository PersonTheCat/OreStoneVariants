package personthecat.osv.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.VariantDescriptor;

@Environment(EnvType.CLIENT)
public class VariantRenderDispatcher {

    @ExpectPlatform
    public static void setupRenderLayer(final VariantDescriptor descriptor, final OreVariant variant) {
        throw new AssertionError();
    }
}
