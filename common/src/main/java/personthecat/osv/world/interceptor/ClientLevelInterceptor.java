package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.util.unsafe.UnsafeUtils;

public class ClientLevelInterceptor extends ClientLevel {

    InterceptorHandle<ClientLevel, TickListHandle<EmptyTickList<Block>>> handle;

    @SuppressWarnings("ConstantConditions")
    private ClientLevelInterceptor() {
        super(null, null, null, null, 0, null, null, false, 0);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    static ClientLevelInterceptor create(final ClientLevel level) {
        final ClientLevelInterceptor interceptor = UnsafeUtils.allocate(ClientLevelInterceptor.class);
        // Copy some data in case someone tries to access the private values.
        UnsafeUtils.copyFields(level, interceptor);
        interceptor.handle = new InterceptorHandle<>(interceptor, new TickListHandle<>(EmptyTickList.empty()));
        return interceptor;
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursion) {
        final ClientLevel level = this.handle.getLevel();
        return level.setBlock(pos, this.handle.expose(pos, state), flags, recursion);
    }

    @Override
    public void setBlocksDirty(final BlockPos pos, final BlockState from, final BlockState to) {
        final ClientLevel level = this.handle.getLevel();
        level.setBlocksDirty(pos, from, this.handle.expose(pos, to));
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        final ClientLevel level = this.handle.getLevel();
        return this.handle.disguise(pos, level.getBlockState(pos));
    }

    @Delegate(excludes = DelegateExclusions.class)
    private ClientLevel getWrapped() {
        return this.handle.getLevel();
    }
}
