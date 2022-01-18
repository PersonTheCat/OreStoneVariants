package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.OreVariant;
import personthecat.osv.block.SharedStateBlock;

public class InterceptorHandle {

    private OreVariant block;
    private BlockState state;
    private Block expected;
    private BlockPos pos;
    private boolean primed;

    InterceptorHandle() {}

    void intercept(final BlockState state, final Block expected, final @Nullable BlockPos pos) {
        this.block = (OreVariant) state.getBlock();
        this.state = state;
        this.expected = expected;
        this.pos = pos;
        this.primed = true;
    }

    void dispose() {
        this.block = null;
        this.state = null;
        this.expected = null;
        this.pos = null;
        this.primed = false;
    }

    boolean isPrimed() {
        return this.primed;
    }

    Block expose(final @Nullable BlockPos pos, final Block expected) {
        return checkPos(pos) && this.expected == expected ? this.block : expected;
    }

    BlockState expose(final @Nullable BlockPos pos, final BlockState expected) {
        return checkPos(pos) && this.expected == expected.getBlock() ? SharedStateBlock.copyInto(this.state, expected) : expected;
    }

    BlockState disguise(final @Nullable BlockPos pos, final BlockState actual) {
        return checkPos(pos) && this.block == actual.getBlock() ? this.block.asOther(actual, this.expected) : actual;
    }

    private boolean checkPos(final BlockPos pos) {
        return this.pos == null || this.pos.equals(pos);
    }
}
