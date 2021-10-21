package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.OreVariant;

public class TickInterceptorHandle<T extends TickList<Block>> {

    private final T interceptor;
    private T list;
    private OreVariant block;
    private Block expected;
    private BlockPos pos;

    TickInterceptorHandle(final T interceptor) {
        this.interceptor = interceptor;
    }

    @SuppressWarnings("unchecked")
    <I extends TickList<Block>> void prime(final I list) {
        this.list = (T) list;
    }

    void intercept(final OreVariant block, final Block expected) {
        this.block = block;
        this.expected = expected;
    }

    void at(final BlockPos pos) {
        this.pos = pos;
    }

    void dispose() {
        this.block = null;
        this.expected = null;
        this.pos = null;
    }

    T getInterceptor() {
        return this.interceptor;
    }

    T getList() {
        return this.list;
    }

    Block expose(final @Nullable BlockPos pos, final Block expected) {
        return checkPos(pos) && this.expected == expected ? this.block : expected;
    }

    private boolean checkPos(final BlockPos pos) {
        return this.pos == null || this.pos == pos;
    }
}
