package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import personthecat.osv.block.OreVariant;
import personthecat.osv.block.SharedStateBlock;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class DummyInterceptorHandle<L extends LevelAccessor> extends InterceptorHandle<L, TickInterceptorHandle<TickList<Block>>> {

    private final WeakReference<L> level;

    DummyInterceptorHandle(final L level) {
        super(null, null);
        this.level = new WeakReference<>(level);
    }

    @Override
    <I extends LevelAccessor> InterceptorHandle<L, TickInterceptorHandle<TickList<Block>>> prime(final I level) {
        return this;
    }

    @Override
    public InterceptorHandle<L, TickInterceptorHandle<TickList<Block>>> intercept(final OreVariant block, final Block expected) {
        return this;
    }

    @Override
    public InterceptorHandle<L, TickInterceptorHandle<TickList<Block>>> at(final BlockPos pos) {
        return this;
    }

    void dispose() {}

    @SuppressWarnings("unchecked")
    public <I extends LevelAccessor> I getInterceptor() {
        return (I) Objects.requireNonNull(this.level.get(), "World reference has been culled");
    }
}
