package personthecat.osv.world.placer;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.PlacedFeatureSettings;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class VariantBlockPlacer implements BlockPlacer {

    public static final Codec<VariantBlockPlacer> CODEC =
        BlockMatchingSpawnConfig.MAP_CODEC.xmap(VariantBlockPlacer::new, p -> p.map);

    final Map<BlockState, Set<BlockMatchingSpawnConfig>> map;
    final int id;

    public VariantBlockPlacer(final PlacedFeatureSettings<?, ?> cfg, final OrePreset preset) {
        this(BlockMatchingSpawnConfig.createMap(cfg, preset));
    }

    public VariantBlockPlacer(final Map<BlockState, Set<BlockMatchingSpawnConfig>> map) {
        this.map = map;
        this.id = BlockMatchingSpawnConfig.hash(map);
    }

    @Override
    public boolean place(final WorldGenLevel level, final Random rand, final BlockPos pos) {
        final BlockState bg = level.getBlockState(pos);
        final Set<BlockMatchingSpawnConfig> blocks = this.map.get(bg);
        if (blocks != null) {
            for (final BlockMatchingSpawnConfig block : blocks) {
                if (block.chance == 1.0 || rand.nextDouble() <= block.chance) {
                    level.setBlock(pos, block.block, 2);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean placeUnchecked(final ChunkAccess chunk, final Random rand, int x, int y, int z) {
        final int i = y >> 4;
        final LevelChunkSection section = chunk.getSections()[i];
        if (section == null) {
            return false;
        }
        x &= 15;
        y &= 15;
        z &= 15;
        final BlockState bg = section.getBlockState(x, y, z);
        final Set<BlockMatchingSpawnConfig> blocks = this.map.get(bg);
        if (blocks != null) {
            for (final BlockMatchingSpawnConfig block : blocks) {
                if (block.chance == 1.0 || rand.nextDouble() <= block.chance) {
                    section.setBlockState(x, y, z, block.block, false);
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
