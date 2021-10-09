package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.client.multiplayer.ClientLevel;
import personthecat.osv.util.unsafe.UnsafeUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class ClientLevelInterceptor extends ClientLevel {

    private WeakReference<ClientLevel> wrapped;

    @SuppressWarnings("ConstantConditions")
    private ClientLevelInterceptor() {
        super(null, null, null, null, 0, null, null, false, 0);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    static ClientLevelInterceptor primeFor(final ClientLevel level) {
        final ClientLevelInterceptor interceptor = UnsafeUtils.allocate(ClientLevelInterceptor.class);
        UnsafeUtils.copyFields(level, interceptor);
        interceptor.wrapped = new WeakReference<>(level);
        return interceptor;
    }

    @Delegate(excludes = DelegateExclusions.class)
    private ClientLevel getWrapped() {
        return Objects.requireNonNull(this.wrapped.get(), "World reference has been culled");
    }
}
