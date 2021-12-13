package personthecat.osv.compat.collector.create.forge;

import com.simibubi.create.foundation.worldgen.ConfigDrivenOreFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.osv.compat.collector.create.CreateClusterCollector;
import personthecat.osv.preset.data.ClusterSettings.ClusterSettingsBuilder;

import java.util.Optional;

public class CreateClusterCollectorImpl extends CreateClusterCollector {

    private static final CreateClusterCollector INSTANCE = new CreateClusterCollectorImpl();

    private CreateClusterCollectorImpl() {}

    public static Optional<CreateClusterCollector> getInstance() {
        return Optional.of(INSTANCE);
    }

    @Override
    public boolean isFeatureConfigSupported(final FeatureConfiguration config) {
        return config instanceof ConfigDrivenOreFeatureConfig;
    }

    @Override
    public boolean featureContainsBlock(final FeatureConfiguration config, final BlockState state) {
        return config instanceof ConfigDrivenOreFeatureConfig
            && state.equals(((ConfigDrivenOreFeatureConfig) config).state);
    }

    @Override
    public void collectFeatureConfig(final ClusterSettingsBuilder builder, final FeatureConfiguration config) {
        if (config instanceof ConfigDrivenOreFeatureConfig)
            builder.size(((ConfigDrivenOreFeatureConfig) config).getSize());
    }
}
