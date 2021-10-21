package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.server.level.WorldGenTickList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.mixin.FallingBlockEntityAccessor;
import personthecat.osv.mixin.MobAccessor;
import personthecat.osv.util.unsafe.UnsafeUtils;

import java.util.function.Predicate;

public class WorldGenRegionInterceptor extends WorldGenRegion {

    InterceptorHandle<WorldGenRegion, TickInterceptorHandle<WorldGenTickList<Block>>> handle;

    private WorldGenRegionInterceptor() {
        super(null, null);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    static WorldGenRegionInterceptor create(final WorldGenRegion region) {
        final WorldGenRegionInterceptor interceptor = UnsafeUtils.allocate(WorldGenRegionInterceptor.class);
        // Copy some data in case someone tries to access the private values.
        UnsafeUtils.copyFields(region, interceptor);
        interceptor.handle = new InterceptorHandle<>(interceptor, new WorldGenTickInterceptor().handle);
        return interceptor;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        final WorldGenRegion region = this.handle.getLevel();
        return this.handle.disguise(pos, region.getBlockState(pos));
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursion) {
        final WorldGenRegion region = this.handle.getLevel();
        return region.setBlock(pos, this.handle.expose(pos, state), flags, recursion);
    }

    @Override
    public boolean isStateAtPosition(final BlockPos pos, final Predicate<BlockState> predicate) {
        return predicate.test(this.getBlockState(pos));
    }

    @Override
    public TickList<Block> getBlockTicks() {
        return this.handle.isPrimed() ? this.handle.getTickList().getInterceptor() : this.handle.getLevel().getBlockTicks();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean addFreshEntity(final Entity entity) {
        final WorldGenRegion region = this.handle.getLevel();
        final Level level = region.getLevel();
        entity.setLevel(level);

        if (entity instanceof FallingBlockEntity) {
            final FallingBlockEntityAccessor accessor = (FallingBlockEntityAccessor) entity;
            accessor.setBlockState(this.handle.expose(null, accessor.getBlockState()));
        } else if (entity instanceof Mob) {
            // Mob entities will keep references to the
            // current world and thus must be restored.
            final MobAccessor accessor = (MobAccessor) entity;
            accessor.setNavigation(accessor.invokeCreateNavigation(level));
            accessor.invokeRegisterGoals();
        }
        return region.addFreshEntity(entity);
    }

    @Override
    public String toString() {
        return "WorldGenRegionInterceptor[" + this.handle.getLevel() + "]";
    }

    @Delegate(excludes = DelegateExclusions.class)
    private WorldGenRegion getWrapped() {
        return this.handle.getLevel();
    }
}
