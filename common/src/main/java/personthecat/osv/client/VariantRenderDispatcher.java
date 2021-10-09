package personthecat.osv.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import personthecat.osv.block.SharedStateBlock;

@Environment(EnvType.CLIENT)
public class VariantRenderDispatcher {

    @ExpectPlatform
    public static void setupRenderLayer(final SharedStateBlock variant) {
        throw new AssertionError();
    }
}
