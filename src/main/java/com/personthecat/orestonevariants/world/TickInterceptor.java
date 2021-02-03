package com.personthecat.orestonevariants.world;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ITickList;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TickInterceptor extends ServerTickList<Block> {

    private ITickList<Block> wrapped;
    private Block from;
    private Block to;

    public TickInterceptor(ServerWorld world) {
        super(world, b -> true, ForgeRegistryEntry::getRegistryName, t -> {});
        this.wrapped = world.getPendingBlockTicks();
    }

    void wrapping(ITickList<Block> ticks) {
        this.wrapped = ticks;
    }

    void listenFor(Block from, Block to) {
        this.from = from;
        this.to = to;
    }

    void clear() {
        this.wrapped = null;
        this.from = null;
        this.to = null;
    }

    @Override
    public boolean isTickPending(BlockPos pos, Block block) {
        return wrapped.isTickPending(pos, block);
    }

    @Override
    public List<NextTickListEntry<Block>> getPending(MutableBoundingBox bb, boolean remove, boolean skipCompleted) {
        if (wrapped instanceof ServerTickList) {
            return ((ServerTickList<Block>) wrapped).getPending(bb, remove, skipCompleted);
        }
        return super.getPending(bb, remove, skipCompleted);
    }

    @Override
    public boolean isTickScheduled(BlockPos pos, Block block) {
        return wrapped.isTickScheduled(pos, block);
    }

    @Override
    public void scheduleTick(BlockPos pos, Block block, int scheduledTime, TickPriority priority) {
        if (block.equals(from)) {
            block = to;
        }
        wrapped.scheduleTick(pos, block, scheduledTime, priority);
    }

    @Override
    public int func_225420_a() {
        if (wrapped instanceof ServerTickList) {
            return ((ServerTickList<Block>) wrapped).func_225420_a();
        }
        return super.func_225420_a();
    }
}
