package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.OreVariant;
import personthecat.osv.block.SharedStateBlock;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class InterceptorHandle<L extends LevelAccessor, T extends TickInterceptorHandle<? extends TickList<Block>>> {

    private final L interceptor;
    private final T tickList;
    private WeakReference<L> level;
    private OreVariant block;
    private BlockState state;
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

    public InterceptorHandle<L, T> intercept(final BlockState state, final Block expected) {
        this.block = (OreVariant) state.getBlock();
        this.state = state;
        this.expected = expected;
        this.tickList.intercept(block, expected);
        return this;
    }

    public InterceptorHandle<L, T> at(final BlockPos pos) {
        this.pos = pos;
        this.tickList.at(pos);
        return this;
    }

    void dispose() {
        this.block = null;
        this.expected = null;
        this.pos = null;
        this.tickList.dispose();
        this.primed = false;
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
        return checkPos(pos) && this.expected == expected.getBlock() ? SharedStateBlock.copyInto(this.state, expected) : expected;
    }

    BlockState disguise(final @Nullable BlockPos pos, final BlockState actual) {
        return checkPos(pos) && this.block == actual.getBlock() ? this.block.asOther(actual, this.expected) : actual;
    }

    private boolean checkPos(final BlockPos pos) {
        return this.pos == null || this.pos == pos;
    }
}
