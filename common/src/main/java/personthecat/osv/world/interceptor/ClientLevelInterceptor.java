package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.util.unsafe.UnsafeUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class ClientLevelInterceptor extends ClientLevel implements InterceptorAccessor {

    ThreadLocal<InterceptorHandle> handles;
    WeakReference<ClientLevel> delegate;

    @SuppressWarnings("ConstantConditions")
    private ClientLevelInterceptor() {
        super(null, null, null, null, 0, null, null, false, 0);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    static ClientLevelInterceptor create(final ClientLevel level) {
        final ClientLevelInterceptor interceptor = UnsafeUtils.allocate(ClientLevelInterceptor.class);
        // Copy some data in case someone tries to access the private values.
        UnsafeUtils.copyFields(level, interceptor);
        interceptor.handles = ThreadLocal.withInitial(InterceptorHandle::new);
        interceptor.delegate = new WeakReference<>(level);
        return interceptor;
    }

    @Override
    public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int recursion) {
        final ClientLevel level = this.getWrapped();
        final InterceptorHandle handle = this.getHandle();
        if (handle.isPrimed()) {
            return level.setBlock(pos, handle.expose(pos, state), flags, recursion);
        }
        return level.setBlock(pos, state, flags, recursion);
    }

    @Override
    public void setBlocksDirty(final BlockPos pos, final BlockState from, final BlockState to) {
        final ClientLevel level = this.getWrapped();
        final InterceptorHandle handle = this.getHandle();
        if (handle.isPrimed()) {
            level.setBlocksDirty(pos, from, handle.expose(pos, to));
        } else {
            level.setBlocksDirty(pos, from, to);
        }
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        final ClientLevel level = this.getWrapped();
        final InterceptorHandle handle = this.getHandle();
        return handle.isPrimed() ? handle.disguise(pos, level.getBlockState(pos)) : level.getBlockState(pos);
    }

    @Delegate(excludes = DelegateExclusions.class)
    private ClientLevel getWrapped() {
        return Objects.requireNonNull(this.delegate.get(), "Client reference has been culled");
    }

    @Override
    public ThreadLocal<InterceptorHandle> handles() {
        return this.handles;
    }
}
