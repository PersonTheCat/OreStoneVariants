package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import personthecat.catlib.event.registry.CommonRegistries;

import java.util.List;

class ServerTickInterceptor extends ServerTickList<Block> {
    
    final TickInterceptorHandle<ServerTickList<Block>> handle;

    ServerTickInterceptor() {
        super(null, b -> true, CommonRegistries.BLOCKS::getKey, data -> {});
        this.handle = new TickInterceptorHandle<>(this);
    }

    @Override
    public void tick() {
        this.handle.getList().tick();
    }

    @Override
    public boolean willTickThisTick(final BlockPos pos, final Block block) {
        return this.handle.getList().willTickThisTick(pos, block);
    }

    @Override
    public boolean hasScheduledTick(final BlockPos pos, final Block block) {
        return this.handle.getList().hasScheduledTick(pos, this.handle.expose(pos, block));
    }

    @Override
    public void scheduleTick(final BlockPos pos, final Block block, final int time, final TickPriority priority) {
        this.handle.getList().scheduleTick(pos, this.handle.expose(pos, block), time, priority);
    }

    @Override
    public List<TickNextTickData<Block>> fetchTicksInArea(final BoundingBox boundingBox, final boolean bl, final boolean bl2) {
        return this.handle.getList().fetchTicksInArea(boundingBox, bl, bl2);
    }

    @Override
    public void copy(final BoundingBox boundingBox, final BlockPos pos) {
        this.handle.getList().copy(boundingBox, pos);
    }

    @Override
    public ListTag save(final ChunkPos pos) {
        return this.handle.getList().save(pos);
    }

    @Override
    public int size() {
        return this.handle.getList().size();
    }
}
