package personthecat.osv.world.interceptor;

import lombok.experimental.Delegate;
import net.minecraft.server.level.WorldGenRegion;
import personthecat.osv.util.unsafe.UnsafeUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class WorldGenRegionInterceptor extends WorldGenRegion {

    private WeakReference<WorldGenRegion> wrapped;

    public WorldGenRegionInterceptor() {
        super(null, null);
        throw new UnsupportedOperationException("Illegal constructor access");
    }

    // Todo: this needs to be split into create and prime for.
    static WorldGenRegionInterceptor primeFor(final WorldGenRegion region) {
        final WorldGenRegionInterceptor interceptor = UnsafeUtils.allocate(WorldGenRegionInterceptor.class);
        interceptor.wrapped = new WeakReference<>(region);
        return interceptor;
    }

    @Delegate(excludes = DelegateExclusions.class)
    private WorldGenRegion getWrapped() {
        return Objects.requireNonNull(this.wrapped.get(), "World reference has been culled");
    }
}
