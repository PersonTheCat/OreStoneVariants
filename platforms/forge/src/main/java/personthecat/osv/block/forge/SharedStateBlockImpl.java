package personthecat.osv.block.forge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import personthecat.osv.block.SharedStateBlock;

public class SharedStateBlockImpl {
    public static SharedStateBlock createPlatformVariant(final Block bg, final Block fg, final Properties properties) {
        return new OreVariant(bg, fg, properties);
    }
}
