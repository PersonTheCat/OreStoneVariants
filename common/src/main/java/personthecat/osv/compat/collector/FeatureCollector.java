package personthecat.osv.compat.collector;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.osv.compat.ModCompat;
import personthecat.osv.exception.EmptyFeatureException;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.data.FlexibleDecoratorSettings;
import personthecat.osv.util.Reference;
import personthecat.osv.world.decorator.DecoratorProvider;
import personthecat.osv.world.feature.FeatureProvider;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FeatureCollector<Settings extends FeatureProvider<?>, Builder> {

    private final Supplier<Builder> creator;
    private final Function<Builder, Settings> build;

    public FeatureCollector(final Supplier<Builder> creator, final Function<Builder, Settings> build) {
        this.creator = creator;
        this.build = build;
    }

    protected Class<? extends DecoratorProvider<?>> decoratorType() {
        return FlexibleDecoratorSettings.class;
    }

    public DecoratedFeatureSettings.Type featureType() {
        return DecoratedFeatureSettings.Type.CLUSTER;
    }

    protected final Collection<DecoratorCollector<?, ?>> decoratorCollectors() {
        return ModCompat.DECORATOR_COLLECTORS.get(this.decoratorType());
    }

    public boolean canCollect(final ConfiguredFeature<?, ?> configured) {
        if (configured == null) {
            return false;
        }
        final FeatureConfiguration config = configured.config();
        if (config instanceof DecoratedFeatureConfiguration) {
            final DecoratedFeatureConfiguration decorated = (DecoratedFeatureConfiguration) config;
            return this.canCollect(decorated.feature.get());
        }
        final Feature<?> feature = configured.feature();
        if (feature == null) {
            LibErrorContext.error(Reference.MOD, new EmptyFeatureException(configured));
            return false;
        }
        return this.isFeatureSupported(feature) || this.isFeatureConfigSupported(config);
    }

    public boolean matchesBlock(final ConfiguredFeature<?, ?> configured, final BlockState state) {
        return configured.getFeatures().anyMatch(cf -> this.featureContainsBlock(cf.config(), state));
    }

    public Settings collect(final ConfiguredFeature<?, ?> configured) {
        final Builder builder = this.creator.get();
        this.collect(builder, configured);
        return this.build.apply(builder);
    }

    public DecoratorProvider<?> collectDecorator(final ConfiguredFeature<?, ?> configured) {
        for (final DecoratorCollector<?, ?> collector : this.decoratorCollectors()) {
            if (collector.canCollect(configured)) {
                return collector.collect(configured);
            }
        }
        return this.defaultDecorator();
    }

    protected DecoratorProvider<?> defaultDecorator() {
        return FlexibleDecoratorSettings.DEFAULTS;
    }

    protected void collect(final Builder builder, final ConfiguredFeature<?, ?> configured) {
        final FeatureConfiguration config = configured.config();
        if (config instanceof DecoratedFeatureConfiguration) {
            final DecoratedFeatureConfiguration decorated = (DecoratedFeatureConfiguration) config;
            this.collect(builder, decorated.feature.get());
        } else {
            final Feature<?> feature = configured.feature();
            this.collectFeatureConfig(builder, config);
            this.collectFeature(builder, feature);
        }
    }

    public abstract boolean isFeatureConfigSupported(final FeatureConfiguration feature);
    public abstract boolean featureContainsBlock(final FeatureConfiguration config, final BlockState state);
    public abstract void collectFeatureConfig(final Builder builder, final FeatureConfiguration config);
    public boolean isFeatureSupported(final Feature<?> feature) { return false; }
    public void collectFeature(final Builder builder, final Feature<?> feature) {}
}
