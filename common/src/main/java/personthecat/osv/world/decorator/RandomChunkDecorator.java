package personthecat.osv.world.decorator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

import java.util.Random;
import java.util.stream.Stream;

public class RandomChunkDecorator extends FeatureDecorator<RandomChunkDecoratorConfig> {

    public static final RandomChunkDecorator INSTANCE = new RandomChunkDecorator();

    private RandomChunkDecorator() {
        super(RandomChunkDecoratorConfig.CODEC);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext ctx, Random rand, RandomChunkDecoratorConfig cfg, BlockPos pos) {
        throw new UnsupportedOperationException();
    }
}
