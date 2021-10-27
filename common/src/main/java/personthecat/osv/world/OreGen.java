package personthecat.osv.world;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.catlib.data.SafeRegistry;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.event.registry.DynamicRegistries;
import personthecat.catlib.event.world.FeatureModificationContext;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.resolver.FeatureSettingsResolver;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class OreGen {

    private static final SafeRegistry<ResourceLocation, ConfiguredFeature<?, ?>> DISABLED_FEATURES =
        SafeRegistry.of(OreGen::loadDisabledFeatures)
            .canBeReset(true);

    private static final SafeRegistry<ResourceLocation, Block> DISABLED_BLOCKS =
        SafeRegistry.of(OreGen::loadDisabledBlocks)
            .canBeReset(true);

    public static void setupOreFeatures(final FeatureModificationContext ctx) {
        log.debug("Injecting changes to biome: {}", ctx.getName());
        DISABLED_FEATURES.forEach((id, feature) -> ctx.removeFeature(id));
    }

    public static void onWorldClosed() {
        SafeRegistry.resetAll(DISABLED_FEATURES, DISABLED_BLOCKS);
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
        for (final Block ore : DISABLED_BLOCKS) {
            if (FeatureSettingsResolver.featureContainsBlock(feature, ore.defaultBlockState())) {
                return true;
            }
        }
        return false;
    }

    private static void addForciblyDisabledFeatures(final Map<ResourceLocation, ConfiguredFeature<?, ?>> features) {
        for (final String key : Cfg.disabledFeatures()) {
            final ResourceLocation id = new ResourceLocation(key);
            final ConfiguredFeature<?, ?> disabled = DynamicRegistries.CONFIGURED_FEATURES.lookup(id);
            if (disabled != null) {
                features.put(id, disabled);
                log.debug("Adding {} to disabled features. It will be disabled globally.", id);
            } else {
                log.error("Cannot disable {}: no such feature. Ignoring...", id);
            }
        }
    }

    private static Map<ResourceLocation, Block> loadDisabledBlocks() {
        final Map<ResourceLocation, Block> ores = new HashMap<>();
        if (!Cfg.enableVanillaOres()) {
            for (final OreVariant variant : ModRegistries.VARIANTS) {
                final Block ore = variant.getFg();
                ores.put(CommonRegistries.BLOCKS.getKey(ore), ore);
            }
        }
        return ores;
    }
}
