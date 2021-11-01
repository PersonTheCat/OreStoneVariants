package personthecat.osv.world.decorator;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.CodecUtils.simpleEither;
import static personthecat.catlib.serialization.FieldDescriptor.field;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

public class RandomChunkDecoratorConfig implements DecoratorConfiguration {

    private static final Codec<RandomChunkDecoratorConfig> SEED_CODEC = codecOf(
        field(Codec.INT, "seed", c -> c.seed),
        defaulted(Codec.DOUBLE, "chance", 1.0, c -> c.chance),
        RandomChunkDecoratorConfig::new
    );

    private static final Codec<RandomChunkDecoratorConfig> STATE_CODEC = codecOf(
        field(BlockState.CODEC, "state", c -> Block.stateById(c.seed)),
        defaulted(Codec.DOUBLE, "chance", 1.0, c -> c.chance),
        RandomChunkDecoratorConfig::new
    );

    public static final Codec<RandomChunkDecoratorConfig> CODEC = simpleEither(SEED_CODEC, STATE_CODEC);

    final int seed;
    final double chance;
    final double threshold;

    public RandomChunkDecoratorConfig(final int seed, final double chance) {
        this.seed = seed;
        this.chance = chance;
        this.threshold = 91.0 * chance;
    }

    public RandomChunkDecoratorConfig(final BlockState state, final double chance) {
        this(Block.getId(state), chance);
    }
}
