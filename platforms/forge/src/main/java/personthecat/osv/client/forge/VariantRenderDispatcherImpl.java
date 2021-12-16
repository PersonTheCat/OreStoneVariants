package personthecat.osv.client.forge;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.preset.data.ModelSettings;

public class VariantRenderDispatcherImpl {

    @SuppressWarnings("deprecation")
    public static void setupRenderLayer(final VariantDescriptor descriptor, final OreVariant variant) {
        final RenderType layer = ItemBlockRenderTypes.getChunkRenderType(variant.getBg().defaultBlockState());
        if (descriptor.getForeground().getModel().getType() == ModelSettings.Type.OVERLAY) {
            ItemBlockRenderTypes.setRenderLayer(variant, l -> l == layer || l == RenderType.translucent());
        }
        ItemBlockRenderTypes.setRenderLayer(variant, layer);
    }
}
