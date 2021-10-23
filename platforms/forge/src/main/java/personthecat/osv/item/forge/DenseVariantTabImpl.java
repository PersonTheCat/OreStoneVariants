package personthecat.osv.item.forge;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.item.VariantItem;

public class DenseVariantTabImpl extends CreativeModeTab {

    private DenseVariantTabImpl() {
        super("dense_variants");
    }

    public static DenseVariantTabImpl createInstance() {
        return new DenseVariantTabImpl();
    }

    @Override
    public ItemStack makeIcon() {
        final VariantItem item = ModRegistries.ITEMS
            .findByValue(v -> v.getFg().equals(Blocks.DIAMOND_ORE) && v.getBg().equals(Blocks.GRANITE))
            .orElseGet(DenseVariantTabImpl::firstDense);
        return new ItemStack(item);
    }

    private static VariantItem firstDense() {
        for (final VariantItem item : ModRegistries.ITEMS) {
            final BlockState state = item.getState();
            if (state.hasProperty(AdditionalProperties.DENSE) && state.getValue(AdditionalProperties.DENSE)) {
                return item;
            }
        }
        return ModRegistries.ITEMS.iterator().next();
    }
}
