package personthecat.osv.world.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import personthecat.catlib.data.DimensionPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class DimensionPlacementModifier extends PlacementModifier {

    public static final Codec<DimensionPlacementModifier> CODEC =
        DimensionPredicate.CODEC.xmap(DimensionPlacementModifier::new, m -> m.predicate);

    public static final PlacementModifierType<DimensionPlacementModifier> TYPE = () -> CODEC;

    private final DimensionPredicate predicate;

    public DimensionPlacementModifier(final DimensionPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext ctx, Random rand, BlockPos pos) {
        return this.predicate.test(ctx.getLevel()) ? Stream.of(pos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
