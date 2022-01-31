package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.data.DimensionPredicate;

import java.util.Random;

public class DimensionLocalFeature<FC extends FeatureConfiguration, F extends Feature<FC>> extends ConfiguredFeature<FC, F> {

    private final DimensionPredicate predicate;

    public DimensionLocalFeature(final DimensionPredicate dims, final F feature, final FC featureConfiguration) {
        super(feature, featureConfiguration);
        this.predicate = dims;
    }

    @Override
    public boolean place(final WorldGenLevel level, final ChunkGenerator chunk, final Random rand, final BlockPos pos) {
        if (this.predicate.test(level.dimensionType())) {
            return super.place(level, chunk, rand, pos);
        }
        return false;
    }
}
