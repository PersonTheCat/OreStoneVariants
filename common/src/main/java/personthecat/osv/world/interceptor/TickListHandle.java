package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.SharedStateBlock;

public class TickListHandle<T extends TickList<Block>> {

    private final T interceptor;
    private T list;
    private SharedStateBlock block;
    private Block expected;
    private BlockPos pos;

    TickListHandle(final T interceptor) {
        this.interceptor = interceptor;
    }

    @SuppressWarnings("unchecked")
    <I extends TickList<Block>> void prime(final I list) {
        this.list = (T) list;
    }

    void intercept(final SharedStateBlock block, final Block expected) {
        this.block = block;
        this.expected = expected;
    }

    void onlyAt(final BlockPos pos) {
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
