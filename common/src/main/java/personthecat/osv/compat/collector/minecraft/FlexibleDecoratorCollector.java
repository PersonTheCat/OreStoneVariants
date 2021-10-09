package personthecat.osv.compat.collector.minecraft;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.placement.*;
import personthecat.catlib.data.Range;
import personthecat.osv.preset.data.FlexibleDecoratorSettings;
import personthecat.osv.preset.data.FlexibleDecoratorSettings.FlexibleDecoratorSettingsBuilder;
import personthecat.osv.preset.collector.DecoratorCollector;

import java.util.Random;
import java.util.Set;

public class FlexibleDecoratorCollector extends DecoratorCollector<FlexibleDecoratorSettings, FlexibleDecoratorSettingsBuilder> {

    private static final Set<Class<? extends FeatureDecorator<?>>> SUPPORTED_DECORATORS =
        ImmutableSet.<Class<? extends FeatureDecorator<?>>>builder()
            .add(BiasedRangeDecorator.class)
            .add(VeryBiasedRangeDecorator.class)
            .add(Spread32Decorator.class)
            .add(EmeraldPlacementDecorator.class)
            .build();

    private static final Set<Class<? extends DecoratorConfiguration>> SUPPORTED_DECORATOR_CONFIGS =
        ImmutableSet.<Class<? extends DecoratorConfiguration>>builder()
            .add(CountConfiguration.class)
            .add(NoiseDependantDecoratorConfiguration.class)
            .add(NoiseCountFactorDecoratorConfiguration.class)
            .add(FrequencyWithExtraChanceDecoratorConfiguration.class)
            .add(RangeDecoratorConfiguration.class)
            .add(ChanceDecoratorConfiguration.class)
            .add(DepthAverageConfigation.class)
            .build();

    private static final Random DUMMY_RANDOM = new Random();

    public FlexibleDecoratorCollector() {
        super(FlexibleDecoratorSettings::builder, FlexibleDecoratorSettingsBuilder::build);
    }

    @Override
    public boolean isDecoratorSupported(final FeatureDecorator<?> decorator) {
        return SUPPORTED_DECORATORS.contains(decorator.getClass());
    }

    @Override
    public boolean isDecoratorConfigSupported(final DecoratorConfiguration decorator) {
        return SUPPORTED_DECORATOR_CONFIGS.contains(decorator.getClass());
    }

    @Override
    public void collectDecorator(final FlexibleDecoratorSettingsBuilder builder, final FeatureDecorator<?> decorator) {
        if (decorator instanceof BiasedRangeDecorator) {
            builder.bias(1);
        } else if (decorator instanceof VeryBiasedRangeDecorator) {
            builder.bias(2);
        } else if (decorator instanceof Spread32Decorator) {
            builder.spread(32);
        } else if (decorator instanceof EmeraldPlacementDecorator) {
            builder.count(Range.of(3, 8));
            builder.height(Range.of(4, 32));
        }
    }

    @Override
    public void collectDecoratorConfig(final FlexibleDecoratorSettingsBuilder builder, final DecoratorConfiguration config) {
        if (config instanceof CountConfiguration) {
            final int count = ((CountConfiguration) config).count().sample(DUMMY_RANDOM);
            builder.count(Range.of(count));
        } else if (config instanceof FrequencyWithExtraChanceDecoratorConfiguration) {
            final FrequencyWithExtraChanceDecoratorConfiguration cfg = (FrequencyWithExtraChanceDecoratorConfiguration) config;
            builder.count(Range.of(cfg.count));
            builder.extraChance(cfg.extraChance);
            builder.extraCount(cfg.extraCount);
        } else if (config instanceof RangeDecoratorConfiguration) {
            final RangeDecoratorConfiguration cfg = (RangeDecoratorConfiguration) config;
            builder.height(Range.of(cfg.bottomOffset, cfg.maximum - cfg.topOffset));
        } else if (config instanceof ChanceDecoratorConfiguration) {
            builder.chance(1.0 / (double) ((ChanceDecoratorConfiguration) config).chance);
        } else if (config instanceof DepthAverageConfigation) {
            final DepthAverageConfigation cfg = (DepthAverageConfigation) config;
            builder.height(Range.of(cfg.baseline - cfg.spread, cfg.baseline + cfg.spread));
        }
    }
}
