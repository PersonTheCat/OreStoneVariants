package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickContainerAccess;
import net.minecraft.world.ticks.WorldGenTickAccess;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class WorldGenTickInterceptor extends WorldGenTickAccess<Block> {

    final ThreadLocal<InterceptorHandle> handles;
    final ThreadLocal<WeakReference<WorldGenRegion>> delegates;

    public WorldGenTickInterceptor(
            final WorldGenRegionInterceptor interceptor,
            final Function<BlockPos, TickContainerAccess<Block>> containerGetter) {
        // Pass in real data so that Sponge never complains.
        super(containerGetter);
        this.handles = interceptor.handles;
        this.delegates = interceptor.delegates;
    }

    @Override
    public boolean hasScheduledTick(final BlockPos pos, final Block block) {
        final WorldGenRegion delegate = this.getDelegate();
        return delegate != null && delegate.getBlockTicks().hasScheduledTick(pos, block);
    }

    @Override
    public void schedule(final ScheduledTick<Block> tick) {
        final WorldGenRegion delegate = this.getDelegate();
        if (delegate == null) return;
        final InterceptorHandle handle = this.handles.get();
        if (handle != null) {
            final Block exposed = handle.expose(tick.pos(), tick.type());
            delegate.getBlockTicks().schedule(
                new ScheduledTick<>(exposed, tick.pos(), tick.triggerTick(), tick.subTickOrder()));
        } else {
            delegate.getBlockTicks().schedule(tick);
        }
    }

    @Override
    public boolean willTickThisTick(final BlockPos blockPos, final Block object) {
        final WorldGenRegion delegate = this.getDelegate();
        return delegate != null && delegate.getBlockTicks().willTickThisTick(blockPos, object);
    }

    @Nullable
    private WorldGenRegion getDelegate() {
        final WeakReference<WorldGenRegion> reference = this.delegates.get();
        return reference != null ? reference.get() : null;
    }
}
