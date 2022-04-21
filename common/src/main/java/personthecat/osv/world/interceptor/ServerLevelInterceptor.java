package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.mixin.FallingBlockEntityAccessor;
import personthecat.osv.mixin.MobAccessor;
import personthecat.osv.util.unsafe.UnsafeUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class ServerLevelInterceptor extends ServerLevel implements InterceptorAccessor {

    ThreadLocal<InterceptorHandle> handles;
    WeakReference<ServerLevel> delegate;
    ServerTickInterceptor tickInterceptor;

    @SuppressWarnings("ConstantConditions")
    private ServerLevelInterceptor() {
        super(null, null, null, null, null, null, null, null, false, 0, null, false);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    static ServerLevelInterceptor create(final ServerLevel level) {
        final ServerLevelInterceptor interceptor = UnsafeUtils.allocate(ServerLevelInterceptor.class);
        UnsafeUtils.copyFields(level, interceptor);

        interceptor.handles = ThreadLocal.withInitial(InterceptorHandle::new);
        interceptor.delegate = new WeakReference<>(level);
        interceptor.tickInterceptor = new ServerTickInterceptor(interceptor, level);
        return interceptor;
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursion) {
        final ServerLevel level = this.getWrapped();
        final InterceptorHandle handle = this.getHandle();
        if (handle.isPrimed()) {
            return level.setBlock(pos, handle.expose(pos, state), flags, recursion);
        }
        return level.setBlock(pos, state, flags, recursion);
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        final ServerLevel level = this.getWrapped();
        final InterceptorHandle handle = this.getHandle();
        return handle.isPrimed() ? handle.disguise(pos, level.getBlockState(pos)) : level.getBlockState(pos);
    }

    @Override
    public ServerTickInterceptor getBlockTicks() {
        return this.tickInterceptor;
    }

    @Override
    public boolean addFreshEntity(final Entity entity) {
        final ServerLevel level = this.getWrapped();
        entity.level = level;

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
        return level.addFreshEntity(entity);
    }

    @Delegate(excludes = DelegateExclusions.class)
    private ServerLevel getWrapped() {
        return Objects.requireNonNull(this.delegate.get(), "Server reference has been culled");
    }

    @Override
    public ThreadLocal<InterceptorHandle> handles() {
        return this.handles;
    }
}