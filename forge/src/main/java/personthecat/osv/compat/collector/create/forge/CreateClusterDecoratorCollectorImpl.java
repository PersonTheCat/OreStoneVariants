package personthecat.osv.compat.collector.create.forge;

import com.simibubi.create.foundation.worldgen.ConfigDrivenOreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.data.Range;
import personthecat.osv.compat.collector.create.CreateClusterPlacementCollector;
import personthecat.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;

import java.util.Optional;

public class CreateClusterDecoratorCollectorImpl extends CreateClusterPlacementCollector {

    private static final CreateClusterPlacementCollector INSTANCE = new CreateClusterDecoratorCollectorImpl();

    private CreateClusterDecoratorCollectorImpl() {}

    public static Optional<CreateClusterPlacementCollector> getInstance() {
        return Optional.of(INSTANCE);
    }

    @Override
    public boolean isFeatureConfigSupported(final FeatureConfiguration config) {
        return config instanceof ConfigDrivenOreConfiguration;
    }

    @Override
    public void collectFeatureConfig(final FlexiblePlacementSettingsBuilder builder, final FeatureConfiguration config) {
        if (config instanceof ConfigDrivenOreConfiguration) {
            final ConfigDrivenOreConfiguration create = (ConfigDrivenOreConfiguration) config;
            builder.count(Range.of((int) create.getFrequency()));
            builder.height(Range.of(create.getMinY(), create.getMaxY()));
        }
    }
}
