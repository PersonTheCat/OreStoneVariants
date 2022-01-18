package personthecat.osv.world.interceptor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.OreVariant;

public interface InterceptorAccessor {

    ThreadLocal<InterceptorHandle> handles();

    default void intercept(final BlockState state, final Block expected, final @Nullable BlockPos pos) {
        if (state.getBlock() instanceof OreVariant) {
            this.getHandle().intercept(state, expected, pos);
        }
    }

    default InterceptorHandle getHandle() {
        return this.handles().get();
    }

    default void dispose() {
        this.getHandle().dispose();
    }

    static void dispose(final LevelAccessor level) {
        if (level instanceof InterceptorHandle) {
            ((InterceptorAccessor) level).dispose();
        }
    }
}
