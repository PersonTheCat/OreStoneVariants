package personthecat.osv.client.forge;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import personthecat.osv.block.SharedStateBlock;

public class VariantRenderDispatcherImpl {

    public static void setupRenderLayer(final SharedStateBlock variant) {
        final RenderType layer = ItemBlockRenderTypes.getRenderType(variant.defaultBlockState(), true);
        ItemBlockRenderTypes.setRenderLayer(variant, layer);
    }
}
