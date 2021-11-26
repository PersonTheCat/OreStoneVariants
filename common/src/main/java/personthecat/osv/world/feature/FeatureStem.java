package personthecat.osv.world.feature;

import lombok.Value;
import personthecat.osv.preset.data.DecoratedFeatureSettings;

@Value
public class FeatureStem {
    DecoratedFeatureSettings<?, ?> config;
    BlockPlacer placer;
}
