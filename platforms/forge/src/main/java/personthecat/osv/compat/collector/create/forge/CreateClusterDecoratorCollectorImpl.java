package personthecat.osv.compat.collector.create.forge;

import com.simibubi.create.foundation.worldgen.ConfigDrivenOreFeatureConfig;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import personthecat.catlib.data.Range;
import personthecat.osv.compat.collector.create.CreateClusterDecoratorCollector;
import personthecat.osv.preset.data.FlexibleDecoratorSettings.FlexibleDecoratorSettingsBuilder;

import java.util.Optional;

public class CreateClusterDecoratorCollectorImpl extends CreateClusterDecoratorCollector {

    private static final CreateClusterDecoratorCollector INSTANCE = new CreateClusterDecoratorCollectorImpl();

    private CreateClusterDecoratorCollectorImpl() {}

    public static Optional<CreateClusterDecoratorCollector> getInstance() {
        return Optional.of(INSTANCE);
    }

    @Override
    public boolean isDecoratorConfigSupported(final DecoratorConfiguration config) {
        return config instanceof ConfigDrivenOreFeatureConfig;
    }

    @Override
    public void collectDecoratorConfig(final FlexibleDecoratorSettingsBuilder builder, final DecoratorConfiguration config) {
        if (config instanceof ConfigDrivenOreFeatureConfig) {
            final ConfigDrivenOreFeatureConfig create = (ConfigDrivenOreFeatureConfig) config;
            builder.count(Range.of((int) create.getFrequency()));
            builder.height(Range.of(create.getMinY(), create.getMaxY()));
        }
    }
}
