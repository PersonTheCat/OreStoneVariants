package personthecat.osv.world.carver;

import lombok.Value;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.world.placer.BlockPlacer;

@Value
public class FeatureStem {
    DecoratedFeatureSettings<?, ?> config;
    BlockPlacer placer;
}
