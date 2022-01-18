package personthecat.osv.world.interceptor;

import lombok.extern.log4j.Log4j2;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.config.Cfg;
import personthecat.osv.util.unsafe.UnsafeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class InterceptorDispatcher {

    private static final Map<Integer, LevelAccessor> INTERCEPTORS = new ConcurrentHashMap<>();
    private static final int REGION_ID = Integer.MIN_VALUE;
    public static final boolean COMPATIBILITY_MODE = Cfg.forceCompatibilityMode() || !UnsafeUtils.isAvailable();

    public static <L extends LevelAccessor> L intercept(
            final L level, final BlockState state, final Block expected, final @Nullable BlockPos pos) {
        if (COMPATIBILITY_MODE) {
            return level;
        }
        final L interceptor = get(level);
        if (interceptor instanceof InterceptorAccessor) {
            if (interceptor instanceof WorldGenRegionInterceptor) {
                ((WorldGenRegionInterceptor) interceptor).prime((WorldGenRegion) level);
            }
            ((InterceptorAccessor) interceptor).intercept(state, expected, pos);
        }
        return interceptor;
    }

    @SuppressWarnings("unchecked")
    private static <L extends LevelAccessor> L get(final L level) {
        final int key = level instanceof WorldGenRegion ? REGION_ID : System.identityHashCode(level);
        return (L) INTERCEPTORS.computeIfAbsent(key, k -> {
            if (level instanceof WorldGenRegion) {
                log.debug("Creating region interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return WorldGenRegionInterceptor.create((WorldGenRegion) level);
            } else if (level instanceof ServerLevel) {
                log.debug("Creating server interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return ServerLevelInterceptor.create((ServerLevel) level);
            } else if (level.isClientSide()) {
                log.debug("Creating client interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return ClientLevelInterceptor.create((ClientLevel) level);
            }
            log.warn("No interceptor for " + level.getClass() + ". Update required.");
            return level;
        });
    }

    public static void unloadAll() {
        INTERCEPTORS.clear();
    }
}
