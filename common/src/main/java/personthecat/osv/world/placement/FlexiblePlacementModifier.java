package personthecat.osv.world.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import personthecat.osv.preset.reader.HeightProviderReader;
import personthecat.osv.preset.reader.IntProviderReader;
import personthecat.osv.world.providers.OffsetHeightProvider;
import personthecat.osv.world.providers.SimpleCountProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

@ParametersAreNonnullByDefault
public class FlexiblePlacementModifier extends PlacementModifier {

    public static final Codec<FlexiblePlacementModifier> CODEC = codecOf(
        defaulted(IntProviderReader.CODEC, "count", new SimpleCountProvider(8, 8), c -> c.count),
        defaulted(HeightProviderReader.CODEC, "height", new OffsetHeightProvider(0, 128), c -> c.height),
        defaulted(Codec.INT, "bias", 0, c -> c.bias),
        defaulted(Codec.DOUBLE, "chance", 1.0, c -> c.chance),
        FlexiblePlacementModifier::new
    );

    public static final PlacementModifierType<FlexiblePlacementModifier> TYPE = () -> CODEC;

    public final IntProvider count;
    public final HeightProvider height;
    public final int bias;
    public final double chance;

    public FlexiblePlacementModifier(final IntProvider count, final HeightProvider height, final int bias, final double chance) {
        this.count = count;
        this.height = height;
        this.bias = bias;
        this.chance = chance;
    }

    @Override
    public Stream<BlockPos> getPositions(final PlacementContext ctx, final Random rand, final BlockPos origin) {
        return IntStream.range(0, this.count.sample(rand))
            .filter(i -> this.chance == 1 || rand.nextFloat() <= this.chance)
            .mapToObj(i -> this.genPos(rand, ctx, origin));
    }

    private BlockPos genPos(final Random rand, final PlacementContext ctx, final BlockPos origin) {
        return new BlockPos(
            rand.nextInt(16) + origin.getX(),
            this.genHeight(rand, ctx),
            rand.nextInt(16) + origin.getZ()
        );
    }

    private int genHeight(final Random rand, final PlacementContext ctx) {
        final int offset = -ctx.getMinGenY();
        int y = this.height.sample(rand, ctx) + offset;
        for (int i = 0; y > 0 && i < this.bias; i++) {
            y = rand.nextInt(y + 1);
        }
        return y - offset;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
