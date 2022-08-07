package personthecat.osv.world;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import personthecat.catlib.data.DimensionPredicate;
import personthecat.catlib.data.collections.MultiValueHashMap;
import personthecat.catlib.data.collections.MultiValueMap;
import personthecat.catlib.data.collections.LazyRegistry;
import personthecat.catlib.registry.CommonRegistries;
import personthecat.catlib.registry.DynamicRegistries;
import personthecat.catlib.event.world.FeatureModificationContext;
import personthecat.catlib.util.LibStringUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.preset.resolver.FeatureSettingsResolver;
import personthecat.osv.util.Reference;
import personthecat.osv.world.carver.*;
import personthecat.osv.world.placer.StoneBlockPlacer;
import personthecat.osv.world.placer.VariantBlockPlacer;

import java.util.*;

@Log4j2
public class OreGen {

    private static final LazyRegistry<ResourceLocation, PlacedFeature> DISABLED_FEATURES =
        LazyRegistry.of(OreGen::loadDisabledFeatures)
            .canBeReset(true);

    private static final LazyRegistry<ResourceLocation, Block> DISABLED_BLOCKS =
        LazyRegistry.of(OreGen::loadDisabledBlocks)
            .canBeReset(true);

    private static final LazyRegistry<ResourceLocation, MappedFeature> ENABLED_STONES =
        LazyRegistry.of(OreGen::loadStoneFeatures)
            .canBeReset(true);

    private static final LazyRegistry<ResourceLocation, MappedFeature> ENABLED_ORES =
        LazyRegistry.of(OreGen::loadOreFeatures)
            .canBeReset(true);

    private static final LazyRegistry<ResourceLocation, ConfiguredWorldCarver<?>> GLOBAL_STONES =
        LazyRegistry.of(OreGen::loadGlobalStones)
            .canBeReset(true);

    private static final LazyRegistry<ResourceLocation, ConfiguredWorldCarver<?>> GLOBAL_ORES =
        LazyRegistry.of(OreGen::loadGlobalOres)
            .canBeReset(true);

