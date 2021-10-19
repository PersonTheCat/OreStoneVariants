package personthecat.osv.world.interceptor;

import lombok.extern.log4j.Log4j2;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;

import java.util.IdentityHashMap;
import java.util.Map;

@Log4j2
public class InterceptorDispatcher {

    private static final ThreadLocal<Map<Class<?>, InterceptorHandle<?, ?>>> INTERCEPTORS =
        ThreadLocal.withInitial(IdentityHashMap::new);

    public static <L extends LevelAccessor> InterceptorHandle<L, ?> prime(final L level) {
        return get(level).prime(level);
    }

    @SuppressWarnings("unchecked")
    private static <L extends LevelAccessor> InterceptorHandle<L, ?> get(final L level) {
        if (level instanceof WorldGenRegion) {
            final WorldGenRegion region = (WorldGenRegion) level;
            return (InterceptorHandle<L, ?>) INTERCEPTORS.get().computeIfAbsent(WorldGenRegion.class, k -> {
                log.debug("Creating region interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return WorldGenRegionInterceptor.create(region).handle;
            });
        } else if (level instanceof ClientLevel) {
            final ClientLevel client = (ClientLevel) level;
            return (InterceptorHandle<L, ?>) INTERCEPTORS.get().computeIfAbsent(ClientLevel.class, k -> {
                log.debug("Creating client interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return ClientLevelInterceptor.create(client).handle;
            });
        } else if (level instanceof ServerLevel) {
            final ServerLevel server = (ServerLevel) level;
            return (InterceptorHandle<L, ?>) INTERCEPTORS.get().computeIfAbsent(ServerLevel.class, k -> {
                log.debug("Creating server interceptor for type: {} in thread: {}",
                    level.getClass(), Thread.currentThread());
                return ServerLevelInterceptor.create(server).handle;
            });
        }
        return (InterceptorHandle<L, ?>) INTERCEPTORS.get().computeIfAbsent(level.getClass(), k -> {
            log.warn("No interceptor for " + level.getClass() + ". Update required.");
            return new DummyInterceptorHandle<>(level);
        });
    }

    public static void unloadAll() {
        INTERCEPTORS.remove();
    }
}
