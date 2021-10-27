package personthecat.osv.world;

import lombok.Value;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.world.decorator.DecoratorProvider;
import personthecat.osv.world.feature.FeatureProvider;

@Value
public class ConfiguredProvider {
    DecoratedFeatureSettings.Type type;
    FeatureProvider<?> feature;
    DecoratorProvider<?> decorator;
    ConfiguredFeature<?, ?> source;
}
