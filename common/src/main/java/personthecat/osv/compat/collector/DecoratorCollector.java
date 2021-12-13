package personthecat.osv.compat.collector;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import personthecat.osv.mixin.ConfiguredDecoratorAccessor;
import personthecat.osv.world.decorator.DecoratorProvider;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class DecoratorCollector<Settings extends DecoratorProvider<?>, Builder> {

    private final Supplier<Builder> creator;
    private final Function<Builder, Settings> build;

    public DecoratorCollector(final Supplier<Builder> creator, final Function<Builder, Settings> build) {
        this.creator = creator;
        this.build = build;
    }

    public boolean canCollect(final ConfiguredFeature<?, ?> configured) {
        final FeatureConfiguration config = configured.config();
        if (config instanceof DecoratedFeatureConfiguration) {
            final DecoratedFeatureConfiguration decorated = (DecoratedFeatureConfiguration) config;
            final FeatureDecorator<?> decorator = ((ConfiguredDecoratorAccessor<?>) decorated.decorator).getDecorator();
            final DecoratorConfiguration cfg = decorated.decorator.config();
            return this.isDecoratorSupported(decorator) || this.isDecoratorConfigSupported(cfg);
        }
        return false;
    }

    public Settings collect(final ConfiguredFeature<?, ?> configured) {
        final Builder builder = this.creator.get();
        this.collect(builder, configured);
        return this.build.apply(builder);
    }

    protected void collect(final Builder builder, final ConfiguredFeature<?, ?> configured) {
        final FeatureConfiguration config = configured.config();
        if (config instanceof DecoratedFeatureConfiguration) {
            final DecoratedFeatureConfiguration decorated = (DecoratedFeatureConfiguration) config;
            final FeatureDecorator<?> decorator = ((ConfiguredDecoratorAccessor<?>) decorated.decorator).getDecorator();
            final DecoratorConfiguration cfg = decorated.decorator.config();
            this.collectDecorator(builder, decorator);
            this.collectDecoratorConfig(builder, cfg);
            this.collect(builder, decorated.feature.get());
        }
    }

    public abstract boolean isDecoratorConfigSupported(final DecoratorConfiguration decorator);
    public abstract void collectDecoratorConfig(final Builder builder, final DecoratorConfiguration config);
    public boolean isDecoratorSupported(final FeatureDecorator<?> decorator) { return false; }
    public void collectDecorator(final Builder builder, final FeatureDecorator<?> decorator) {}
}
