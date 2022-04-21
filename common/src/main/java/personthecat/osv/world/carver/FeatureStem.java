package personthecat.osv.world.carver;

import lombok.Value;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.world.placer.BlockPlacer;

@Value
public class FeatureStem {
    PlacedFeatureSettings<?, ?> config;
    BlockPlacer placer;
}
