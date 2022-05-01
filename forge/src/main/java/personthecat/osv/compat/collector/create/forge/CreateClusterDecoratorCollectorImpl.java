package personthecat.osv.compat.collector.create.forge;

import com.simibubi.create.foundation.worldgen.ConfigDrivenOreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.osv.compat.collector.create.CreateClusterPlacementCollector;
import personthecat.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;
import personthecat.osv.world.providers.SimpleCountProvider;
import personthecat.osv.world.providers.SimpleHeightProvider;

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
        if (config instanceof ConfigDrivenOreConfiguration create) {
            builder.count(new SimpleCountProvider(0, (int) create.getFrequency()));
            builder.height(new SimpleHeightProvider(create.getMinY(), create.getMaxY()));
        }
    }
}
