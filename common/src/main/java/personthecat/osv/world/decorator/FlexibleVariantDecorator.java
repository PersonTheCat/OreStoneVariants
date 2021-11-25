package personthecat.osv.world.decorator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FlexibleVariantDecorator extends FeatureDecorator<FlexibleDecoratorConfig> {

    public static final FlexibleVariantDecorator INSTANCE = new FlexibleVariantDecorator();

    private FlexibleVariantDecorator() {
        super(FlexibleDecoratorConfig.CODEC);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext ctx, Random rand, FlexibleDecoratorConfig cfg, BlockPos origin) {
        return IntStream.range(0, cfg.count.rand(rand))
            .filter(i -> cfg.chance == 1 || rand.nextFloat() <= cfg.chance)
            .mapToObj(i -> genPos(rand, cfg, origin));
    }

    private static BlockPos genPos(Random rand, FlexibleDecoratorConfig config, BlockPos origin) {
        return new BlockPos(
            rand.nextInt(16) + origin.getX(),
            config.height.rand(rand),
            rand.nextInt(16) + origin.getZ()
        );
    }
}
