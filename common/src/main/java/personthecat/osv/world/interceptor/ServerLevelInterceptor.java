package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.server.level.ServerLevel;
import personthecat.osv.util.unsafe.UnsafeUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class ServerLevelInterceptor extends ServerLevel {

    private WeakReference<ServerLevel> wrapped;

    private ServerLevelInterceptor() {
        super(null, null, null, null, null, null, null, null, false, 0, null, false);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    static ServerLevelInterceptor primeFor(final ServerLevel level) {
        final ServerLevelInterceptor interceptor = UnsafeUtils.allocate(ServerLevelInterceptor.class);
        UnsafeUtils.copyFields(level, interceptor);
        interceptor.wrapped = new WeakReference<>(level);
        return interceptor;
    }

    @Delegate(excludes = DelegateExclusions.class)
    private ServerLevel getWrapped() {
        return Objects.requireNonNull(this.wrapped.get(), "World reference has been culled");
    }
}