    public static void setupOreFeatures(final FeatureModificationContext ctx) {
        log.debug("Injecting changes to biome: {}", ctx.getName());

        DISABLED_FEATURES.forEach((id, feature) -> ctx.removeFeature(id));
        ENABLED_STONES.forEach((id, feature) -> {
            if (feature.canSpawn(ctx.getBiome())) {
                ctx.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature.getFeature());
            }
        });
        ENABLED_ORES.forEach((id, feature) -> {
            if (feature.canSpawn(ctx.getBiome())) {
                ctx.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, feature.getFeature());
            }
        });
        GLOBAL_STONES.forEach((id, carver) -> ctx.addCarver(GenerationStep.Carving.LIQUID, carver));
        GLOBAL_ORES.forEach((id, carver) -> ctx.addCarver(GenerationStep.Carving.LIQUID, carver));
    }

    public static void onWorldClosed() {
        LazyRegistry.resetAll(DISABLED_FEATURES, DISABLED_BLOCKS, ENABLED_ORES, ENABLED_STONES);
    }

    private static Map<ResourceLocation, PlacedFeature> loadDisabledFeatures() {
        final Map<ResourceLocation, PlacedFeature> features = new HashMap<>();
        addDynamicallyDisabledFeatures(features);
        addForciblyDisabledFeatures(features);
        return features;
    }

    private static void addDynamicallyDisabledFeatures(final Map<ResourceLocation, PlacedFeature> features) {
        DynamicRegistries.PLACED_FEATURES.forEach((id, feature) -> {
            if (isDisabled(feature)) {
                features.put(id, feature);
                log.debug("Feature {} was dynamically resolved. It will be disabled globally.", id);
            }
        });
    }

    private static boolean isDisabled(final PlacedFeature feature) {
        for (final Block block : DISABLED_BLOCKS) {
            if (FeatureSettingsResolver.featureContainsBlock(feature, block.defaultBlockState())) {
                return true;
            }
        }
        return false;
    }

    private static void addForciblyDisabledFeatures(final Map<ResourceLocation, PlacedFeature> features) {
        int numDisabled = 0;
        for (final String id : Cfg.disabledFeatures()) {
            final ResourceLocation key = new ResourceLocation(id);
            final Feature<?> feature = Registry.FEATURE.get(key);
            if (feature != null) {
                for (final PlacedFeature pf : DynamicRegistries.PLACED_FEATURES) {
                    if (pf.getFeatures().anyMatch(f -> feature.equals(f.feature()))) {
                        final ResourceLocation location = DynamicRegistries.PLACED_FEATURES.getKey(pf);
                        features.put(Objects.requireNonNull(location), pf);
                        log.debug("Adding {} to disabled features (matching {}). It will be disabled globally.", location, id);
                        numDisabled++;
                    }
                }
            } else if (DynamicRegistries.PLACED_FEATURES.isRegistered(key)) {
                final PlacedFeature pf = DynamicRegistries.PLACED_FEATURES.lookup(key);
                features.put(key, Objects.requireNonNull(pf));
                log.debug("Adding {} to disabled features. It will be disabled globally.", id);
                numDisabled++;
            } else {
                log.error("Cannot disable {}: no such feature. Ignoring...", id);
            }
        }
        log.info("Forcibly disabled {} features from {} entries.", numDisabled, Cfg.disabledFeatures().size());
    }

    private static Map<ResourceLocation, Block> loadDisabledBlocks() {
        final Map<ResourceLocation, Block> disabled = new HashMap<>();
        if (Cfg.autoDisableStone()) {
            addDisabledStones(disabled);
        }
        if (Cfg.autoDisableOres()) {
            addDisabledOres(disabled);
        }
        return disabled;
    }

    private static void addDisabledStones(final Map<ResourceLocation, Block> disabled) {
        for (final StonePreset preset : ModRegistries.STONE_PRESETS) {
            final Block stone = preset.getStone().getBlock();
            disabled.put(CommonRegistries.BLOCKS.getKey(stone), stone);
        }
    }

    private static void addDisabledOres(final Map<ResourceLocation, Block> disabled) {
        for (final OreVariant variant : ModRegistries.VARIANTS) {
            final Block ore = variant.getFg();
            disabled.put(CommonRegistries.BLOCKS.getKey(ore), ore);
        }
    }

    private static Map<ResourceLocation, MappedFeature> loadStoneFeatures() {
        final Map<ResourceLocation, MappedFeature> features = new HashMap<>();
        if (Cfg.enableOSVStone()) {
            addStoneFeatures(features);
        }
        return features;
    }

    private static void addStoneFeatures(final Map<ResourceLocation, MappedFeature> features) {
        for (final StonePreset preset : ModRegistries.STONE_PRESETS) {
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (!cfg.isGlobal()) {
                    final ResourceLocation id = randId("stone_");
                    final MappedFeature feature = cfg.createStoneFeature(preset);
                    Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, feature.getFeature().feature().value());
                    Registry.register(BuiltinRegistries.PLACED_FEATURE, id, feature.getFeature());
                    features.put(id, feature);
                }
            }
        }
    }

    private static Map<ResourceLocation, MappedFeature> loadOreFeatures() {
        final Map<ResourceLocation, MappedFeature> features = new HashMap<>();
        if (Cfg.enableOSVOres()) {
            addOreFeatures(features);
        }
        return features;
    }

    private static void addOreFeatures(final Map<ResourceLocation, MappedFeature> features) {
        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (!cfg.isGlobal()) {
                    final ResourceLocation id = randId("ore_");
                    final MappedFeature feature = cfg.createOreFeature(preset);
                    Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, feature.getFeature().feature().value());
                    Registry.register(BuiltinRegistries.PLACED_FEATURE, id, feature.getFeature());
                    features.put(id, feature);
                }
            }
        }
    }

    private static Map<ResourceLocation, ConfiguredWorldCarver<?>> loadGlobalStones() {
        final Map<ResourceLocation, ConfiguredWorldCarver<?>> features = new HashMap<>();
        if (Cfg.enableOSVStone()) {
            addGlobalStones(features);
        }
        return features;
    }

    private static void addGlobalStones(final Map<ResourceLocation, ConfiguredWorldCarver<?>> features) {
        final MultiValueMap<GlobalFeature<?>, FeatureStem> globalConfigs = new MultiValueHashMap<>();
        for (final StonePreset preset : ModRegistries.STONE_PRESETS) {
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (cfg.isGlobal()) {
                    final GlobalFeatureProvider<?> provider = (GlobalFeatureProvider<?>) cfg.getConfig();
                    globalConfigs.add(provider.getFeatureType(), new FeatureStem(cfg, new StoneBlockPlacer(preset)));
                }
            }
        }
        for (final ConfiguredWorldCarver<?> carver : sort(globalConfigs)) {
            features.put(randId("global_stone_"), carver);
        }
    }

    private static Map<ResourceLocation, ConfiguredWorldCarver<?>> loadGlobalOres() {
        final Map<ResourceLocation, ConfiguredWorldCarver<?>> features = new HashMap<>();
        if (Cfg.enableOSVOres()) {
            addGlobalOres(features);
        }
        return features;
    }

    private static void addGlobalOres(final Map<ResourceLocation, ConfiguredWorldCarver<?>> features) {
        final MultiValueMap<GlobalFeature<?>, FeatureStem> globalConfigs = new MultiValueHashMap<>();
        for (final OrePreset preset : ModRegistries.ORE_PRESETS) {
            for (final PlacedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (cfg.isGlobal()) {
                    final GlobalFeatureProvider<?> provider = (GlobalFeatureProvider<?>) cfg.getConfig();
                    globalConfigs.add(provider.getFeatureType(), new FeatureStem(cfg, new VariantBlockPlacer(cfg, preset)));
                }
            }
        }
        for (final ConfiguredWorldCarver<?> carver : sort(globalConfigs)) {
            features.put(randId("global_ore_"), carver);
        }
    }

    private static List<ConfiguredWorldCarver<?>> sort(final MultiValueMap<GlobalFeature<?>, FeatureStem> globals) {
        final List<ConfiguredWorldCarver<?>> carvers = new ArrayList<>();
        globals.forEach((feature, stems) -> {
            final MultiValueMap<DimensionPredicate, FeatureStem> sorted = new MultiValueHashMap<>();
            for (final FeatureStem stem : stems) {
                sorted.add(stem.getConfig().getDimensions(), stem);
            }
            sorted.forEach((dims, sortedStems) -> {
                final ConfiguredWorldCarver<?> configured = feature.configured(sortedStems);
                if (dims.equals(DimensionPredicate.ALL_DIMENSIONS)) {
                    carvers.add(configured);
                } else {
                    carvers.add(DimensionLocalCarver.INSTANCE.configured(
                        new DimensionLocalCarverConfig(dims, configured)));
                }
            });
        });
        return carvers;
    }

    private static ResourceLocation randId(final String prefix) {
        return new ResourceLocation(Reference.MOD_ID, prefix + LibStringUtils.randId(8));
    }
}
