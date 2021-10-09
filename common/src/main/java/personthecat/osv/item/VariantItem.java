package personthecat.osv.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import personthecat.osv.block.SharedStateBlock;

public class VariantItem extends BlockItem {

    private final ItemType type;

    // Todo: Custom item properties?
    public VariantItem(final SharedStateBlock variant, final ItemType type) {
        super(variant, new Properties().tab(CreativeModeTab.TAB_MATERIALS));
        this.type = type;
    }

    public Block getBg() {
        return ((SharedStateBlock) this.getBlock()).getBg();
    }

    public Block getFg() {
        return ((SharedStateBlock) this.getBlock()).getFg();
    }

    public ItemType getType() {
        return this.type;
    }

    public boolean isDense() {
        return this.type == ItemType.DENSE;
    }
}
