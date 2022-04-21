package personthecat.osv.world.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import personthecat.catlib.data.Range;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

@ParametersAreNonnullByDefault
public class FlexiblePlacementModifier extends PlacementModifier {

    public static final Codec<FlexiblePlacementModifier> CODEC = codecOf(
        defaulted(Range.CODEC, "count", Range.of(8, 8), c -> c.count),
        defaulted(Range.CODEC, "height", Range.of(0, 32), c -> c.height),
        defaulted(Codec.INT, "bias", 0, c -> c.bias),
        defaulted(Codec.DOUBLE, "chance", 1.0, c -> c.chance),
        FlexiblePlacementModifier::new
    );

    public static final PlacementModifierType<FlexiblePlacementModifier> TYPE = () -> CODEC;

    private final Range count;
    private final Range height;
    private final int offset;
    private final int bias;
    private final double chance;

    public FlexiblePlacementModifier(final Range count, final Range height, final int bias, final double chance) {
        this.count = count;
        this.height = height;
        this.offset = -Math.min(height.min, 0);
        this.bias = bias;
        this.chance = chance;
    }

    @Override
    public Stream<BlockPos> getPositions(final PlacementContext ctx, final Random rand, final BlockPos origin) {
        return IntStream.range(0, this.count.rand(rand))
            .filter(i -> this.chance == 1 || rand.nextFloat() <= this.chance)
            .mapToObj(i -> this.genPos(rand, origin));
    }

    private BlockPos genPos(final Random rand, final BlockPos origin) {
        return new BlockPos(
            rand.nextInt(16) + origin.getX(),
            this.genHeight(rand),
            rand.nextInt(16) + origin.getZ()
        );
    }

    private int genHeight(final Random rand) {
        int y = this.offset + this.height.rand(rand);
        for (int i = 0; y > 0 && i < this.bias; i++) {
            y = rand.nextInt(y + 1);
        }
        return y - this.offset;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
