package personthecat.osv.compat.collector.minecraft;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import personthecat.osv.mixin.UniformIntAccessor;
import personthecat.osv.preset.data.ClusterSettings;
import personthecat.osv.preset.data.ClusterSettings.ClusterSettingsBuilder;
import personthecat.osv.compat.collector.FeatureCollector;

import java.util.Set;

public class ClusterFeatureCollector extends FeatureCollector<ClusterSettings, ClusterSettingsBuilder> {

    private static final Set<Class<? extends FeatureConfiguration>> SUPPORTED_FEATURE_CONFIGS =
        ImmutableSet.<Class<? extends FeatureConfiguration>>builder()
            .add(OreConfiguration.class)
            .add(ReplaceBlockConfiguration.class)
            .add(DiskConfiguration.class)
            .build();

    public ClusterFeatureCollector() {
        super(ClusterSettings::builder, ClusterSettingsBuilder::build);
    }

    @Override
    public boolean isFeatureConfigSupported(final FeatureConfiguration feature) {
        return SUPPORTED_FEATURE_CONFIGS.contains(feature.getClass());
    }

    @Override
    public boolean featureContainsBlock(final FeatureConfiguration config, final BlockState state) {
        if (config instanceof OreConfiguration) {
            return state.equals(((OreConfiguration) config).state);
        } else if (config instanceof ReplaceBlockConfiguration) {
            return state.equals(((ReplaceBlockConfiguration) config).state);
        } else if (config instanceof DiskConfiguration) {
            return state.equals(((DiskConfiguration) config).state);
        }
        return false;
    }

    @Override
    public void collectFeatureConfig(final ClusterSettingsBuilder builder, final FeatureConfiguration config) {
        if (config instanceof OreConfiguration) {
            builder.size(((OreConfiguration) config).size);
        } else if (config instanceof ReplaceBlockConfiguration) {
            builder.size(3);
        } else if (config instanceof DiskConfiguration) {
            // Todo: just an approximation until we get a sphere generator
            final DiskConfiguration cfg = (DiskConfiguration) config;
            final UniformIntAccessor radius = (UniformIntAccessor) cfg.radius;
            builder.size(radius.getBaseValue() * 2);
        }
    }
}
