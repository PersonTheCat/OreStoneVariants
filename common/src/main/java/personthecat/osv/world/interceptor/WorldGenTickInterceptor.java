package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.server.level.WorldGenTickList;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Function;

public class WorldGenTickInterceptor extends WorldGenTickList<Block> {

    final ThreadLocal<InterceptorHandle> handles;
    final ThreadLocal<WeakReference<WorldGenRegion>> delegates;

    public WorldGenTickInterceptor(final WorldGenRegionInterceptor interceptor, final Function<BlockPos, TickList<Block>> index) {
        // Pass in real data so that Sponge never complains.
        super(index);
        this.handles = interceptor.handles;
        this.delegates = interceptor.delegates;
    }

    @Override
    public boolean hasScheduledTick(final BlockPos pos, final Block block) {
        final WorldGenRegion delegate = this.getDelegate();
        return delegate != null && delegate.getBlockTicks().hasScheduledTick(pos, block);
    }

    @Override
    public void scheduleTick(final BlockPos pos, final Block block, final int time, final TickPriority priority) {
        final WorldGenRegion delegate = this.getDelegate();
        if (delegate == null) return;
        final InterceptorHandle handle = this.handles.get();
        if (handle != null) {
            delegate.getBlockTicks().scheduleTick(pos, handle.expose(pos, block), time, priority);
        } else {
            delegate.getBlockTicks().scheduleTick(pos, block, time, priority);
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
