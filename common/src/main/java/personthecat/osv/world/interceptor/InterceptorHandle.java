package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.SharedStateBlock;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class InterceptorHandle<L extends LevelAccessor, T extends TickListHandle<? extends TickList<Block>>> {

    private final L interceptor;
    private final T tickList;
    private WeakReference<L> level;
    private SharedStateBlock block;
    private Block expected;
    private BlockPos pos;
    private boolean primed;

    InterceptorHandle(final L interceptor, final T tickList) {
        this.interceptor = interceptor;
        this.tickList = tickList;
    }

    @SuppressWarnings("unchecked")
    <I extends LevelAccessor> InterceptorHandle<L, T> prime(final I level) {
        this.level = new WeakReference<>((L) level);
        this.tickList.prime(level.getBlockTicks());
        this.primed = true;
        return this;
    }

    InterceptorHandle<L, T> intercept(final SharedStateBlock block, final Block expected) {
        this.block = block;
        this.expected = expected;
        this.tickList.intercept(block, expected);
        return this;
    }

    InterceptorHandle<L, T> onlyAt(final BlockPos pos) {
        this.pos = pos;
        this.tickList.onlyAt(pos);
        return this;
    }

    InterceptorHandle<L, T> dispose() {
        this.block = null;
        this.expected = null;
        this.pos = null;
        this.tickList.dispose();
        this.primed = false;
        return this;
    }

    L getLevel() {
        return Objects.requireNonNull(this.level.get(), "World reference has been culled");
    }

    T getTickList() {
        return this.tickList;
    }

    @SuppressWarnings("unchecked")
    public <I extends LevelAccessor> I getInterceptor() {
        return (I) this.interceptor;
    }

    boolean isPrimed() {
        return this.primed;
    }

    Block expose(final @Nullable BlockPos pos, final Block expected) {
        return checkPos(pos) && this.expected == expected ? this.block : expected;
    }

    BlockState expose(final @Nullable BlockPos pos, final BlockState expected) {
        return checkPos(pos) && this.expected == expected.getBlock() ? this.block.fromEither(expected) : expected;
    }

    BlockState disguise(final @Nullable BlockPos pos, final BlockState actual) {
        return checkPos(pos) && this.block == actual.getBlock() ? this.block.toEither(actual, this.expected) : actual;
    }

    private boolean checkPos(final BlockPos pos) {
        return this.pos == null || this.pos == pos;
    }
}
