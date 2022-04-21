package personthecat.osv.block.forge;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.block.StateConfig;
import personthecat.osv.preset.OrePreset;

public class OreVariantImpl {
    public static OreVariant createPlatformVariant(final OrePreset preset, final Properties properties, final StateConfig config) {
        return new ForgeVariant(preset, properties, config);
    }
}
