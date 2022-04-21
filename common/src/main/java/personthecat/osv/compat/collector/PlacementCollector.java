package personthecat.osv.compat.collector;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import personthecat.osv.world.placement.PlacementProvider;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class PlacementCollector<Settings extends PlacementProvider<?>, Builder> {

    private final Supplier<Builder> creator;
    private final Function<Builder, Settings> build;

    public PlacementCollector(final Supplier<Builder> creator, final Function<Builder, Settings> build) {
        this.creator = creator;
        this.build = build;
    }

    public boolean canCollect(final PlacedFeature placed) {
        return placed.placement().stream().anyMatch(this::isPlacementSupported)
            || placed.getFeatures().anyMatch(cf -> this.isFeatureConfigSupported(cf.config()));
    }

    public Settings collect(final PlacedFeature placed) {
        final Builder builder = this.creator.get();
        placed.placement().forEach(modifier -> this.collectPlacement(builder, modifier));
        placed.getFeatures().forEach(cf -> this.collectFeatureConfig(builder, cf.config()));
        return this.build.apply(builder);
    }

    public boolean isPlacementSupported(final PlacementModifier modifier) {
        return false;
    }

    public void collectPlacement(final Builder builder, final PlacementModifier modifier) {}

    public boolean isFeatureConfigSupported(final FeatureConfiguration config) {
        return false;
    }

    public void collectFeatureConfig(final Builder builder, final FeatureConfiguration config) {}
}
