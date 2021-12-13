package personthecat.osv.world;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import personthecat.catlib.data.MultiValueHashMap;
import personthecat.catlib.data.MultiValueMap;
import personthecat.catlib.data.SafeRegistry;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.event.registry.DynamicRegistries;
import personthecat.catlib.event.world.FeatureModificationContext;
import personthecat.catlib.util.LibStringUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.resolver.FeatureSettingsResolver;
import personthecat.osv.util.Reference;
import personthecat.osv.world.carver.FeatureStem;
import personthecat.osv.world.carver.GlobalFeature;
import personthecat.osv.world.carver.GlobalFeatureProvider;
import personthecat.osv.world.placer.StoneBlockPlacer;
import personthecat.osv.world.placer.VariantBlockPlacer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class OreGen {

    private static final SafeRegistry<ResourceLocation, ConfiguredFeature<?, ?>> DISABLED_FEATURES =
        SafeRegistry.of(OreGen::loadDisabledFeatures)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, Block> DISABLED_BLOCKS =
        SafeRegistry.of(OreGen::loadDisabledBlocks)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, MappedFeature> ENABLED_STONES =
        SafeRegistry.of(OreGen::loadStoneFeatures)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, MappedFeature> ENABLED_ORES =
        SafeRegistry.of(OreGen::loadOreFeatures)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, ConfiguredWorldCarver<?>> GLOBAL_STONES =
        SafeRegistry.of(OreGen::loadGlobalStones)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, ConfiguredWorldCarver<?>> GLOBAL_ORES =
        SafeRegistry.of(OreGen::loadGlobalOres)
            .canBeReset(true);

    public static void setupOreFeatures(final FeatureModificationContext ctx) {
        log.debug("Injecting changes to biome: {}", ctx.getName());

        DISABLED_FEATURES.forEach((id, feature) -> ctx.removeFeature(id));
        ENABLED_STONES.forEach((id, feature) -> {
            if (feature.getBiomes().test(ctx.getBiome())) {
                ctx.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature.getFeature());
            }
        });
        ENABLED_ORES.forEach((id, feature) -> {
            if (feature.getBiomes().test(ctx.getBiome())) {
                ctx.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, feature.getFeature());
            }
        });
        GLOBAL_STONES.forEach((id, carver) -> ctx.addCarver(GenerationStep.Carving.LIQUID, carver));
        GLOBAL_ORES.forEach((id, carver) -> ctx.addCarver(GenerationStep.Carving.LIQUID, carver));
    }

    public static void onWorldClosed() {
        SafeRegistry.resetAll(DISABLED_FEATURES, DISABLED_BLOCKS, ENABLED_ORES, ENABLED_STONES);
    }

    private static Map<ResourceLocation, ConfiguredFeature<?, ?>> loadDisabledFeatures() {
        final Map<ResourceLocation, ConfiguredFeature<?, ?>> features = new HashMap<>();
        addDynamicallyDisabledFeatures(features);
        addForciblyDisabledFeatures(features);
        return features;
    }

    private static void addDynamicallyDisabledFeatures(final Map<ResourceLocation, ConfiguredFeature<?, ?>> features) {
        DynamicRegistries.CONFIGURED_FEATURES.forEach((id, feature) -> {
            if (isDisabled(feature)) {
                features.put(id, feature);
                log.debug("Feature {} was dynamically resolved. It will be disabled globally.", id);
            }
        });
    }

    private static boolean isDisabled(final ConfiguredFeature<?, ?> feature) {
        for (final Block block : DISABLED_BLOCKS) {
            if (FeatureSettingsResolver.featureContainsBlock(feature, block.defaultBlockState())) {
                return true;
            }
        }
        return false;
    }

    private static void addForciblyDisabledFeatures(final Map<ResourceLocation, ConfiguredFeature<?, ?>> features) {
        int numDisabled = 0;
        for (final String id : Cfg.disabledFeatures()) {
            final ResourceLocation key = new ResourceLocation(id);
            final Feature<?> feature = Registry.FEATURE.get(key);
            if (feature != null) {
                for (final ConfiguredFeature<?, ?> cf : DynamicRegistries.CONFIGURED_FEATURES) {
                    if (cf.getFeatures().anyMatch(f -> feature.equals(f.feature))) {
                        final ResourceLocation location = DynamicRegistries.CONFIGURED_FEATURES.getKey(cf);
                        features.put(Objects.requireNonNull(location), cf);
                        log.debug("Adding {} to disabled features (matching {}). It will be disabled globally.", location, id);
                        numDisabled++;
                    }
                }
            } else if (DynamicRegistries.CONFIGURED_FEATURES.isRegistered(key)) {
                final ConfiguredFeature<?, ?> cf = DynamicRegistries.CONFIGURED_FEATURES.lookup(key);
                features.put(key, Objects.requireNonNull(cf));
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
            for (final DecoratedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (!cfg.isGlobal()) {
                    final ResourceLocation id = randId("stone_");
                    final MappedFeature feature = cfg.createStoneFeature(preset);
                    Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, feature.getFeature());
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
            for (final DecoratedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (!cfg.isGlobal()) {
                    final ResourceLocation id = randId("ore_");
                    final MappedFeature feature = cfg.createOreFeature(preset);
                    Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, feature.getFeature());
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
            for (final DecoratedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (cfg.isGlobal()) {
                    final GlobalFeatureProvider<?> provider = (GlobalFeatureProvider<?>) cfg.getConfig();
                    globalConfigs.add(provider.getFeatureType(), new FeatureStem(cfg, new StoneBlockPlacer(preset)));
                }
            }
        }
        globalConfigs.forEach((feature, stems) ->
            features.put(randId("global_stone_"), feature.configured(stems)));
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
            for (final DecoratedFeatureSettings<?, ?> cfg : preset.getFeatures()) {
                if (cfg.isGlobal()) {
                    final GlobalFeatureProvider<?> provider = (GlobalFeatureProvider<?>) cfg.getConfig();
                    globalConfigs.add(provider.getFeatureType(), new FeatureStem(cfg, new VariantBlockPlacer(cfg, preset)));
                }
            }
        }
        globalConfigs.forEach((feature, stems) ->
            features.put(randId("global_ore_"), feature.configured(stems)));
    }

    private static ResourceLocation randId(final String prefix) {
        return new ResourceLocation(Reference.MOD_ID, prefix + LibStringUtils.randId(8));
    }
}
