package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.properties.WorldGenProperties;
import net.minecraft.block.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import org.apache.logging.log4j.util.BiConsumer;

import java.util.*;
import java.util.function.Supplier;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static net.minecraft.world.gen.GenerationStage.Decoration.UNDERGROUND_ORES;
import static net.minecraft.world.gen.GenerationStage.Decoration.UNDERGROUND_DECORATION;

// Todo: WIP
public class OreGen {

    /** A cleaner reference to VariantFeature#INSTANCE. */
    private static final Feature<VariantFeatureConfig> VARIANT_FEATURE = VariantFeature.INSTANCE;

    /** A cleaner reference to VariantPlacement#INSTANCE. */
    private static final Placement<VariantPlacementConfig> VARIANT_PLACEMENT = VariantPlacement.INSTANCE;

    /** Handles all ore generation features for this mod in the current biome. */
    public static void setupOreFeatures(final BiomeLoadingEvent event) {
        final BiomeGenerationSettingsBuilder generation = event.getGeneration();

        // Todo: Check all stages?
        final List<Supplier<ConfiguredFeature<?, ?>>> ores =
            generation.getFeatures(UNDERGROUND_ORES);
        // Most Nether ores are added at this stage.
        final List<Supplier<ConfiguredFeature<?, ?>>> decorations =
            generation.getFeatures(UNDERGROUND_DECORATION);

        // Todo: this check is no longer exhaustive.
        if (ores == null && decorations == null) {
            error("Unexpected null feature set @ {}. Skipping...", event.getName());
            return;
        }
        if (!(Cfg.enableVanillaOres.get() && Cfg.enableVanillaStone.get())) {
            if (ores != null) {
                disableGenerators(ores, event.getName());
            }
            if (decorations != null) {
                disableGenerators(decorations, event.getName());
            }
        }
        if (Cfg.enableOSVStone.get()) {
            registerStoneGenerators(generation, event.getName());
        }
        if (Cfg.enableOSVOres.get()) {
            registerVariantGenerators(generation, event.getName());
        }
    }

    /** Disables all applicable underground ore decorators. */
    private static void disableGenerators(List<Supplier<ConfiguredFeature<?, ?>>> ores, ResourceLocation name) {
        final List<Supplier<ConfiguredFeature<?, ?>>> drain = new ArrayList<>();
        ores.forEach(ore ->
            findOreConfig(ore.get()).ifPresent(config -> {
                if (shouldDisable(config.state)) {
                    info("Removing {} from generation in {}.", config.state, name);
                    drain.add(ore);
                }
            })
        );
        ores.removeAll(drain);
    }

    /** Attempts to load a standard OreFeatureConfig from the input feature. */
    private static Optional<OreFeatureConfig> findOreConfig(ConfiguredFeature<?, ?> feature) {
        return feature.config.func_241856_an_()
            .filter(nested -> nested.config instanceof OreFeatureConfig)
            .findFirst()
            .map(nested -> (OreFeatureConfig) nested.config);
    }

    /** Determines whether the input block should be drained, per the current biome config. */
    private static boolean shouldDisable(BlockState state) {
        return (!Cfg.enableVanillaOres.get() && isOre(state.getBlock()))
            || (!Cfg.enableVanillaStone.get() && isStoneGen(state.getBlock()));
    }

    private static boolean isOre(Block block) {
        return block instanceof OreBlock || block instanceof RedstoneOreBlock;
    }

    // Todo: remove from a list including Nether ores.
    private static boolean isStoneGen(Block block) {
        return block == Blocks.STONE
            || block == Blocks.ANDESITE
            || block == Blocks.DIORITE
            || block == Blocks.GRANITE
            || block == Blocks.DIRT
            // Todo: gravel spawns differently in the nether and is now gone.
            || block == Blocks.GRAVEL;
    }

    /** Generates and registers all ore decorators with the appropriate biomes. */
    private static void registerVariantGenerators(BiomeGenerationSettingsBuilder generation, ResourceLocation name) {
        forEnabledProps((props, gen) -> {
            VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height, gen.chance);
            VariantFeatureConfig featureConfig = new VariantFeatureConfig(props, gen.size, gen.denseRatio);
            final ConfiguredFeature<?, ?> configured = createFeature(featureConfig, placementConfig);
            if (gen.biomes.get().check(Biome::getRegistryName, name)) {
                // Todo: move null check to this point.
                generation.getFeatures(gen.stage).add(() -> configured);
            }
        });
    }

    /** Iterates through all WorldGenProperties and their respective BlockStates. */
    private static void forEnabledProps(BiConsumer<OreProperties, WorldGenProperties> fun) {
        for (OreProperties props : Main.ORE_PROPERTIES) {
            for (WorldGenProperties gen : props.gen) {
                fun.accept(props, gen);
            }
        }
    }

    /** Generates and registers all stone decorators with the appropriate biomes. */
    private static void registerStoneGenerators(BiomeGenerationSettingsBuilder generation, ResourceLocation name) {
        forEnabledStone((block, gen) -> {
            VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height, gen.chance);
            OreFeatureConfig stoneConfig = new OreFeatureConfig(FillerBlockType.BASE_STONE_OVERWORLD, block, gen.size);
            final ConfiguredFeature<?, ?> configured = createFeature(stoneConfig, placementConfig);
            if (gen.biomes.get().check(Biome::getRegistryName, name)) {
                // Todo: move null check to this point.
                generation.getFeatures(gen.stage).add(() -> configured);
            }
        }
    );
}

    /** Iterates through all StoneProperties and their respective blocks and settings. */
    private static void forEnabledStone(BiConsumer<BlockState, WorldGenProperties> fun) {
        for (StoneProperties props : Main.STONE_PROPERTIES) {
            for (WorldGenProperties gen : props.gen) {
                fun.accept(props.stone, gen);
            }
        }
    }

    /** Generates and registers a new ConfiguredFeature for ore variants. */
    private static ConfiguredFeature createFeature(VariantFeatureConfig featureConfig, VariantPlacementConfig placementConfig) {
        final ConfiguredFeature<?, ?> feature = VARIANT_FEATURE.withConfiguration(featureConfig)
            .withPlacement(VARIANT_PLACEMENT.configure(placementConfig));
        return registerRandomly(feature);
    }

    /** Generates and registers a new ConfiguredFeature for stone types. */
    private static ConfiguredFeature createFeature(OreFeatureConfig stoneConfig, VariantPlacementConfig placementConfig) {
        final ConfiguredFeature<?, ?> feature = Feature.ORE.withConfiguration(stoneConfig)
            .withPlacement(VARIANT_PLACEMENT.configure(placementConfig));
        return registerRandomly(feature);
    }

    /** A temporary solution for generating registries until I can figure out why mine aren't working. */
    private static ConfiguredFeature<?, ?> registerRandomly(ConfiguredFeature<?, ?> feature) {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, randID(), feature);
    }

    private static String randID() {
        final StringBuilder sb = new StringBuilder("osv:");
        final Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            sb.append((char) rand.nextInt(26) + 'a');
        }
        return sb.toString();
    }
}