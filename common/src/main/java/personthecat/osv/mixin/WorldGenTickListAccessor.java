package personthecat.osv.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenTickList;
import net.minecraft.world.level.TickList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(WorldGenTickList.class)
public interface WorldGenTickListAccessor<T> {

    @Accessor
    Function<BlockPos, TickList<T>> getIndex();
}
