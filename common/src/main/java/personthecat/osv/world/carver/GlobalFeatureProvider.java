package personthecat.osv.world.carver;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.world.feature.FeatureProvider;

public interface GlobalFeatureProvider<T> extends FeatureProvider<T> {

    @Override
    @Deprecated
    default ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(Feature.NO_OP, FeatureConfiguration.NONE);
    }

    @Override
    @Deprecated
    default ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(Feature.NO_OP, FeatureConfiguration.NONE);
    }

    GlobalFeature<?> getFeatureType();
}
