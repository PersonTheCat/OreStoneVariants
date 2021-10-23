package personthecat.osv.item.forge;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import personthecat.osv.ModRegistries;

public class VariantTabImpl extends CreativeModeTab {

    private VariantTabImpl() {
        super("variants");
    }

    public static VariantTabImpl createInstance() {
        return new VariantTabImpl();
    }

    @Override
    public ItemStack makeIcon() {
        final Block block = ModRegistries.VARIANTS
            .findByValue(v -> v.getFg().equals(Blocks.COAL_ORE) && v.getBg().equals(Blocks.DIORITE))
            .orElseGet(() -> ModRegistries.VARIANTS.iterator().next());
        return new ItemStack(block);
    }
}
