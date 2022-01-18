package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import personthecat.catlib.event.registry.CommonRegistries;

import java.util.List;

class ServerTickInterceptor extends ServerTickList<Block> {
    
    final ThreadLocal<InterceptorHandle> handles;
    final ServerTickList<Block> delegate;

    ServerTickInterceptor(final ServerLevelInterceptor interceptor, final ServerLevel level) {
        super(level, b -> true, CommonRegistries.BLOCKS::getKey, data -> {});
        this.handles = interceptor.handles;
        this.delegate = level.getBlockTicks();
    }

    @Override
    public void tick() {
        this.delegate.tick();
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
    public void scheduleTick(final BlockPos pos, final Block block, final int time, final TickPriority priority) {
        final InterceptorHandle handle = this.handles.get();
        if (handle != null) {
            this.delegate.scheduleTick(pos, handle.expose(pos, block), time, priority);
        } else {
            this.delegate.scheduleTick(pos, block, time, priority);
        }
    }

    @Override
    public List<TickNextTickData<Block>> fetchTicksInArea(final BoundingBox boundingBox, final boolean bl, final boolean bl2) {
        return this.delegate.fetchTicksInArea(boundingBox, bl, bl2);
    }

    @Override
    public void copy(final BoundingBox boundingBox, final BlockPos pos) {
        this.delegate.copy(boundingBox, pos);
    }

    @Override
    public ListTag save(final ChunkPos pos) {
        return this.delegate.save(pos);
    }

    @Override
    public int size() {
        return this.delegate.size();
    }
}
