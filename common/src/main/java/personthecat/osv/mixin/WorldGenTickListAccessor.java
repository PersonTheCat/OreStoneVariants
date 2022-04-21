package personthecat.osv.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.TickContainerAccess;
import net.minecraft.world.ticks.WorldGenTickAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(WorldGenTickAccess.class)
public interface WorldGenTickListAccessor<T> {

    @Accessor
    Function<BlockPos, TickContainerAccess<T>> getContainerGetter();
}
