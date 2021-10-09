package personthecat.osv.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(InfestedBlock.class)
public interface InfestedBlockAccessor {

    @Accessor("BLOCK_BY_HOST_BLOCK")
    static Map<Block, Block> getBlockByHostMap() {
        throw new AssertionError();
    }
}
