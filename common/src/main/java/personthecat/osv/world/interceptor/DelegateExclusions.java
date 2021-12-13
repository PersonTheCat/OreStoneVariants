package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

@SuppressWarnings("unused")
interface DelegateExclusions {
    boolean isDebug();
    boolean setBlock(final BlockPos pos, final BlockState state, final int flags);
    boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursion);
    boolean setBlockAndUpdate(final BlockPos pos, final BlockState state);
    void setBlocksDirty(final BlockPos pos, final BlockState from, final BlockState to);
    BlockState getBlockState(final BlockPos pos);
    boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate);
    TickList<Block> getBlockTicks();
    void sendBlockUpdated(final BlockPos pos, final BlockState from, final BlockState to, final int flags);
    void onBlockStateChange(final BlockPos pos, final BlockState from, final BlockState tgo);
    void blockUpdated(final BlockPos pos, final Block block);
    boolean addFreshEntity(final Entity entity);
    boolean addWithUUID(final Entity entity);
    boolean tryAddFreshEntityWithPassengers(final Entity entity);
    void addFreshEntityWithPassengers(final Entity entity);
}
