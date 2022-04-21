package personthecat.osv.preset.resolver;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.data.DimensionPredicate;
import personthecat.catlib.registry.DynamicRegistries;
import personthecat.osv.compat.ModCompat;
import personthecat.osv.compat.collector.FeatureCollector;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.world.ConfiguredProvider;
import personthecat.osv.world.placement.PlacementProvider;
import personthecat.osv.world.feature.FeatureProvider;

import java.util.ArrayList;
import java.util.List;

public class FeatureSettingsResolver {

    public static List<PlacedFeatureSettings<?, ?>> resolveFeatures(final BlockState state) {
        final ImmutableList.Builder<PlacedFeatureSettings<?, ?>> features = ImmutableList.builder();
        for (final ConfiguredProvider provider : resolveProviders(state)) {
            features.add(createSettings(provider, getBiomesOf(provider.getSource())));
        }
        return features.build();
    }

    public static List<ConfiguredProvider> resolveProviders(final BlockState state) {
        final ImmutableList.Builder<ConfiguredProvider> list = ImmutableList.builder();
        for (final FeatureCollector<?, ?> collector : ModCompat.FEATURE_COLLECTORS) {
            for (final PlacedFeature feature : DynamicRegistries.PLACED_FEATURES) {
                if (collector.canCollect(feature) && collector.matchesBlock(feature, state)) {
                    list.add(collect(collector, feature));
                }
            }
        }
        return list.build();
    }

    public static boolean featureContainsBlock(final PlacedFeature feature, final BlockState state) {
        for (final FeatureCollector<?, ?> collector : ModCompat.FEATURE_COLLECTORS) {
            if (collector.canCollect(feature) && collector.matchesBlock(feature, state)) {
                return true;
            }
        }
        return false;
    }

    private static BiomePredicate getBiomesOf(final PlacedFeature placed) {
        final List<ResourceLocation> ids = new ArrayList<>();
        for (final Biome biome : DynamicRegistries.BIOMES) {
            if (containsFeature(biome, placed)) {
                ids.add(DynamicRegistries.BIOMES.getKey(biome));
            }
        }
        return BiomePredicate.builder().names(ids).build().simplify();
    }

    private static boolean containsFeature(final Biome biome, final PlacedFeature placed) {
        for (final HolderSet<PlacedFeature> holders : biome.getGenerationSettings().features()) {
            for (final Holder<PlacedFeature> holder : holders) {
                if (placed == holder.value()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static PlacedFeatureSettings<?, ?> createSettings(final ConfiguredProvider provider, final BiomePredicate biomes) {
        return PlacedFeatureSettings.builder()
            .type(provider.getType())
            .config(provider.getFeature())
            .placement(provider.getDecorator())
            .biomes(biomes)
            .dimensions(DimensionPredicate.ALL_DIMENSIONS)
            .denseRatio(Cfg.denseChance())
            .build();
    }

    @Nullable
    public static ConfiguredProvider resolveProvider(final ResourceLocation id) {
        return resolveProvider(DynamicRegistries.PLACED_FEATURES.lookup(id));
    }

    @Nullable
    public static ConfiguredProvider resolveProvider(final PlacedFeature placed) {
        for (final FeatureCollector<?, ?> collector : ModCompat.FEATURE_COLLECTORS) {
            if (collector.canCollect(placed)) {
                return collect(collector, placed);
            }
        }
        return null;
    }

    private static ConfiguredProvider collect(final FeatureCollector<?, ?> collector, final PlacedFeature placed) {
        final FeatureProvider<?> fp = collector.collect(placed);
        final PlacementProvider<?> dp = collector.collectPlacement(placed);
        return new ConfiguredProvider(collector.featureType(), fp, dp, placed);
    }
}
