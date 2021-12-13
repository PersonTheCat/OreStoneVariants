package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenTickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Block;

public class WorldGenTickInterceptor extends WorldGenTickList<Block> {

    final TickInterceptorHandle<WorldGenTickList<Block>> handle;

    public WorldGenTickInterceptor() {
        super(null);
        this.handle = new TickInterceptorHandle<>(this);
    }

    @Override
    public boolean hasScheduledTick(final BlockPos pos, final Block block) {
        return this.handle.getList().hasScheduledTick(pos, block);
    }

    @Override
    public void scheduleTick(final BlockPos pos, final Block block, final int time, final TickPriority priority) {
        this.handle.getList().scheduleTick(pos, this.handle.expose(pos, block), time, priority);
    }

    @Override
    public boolean willTickThisTick(final BlockPos blockPos, final Block object) {
        return this.handle.getList().willTickThisTick(blockPos, object);
    }
}
