package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.DecoratedFeatureSettings;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class VariantBlockPlacer implements BlockPlacer {

    public static final Codec<VariantBlockPlacer> CODEC =
        BlockMatchingSpawnConfig.MAP_CODEC.xmap(VariantBlockPlacer::new, p -> p.map);

    final Map<BlockState, Set<BlockMatchingSpawnConfig>> map;
    final int id;

    public VariantBlockPlacer(final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {
        this(BlockMatchingSpawnConfig.createMap(cfg, preset));
    }

    public VariantBlockPlacer(final Map<BlockState, Set<BlockMatchingSpawnConfig>> map) {
        this.map = map;
        this.id = BlockMatchingSpawnConfig.hash(map);
    }

    @Override
    public boolean place(final WorldGenLevel level, final Random rand, final BlockPos pos) {
        final BlockState bg = level.getBlockState(pos);
        final Set<BlockMatchingSpawnConfig> blocks = map.get(bg);
        if (blocks != null) {
            for (final BlockMatchingSpawnConfig block : blocks) {
                if (rand.nextDouble() <= block.chance) {
                    level.setBlock(pos, block.block, 2);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getId() {
        return this.id;
    }
}
