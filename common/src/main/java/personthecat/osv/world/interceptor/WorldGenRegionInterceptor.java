package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.mixin.FallingBlockEntityAccessor;
import personthecat.osv.mixin.MobAccessor;
import personthecat.osv.mixin.WorldGenTickListAccessor;
import personthecat.osv.util.unsafe.UnsafeUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class WorldGenRegionInterceptor extends WorldGenRegion implements InterceptorAccessor {

    ThreadLocal<InterceptorHandle> handles;
    ThreadLocal<WeakReference<WorldGenRegion>> delegates;
    WorldGenTickInterceptor tickInterceptor;

    private WorldGenRegionInterceptor() {
        super(null, null);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    @SuppressWarnings("unchecked")
    static WorldGenRegionInterceptor create(final WorldGenRegion region) {
        final WorldGenRegionInterceptor interceptor = UnsafeUtils.allocate(WorldGenRegionInterceptor.class);
        final TickList<Block> ticks = region.getBlockTicks();
        final Function<BlockPos, TickList<Block>> index = ((WorldGenTickListAccessor<Block>) ticks).getIndex();
        interceptor.handles = ThreadLocal.withInitial(InterceptorHandle::new);
        interceptor.delegates = new ThreadLocal<>();
        interceptor.tickInterceptor = new WorldGenTickInterceptor(interceptor, index);
        // Copy some data in case someone tries to access the private values.
        UnsafeUtils.copyFields(region, interceptor);
        return interceptor;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        final WorldGenRegion region = this.getWrapped();
        final InterceptorHandle handle = this.getHandle();
        final BlockState state = region.getBlockState(pos);
        return handle.isPrimed() ? handle.disguise(pos, state) : state;
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursion) {
        final WorldGenRegion region = this.getWrapped();
        final InterceptorHandle handle = this.getHandle();
        if (handle.isPrimed()) {
            return region.setBlock(pos, handle.expose(pos, state), flags, recursion);
        }
        return region.setBlock(pos, state, flags, recursion);
    }

    @Override
    public boolean isStateAtPosition(final BlockPos pos, final Predicate<BlockState> predicate) {
        return predicate.test(this.getBlockState(pos));
    }

    @Override
    public TickList<Block> getBlockTicks() {
        return this.tickInterceptor;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean addFreshEntity(final Entity entity) {
        final WorldGenRegion region = this.getWrapped();
        final Level level = region.getLevel();
        entity.setLevel(level);

        final InterceptorHandle handle = this.getHandle();
        if (handle.isPrimed() && entity instanceof FallingBlockEntity) {
            final FallingBlockEntityAccessor accessor = (FallingBlockEntityAccessor) entity;
            accessor.setBlockState(handle.expose(null, accessor.getBlockState()));
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
        return "WorldGenRegionInterceptor[" + this.getHandle() + "]";
    }

    @Delegate(excludes = DelegateExclusions.class)
    private WorldGenRegion getWrapped() {
        final WeakReference<WorldGenRegion> reference =
            Objects.requireNonNull(this.delegates.get(), "No delegate in thread");
        return Objects.requireNonNull(reference.get(), "Region reference has been culled");
    }

    public void prime(final WorldGenRegion region) {
        this.delegates.set(new WeakReference<>(region));
    }

    @Override
    public ThreadLocal<InterceptorHandle> handles() {
        return this.handles;
    }
}
