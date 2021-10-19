package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import personthecat.osv.block.SharedStateBlock;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class DummyInterceptorHandle<L extends LevelAccessor> extends InterceptorHandle<L, TickListHandle<TickList<Block>>> {

    private final WeakReference<L> level;

    DummyInterceptorHandle(final L level) {
        super(null, null);
        this.level = new WeakReference<>(level);
    }

    @Override
    <I extends LevelAccessor> InterceptorHandle<L, TickListHandle<TickList<Block>>> prime(final I level) {
        return this;
    }

    InterceptorHandle<L, TickListHandle<TickList<Block>>> intercept(final SharedStateBlock block, final Block expected) {
        return this;
    }

    InterceptorHandle<L, TickListHandle<TickList<Block>>> onlyAt(final BlockPos pos) {
        return this;
    }

    InterceptorHandle<L, TickListHandle<TickList<Block>>> dispose() {
        return this;
    }

    @SuppressWarnings("unchecked")
    public <I extends LevelAccessor> I getInterceptor() {
        return (I) Objects.requireNonNull(this.level.get(), "World reference has been culled");
    }
}
