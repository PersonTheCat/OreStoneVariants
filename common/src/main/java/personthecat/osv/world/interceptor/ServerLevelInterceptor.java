package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.mixin.FallingBlockEntityAccessor;
import personthecat.osv.mixin.MobAccessor;
import personthecat.osv.util.unsafe.UnsafeUtils;

public class ServerLevelInterceptor extends ServerLevel {

    InterceptorHandle<ServerLevel, TickInterceptorHandle<ServerTickList<Block>>> handle;

    private ServerLevelInterceptor() {
        super(null, null, null, null, null, null, null, null, false, 0, null, false);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    static ServerLevelInterceptor create(final ServerLevel level) {
        final ServerLevelInterceptor interceptor = UnsafeUtils.allocate(ServerLevelInterceptor.class);
        UnsafeUtils.copyFields(level, interceptor);
        interceptor.handle = new InterceptorHandle<>(interceptor, new ServerTickInterceptor(level).handle);
        return interceptor;
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursion) {
        final ServerLevel level = this.handle.getLevel();
        return level.setBlock(pos, this.handle.expose(pos, state), flags, recursion);
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        final ServerLevel level = this.handle.getLevel();
        return this.handle.disguise(pos, level.getBlockState(pos));
    }

    @Override
    public ServerTickList<Block> getBlockTicks() {
        return this.handle.isPrimed() ? this.handle.getTickList().getInterceptor() : this.handle.getLevel().getBlockTicks();
    }

    @Override
    public boolean addFreshEntity(final Entity entity) {
        final ServerLevel level = this.handle.getLevel();
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
        return level.addFreshEntity(entity);
    }

    @Delegate(excludes = DelegateExclusions.class)
    private ServerLevel getWrapped() {
        return this.handle.getLevel();
    }
}