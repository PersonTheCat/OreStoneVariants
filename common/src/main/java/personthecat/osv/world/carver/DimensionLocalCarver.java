package personthecat.osv.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class DimensionLocalCarver extends WorldCarver<DimensionLocalCarverConfig> {

    public static final DimensionLocalCarver INSTANCE = new DimensionLocalCarver();
    public static final Codec<DimensionLocalCarver> CODEC = Codec.unit(INSTANCE);

    private DimensionLocalCarver() {
        super(DimensionLocalCarverConfig.CODEC);
    }

    @Override
    public boolean carve(CarvingContext ctx, DimensionLocalCarverConfig cfg, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomes, Random rand, Aquifer aquifer, ChunkPos pos, CarvingMask mask) {
        if (cfg.dimensions.test(chunk) && cfg.delegate.isStartChunk(rand)) {
            return cfg.delegate.carve(ctx, chunk, biomes, rand, aquifer, pos, mask);
        }
        return false;
    }

    @Override
    public boolean isStartChunk(DimensionLocalCarverConfig cfg, Random rand) {
        return true;
    }
}
