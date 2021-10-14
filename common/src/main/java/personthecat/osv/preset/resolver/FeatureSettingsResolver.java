package personthecat.osv.preset.resolver;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.event.registry.DynamicRegistries;
import personthecat.osv.compat.ModCompat;
import personthecat.osv.compat.collector.FeatureCollector;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.data.GenerationSettings;;
import personthecat.osv.world.ConfiguredProvider;
import personthecat.osv.world.decorator.DecoratorProvider;
import personthecat.osv.world.feature.FeatureProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FeatureSettingsResolver {

    public static GenerationSettings resolveFeatures(final BlockState state) {
        final ImmutableList.Builder<DecoratedFeatureSettings<?, ?>> features = ImmutableList.builder();
        for (final ConfiguredProvider provider : resolveProviders(state)) {
            features.add(createSettings(provider, getBiomesOf(provider.getSource())));
        }
        return new GenerationSettings(features.build());
    }

    public static List<ConfiguredProvider> resolveProviders(final BlockState state) {
        final ImmutableList.Builder<ConfiguredProvider> list = ImmutableList.builder();
        for (final FeatureCollector<?, ?> collector : ModCompat.FEATURE_COLLECTORS) {
            for (final ConfiguredFeature<?, ?> feature : DynamicRegistries.CONFIGURED_FEATURES) {
                if (collector.canCollect(feature) && collector.matchesBlock(feature, state)) {
                    list.add(collect(collector, feature));
                }
            }
        }
        return list.build();
    }

    private static BiomePredicate getBiomesOf(final ConfiguredFeature<?, ?> feature) {
        final List<ResourceLocation> ids = new ArrayList<>();
        for (final Biome biome : DynamicRegistries.BIOMES) {
            if (containsFeature(biome, feature)) {
                ids.add(DynamicRegistries.BIOMES.getKey(biome));
            }
        }
        return BiomePredicate.builder().names(ids).build();
    }

    private static boolean containsFeature(final Biome biome, final ConfiguredFeature<?, ?> feature) {
        for (final List<Supplier<ConfiguredFeature<?, ?>>> suppliers : biome.getGenerationSettings().features()) {
            for (final Supplier<ConfiguredFeature<?, ?>> supplier : suppliers) {
                if (feature == supplier.get()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static DecoratedFeatureSettings<?, ?> createSettings(final ConfiguredProvider provider, final BiomePredicate biomes) {
        return DecoratedFeatureSettings.builder()
            .type(provider.getType())
            .config(provider.getFeature())
            .decorator(provider.getDecorator())
            .biomes(biomes)
            .build();
    }

    @Nullable
    public static ConfiguredProvider resolveProvider(final ResourceLocation id) {
        return resolveProvider(DynamicRegistries.CONFIGURED_FEATURES.lookup(id));
    }

    @Nullable
    public static ConfiguredProvider resolveProvider(final ConfiguredFeature<?, ?> feature) {
        for (final FeatureCollector<?, ?> collector : ModCompat.FEATURE_COLLECTORS) {
            if (collector.canCollect(feature)) {
                return collect(collector, feature);
            }
        }
        return null;
    }

    private static ConfiguredProvider collect(final FeatureCollector<?, ?> collector, final ConfiguredFeature<?, ?> feature) {
        final FeatureProvider<?> fp = collector.collect(feature);
        final DecoratorProvider<?> dp = collector.collectDecorator(feature);
        return new ConfiguredProvider(collector.featureType(), fp, dp, feature);
    }
}
