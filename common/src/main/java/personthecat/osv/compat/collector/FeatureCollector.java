package personthecat.osv.compat.collector;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.osv.compat.ModCompat;
import personthecat.osv.exception.EmptyFeatureException;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.preset.data.FlexiblePlacementSettings;
import personthecat.osv.util.Reference;
import personthecat.osv.world.placement.PlacementProvider;
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

    protected Class<? extends PlacementProvider<?>> decoratorType() {
        return FlexiblePlacementSettings.class;
    }

    public PlacedFeatureSettings.Type featureType() {
        return PlacedFeatureSettings.Type.CLUSTER;
    }

    protected final Collection<PlacementCollector<?, ?>> decoratorCollectors() {
        return ModCompat.DECORATOR_COLLECTORS.get(this.decoratorType());
    }

    public boolean canCollect(final PlacedFeature placed) {
        return this.canCollect(placed.feature().value());
    }

    public boolean canCollect(final ConfiguredFeature<?, ?> configured) {
        if (configured == null) {
            return false;
        }
        final FeatureConfiguration config = configured.config();
        final Feature<?> feature = configured.feature();
        if (config == null || feature == null) {
            LibErrorContext.error(Reference.MOD, new EmptyFeatureException(configured));
            return false;
        }
        return this.isFeatureSupported(feature) || this.isFeatureConfigSupported(config);
    }

    public boolean matchesBlock(final PlacedFeature placed, final BlockState state) {
        return this.matchesBlock(placed.feature().value(), state);
    }

    public boolean matchesBlock(final ConfiguredFeature<?, ?> configured, final BlockState state) {
        return configured.getFeatures().anyMatch(cf -> this.featureContainsBlock(cf.config(), state));
    }

    public Settings collect(final PlacedFeature placed) {
        final Builder builder = this.creator.get();
        this.collect(builder, placed);
        return this.build.apply(builder);
    }

    public PlacementProvider<?> collectPlacement(final PlacedFeature placed) {
        for (final PlacementCollector<?, ?> collector : this.decoratorCollectors()) {
            if (collector.canCollect(placed)) {
                return collector.collect(placed);
            }
        }
        return this.defaultPlacement();
    }

    protected PlacementProvider<?> defaultPlacement() {
        return FlexiblePlacementSettings.DEFAULTS;
    }

    protected void collect(final Builder builder, final PlacedFeature placed) {
        this.collectFeatureConfig(builder, placed.feature().value().config());
        this.collectFeature(builder, placed.feature().value().feature());
    }

    public abstract boolean isFeatureConfigSupported(final FeatureConfiguration feature);
    public abstract boolean featureContainsBlock(final FeatureConfiguration config, final BlockState state);
    public abstract void collectFeatureConfig(final Builder builder, final FeatureConfiguration config);
    public boolean isFeatureSupported(final Feature<?> feature) { return false; }
    public void collectFeature(final Builder builder, final Feature<?> feature) {}
}
