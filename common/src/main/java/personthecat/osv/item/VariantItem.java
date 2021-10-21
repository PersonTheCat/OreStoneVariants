package personthecat.osv.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.OreVariant;

import static personthecat.osv.block.AdditionalProperties.DENSE;

public class VariantItem extends BlockItem {

    private final BlockState state;

    public VariantItem(final BlockState state, final Properties properties) {
        super(state.getBlock(), properties);
        this.state = state;
    }

    public Block getBg() {
        return ((OreVariant) this.getBlock()).getBg();
    }

    public Block getFg() {
        return ((OreVariant) this.getBlock()).getFg();
    }

    public boolean isDense() {
        return this.state.hasProperty(DENSE) && this.state.getValue(DENSE);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(final BlockPlaceContext ctx) {
        return this.canPlace(ctx, this.state) ? this.state : null;
    }
}
