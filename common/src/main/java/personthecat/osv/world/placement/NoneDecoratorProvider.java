package personthecat.osv.world.placement;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Collections;

public interface NoneDecoratorProvider<T> extends PlacementProvider<T> {

    @Override
    default PlacedFeature place(final ConfiguredFeature<?, ?> feature) {
        return new PlacedFeature(Holder.direct(feature), Collections.emptyList());
    }
}
