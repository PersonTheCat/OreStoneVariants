package personthecat.osv.compat.collector.minecraft;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import personthecat.osv.compat.collector.FeatureCollector;
import personthecat.osv.preset.data.ClusterSettings;
import personthecat.osv.preset.data.ClusterSettings.ClusterSettingsBuilder;

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
            for (final OreConfiguration.TargetBlockState target : ((OreConfiguration) config).targetStates) {
                if (target.state.equals(state)) {
                    return true;
                }
            }
        } else if (config instanceof ReplaceBlockConfiguration) {
            for (final OreConfiguration.TargetBlockState target : ((ReplaceBlockConfiguration) config).targetStates) {
                if (target.state.equals(state)) {
                    return true;
                }
            }
        } else if (config instanceof DiskConfiguration) {
            for (final BlockState target : ((DiskConfiguration) config).targets()) {
                if (target.equals(state)) {
                    return true;
                }
            }
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
            final IntProvider radius = cfg.radius();
            builder.size(((radius.getMinValue() + radius.getMaxValue()) / 2) * 2);
        }
    }
}
