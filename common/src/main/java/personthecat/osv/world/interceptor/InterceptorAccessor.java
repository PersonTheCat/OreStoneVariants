package personthecat.osv.world.interceptor;

import net.minecraft.world.level.LevelAccessor;

public class InterceptorAccessor {
    public static void dispose(final LevelAccessor level) {
        if (level instanceof WorldGenRegionInterceptor) {
            ((WorldGenRegionInterceptor) level).handle.dispose();
        } else if (level instanceof ServerLevelInterceptor) {
            ((ServerLevelInterceptor) level).handle.dispose();
        } else if (level.isClientSide()) {
            ((ClientLevelInterceptor) level).handle.dispose();
        }
    }
}
