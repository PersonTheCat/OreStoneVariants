package personthecat.osv.world.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import personthecat.catlib.data.DimensionPredicate;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

public class DimensionLocalCarver<WC extends CarverConfiguration> extends ConfiguredWorldCarver<WC> {

    private final DimensionPredicate predicate;

    public DimensionLocalCarver(final DimensionPredicate dims, final WorldCarver<WC> carver, final WC featureConfiguration) {
        super(carver, featureConfiguration);
        this.predicate = dims;
    }

    @Override
    public boolean carve(ChunkAccess chunk, Function<BlockPos, Biome> biomes, Random rand, int i, int j, int k, int l, int m, BitSet mask) {
        if (this.predicate.test(chunk)) {
            return super.carve(chunk, biomes, rand, i, j, k, l, m, mask);
        }
        return false;
    }
}
