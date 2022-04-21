package personthecat.osv.compat.collector.create.forge;

import com.simibubi.create.foundation.worldgen.ConfigDrivenOreConfiguration;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
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
        return config instanceof ConfigDrivenOreConfiguration;
    }

    @Override
    public boolean featureContainsBlock(final FeatureConfiguration config, final BlockState state) {
        if (config instanceof ConfigDrivenOreConfiguration) {
            final ConfigDrivenOreConfiguration create = (ConfigDrivenOreConfiguration) config;
            for (final OreConfiguration.TargetBlockState target : create.targetStates) {
                if (state.equals(target.state)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void collectFeatureConfig(final ClusterSettingsBuilder builder, final FeatureConfiguration config) {
        if (config instanceof ConfigDrivenOreConfiguration) {
            builder.size(((ConfigDrivenOreConfiguration) config).getSize());
        }
    }
}
