package personthecat.osv.world;

import lombok.Value;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.world.feature.FeatureProvider;
import personthecat.osv.world.placement.PlacementProvider;

@Value
public class ConfiguredProvider {
    PlacedFeatureSettings.Type type;
    FeatureProvider<?> feature;
    PlacementProvider<?> decorator;
    PlacedFeature source;
}
