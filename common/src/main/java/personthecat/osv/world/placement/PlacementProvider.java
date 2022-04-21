package personthecat.osv.world.placement;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import personthecat.osv.preset.data.DynamicSerializable;

public interface PlacementProvider<T> extends DynamicSerializable<T> {
    PlacedFeature place(final ConfiguredFeature<?, ?> feature);
}
