package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
class ServerTickInterceptor extends LevelTicks<Block> {
    
    final ThreadLocal<InterceptorHandle> handles;
    final LevelTicks<Block> delegate;

    ServerTickInterceptor(final ServerLevelInterceptor interceptor, final ServerLevel level) {
        super(l -> true, () -> InactiveProfiler.INSTANCE);
        this.handles = interceptor.handles;
        this.delegate = level.getBlockTicks();
    }

    @Override
    public void tick(final long i, final int j, final BiConsumer<BlockPos, Block> f) {
        this.delegate.tick(i, j, f);
    }

    @Override
    public boolean willTickThisTick(final BlockPos pos, final Block block) {
        return this.delegate.willTickThisTick(pos, block);
    }

    @Override
    public boolean hasScheduledTick(final BlockPos pos, final Block block) {
        final InterceptorHandle handle = this.handles.get();
        if (handle != null) {
            return this.delegate.hasScheduledTick(pos, handle.expose(pos, block));
        }
        return this.delegate.hasScheduledTick(pos, block);
    }

    @Override
    public void schedule(final ScheduledTick<Block> tick) {
        final InterceptorHandle handle = this.handles.get();
        if (handle != null) {
            final Block exposed = handle.expose(tick.pos(), tick.type());
            this.delegate.schedule(
                new ScheduledTick<>(exposed, tick.pos(), tick.triggerTick(), tick.subTickOrder()));
        } else {
            this.delegate.schedule(tick);
        }
    }

    @Override
    public void addContainer(final ChunkPos pos, final LevelChunkTicks<Block> ticks) {
        this.delegate.addContainer(pos, ticks);
    }

    @Override
    public void copyArea(final BoundingBox boundingBox, final Vec3i pos) {
        this.delegate.copyArea(boundingBox, pos);
    }

    @Override
    public void clearArea(final BoundingBox boundingBox) {
        this.delegate.clearArea(boundingBox);
    }

    @Override
    public int count() {
        return this.delegate.count();
    }
}
