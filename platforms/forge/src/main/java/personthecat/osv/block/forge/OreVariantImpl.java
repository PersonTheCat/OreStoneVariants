package personthecat.osv.block.forge;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.block.StateConfig;

public class OreVariantImpl {
    public static OreVariant createPlatformVariant(final Properties properties, final StateConfig config) {
        return new ForgeVariant(properties, config);
    }
}
