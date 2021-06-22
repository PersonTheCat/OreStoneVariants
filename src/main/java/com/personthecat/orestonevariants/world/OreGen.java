package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.properties.NestedProperties;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.properties.WorldGenProperties;
import com.personthecat.orestonevariants.util.SafeRegistry;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.nullable;
import static com.personthecat.orestonevariants.util.CommonMethods.randomId;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static net.minecraft.world.gen.GenerationStage.Decoration.UNDERGROUND_DECORATION;
import static net.minecraft.world.gen.GenerationStage.Decoration.UNDERGROUND_ORES;

@Log4j2
public class OreGen {

    /** A cleaner reference to VariantFeature#INSTANCE. */
    private static final Feature<VariantFeatureConfig> VARIANT_FEATURE = VariantFeature.INSTANCE;

    /** A cleaner reference to VariantPlacement#INSTANCE. */
    private static final Placement<VariantPlacementConfig> VARIANT_PLACEMENT = VariantPlacement.INSTANCE;

    /** All of the enabled ores that we have variants of. */
    private static final SafeRegistry<Integer, Block> ENABLED_ORES = SafeRegistry.enumerated(OreGen::getEnabledOres);

    /** All of the enabled stone types that we are spawning. */
    private static final SafeRegistry<Integer, Block> ENABLED_STONE = SafeRegistry.enumerated(OreGen::getEnabledStone);

    /** All of the nested property types to be used for each generator. */
    private static final SafeRegistry<WorldGenProperties, List<NestedType>> NESTED_TYPES = SafeRegistry.of(OreGen::getNestedTypes);

    /** Handles all ore generation features for this mod in the current biome. */
    public static void setupOreFeatures(final BiomeLoadingEvent event) {
        final BiomeGenerationSettingsBuilder generation = event.getGeneration();

        if (!(Cfg.enableVanillaOres.get() && Cfg.enableVanillaStone.get())) {
            final List<Supplier<ConfiguredFeature<?, ?>>> ores =
                generation.getFeatures(UNDERGROUND_ORES);
            // Most Nether ores are added at this stage.
            final List<Supplier<ConfiguredFeature<?, ?>>> decorations =
                generation.getFeatures(UNDERGROUND_DECORATION);

            disableGenerators(ores, event.getName());
            disableGenerators(decorations, event.getName());
        }
        if (Cfg.enableOSVStone.get()) {
            registerStoneGenerators(generation, event.getName());
        }
        if (Cfg.enableOSVOres.get()) {
            registerVariantGenerators(generation, event.getName());
        }
    }

    /** Allows world generation settings to be dynamically reloaded when the world is restarted. */
    public static void onWorldClosed() {
        ENABLED_ORES.reset();
        ENABLED_STONE.reset();
        NESTED_TYPES.reset();
    }

    /** Generates a set containing all of the ores that we have variants of. */
    private static Set<Block> getEnabledOres() {
        return LazyRegistries.ORE_PROPERTIES.stream()
            .map(properties -> properties.ore.get().getBlock())
            .collect(Collectors.toSet());
    }

    /** Generates a set containing all of the stone types that we are spawning. */
    private static Set<Block> getEnabledStone() {
        return LazyRegistries.STONE_PROPERTIES.stream()
            .map(properties -> properties.stone.getBlock())
            .collect(Collectors.toSet());
    }

    /** Generates a map of all final nested property types for each generator. */
    private static Map<WorldGenProperties, List<NestedType>> getNestedTypes() {
        final Map<WorldGenProperties, List<NestedType>> containers = new HashMap<>();
        for (OreProperties props : LazyRegistries.ORE_PROPERTIES) {
            for (WorldGenProperties gen : props.gen) {
                containers.put(gen, getNested(gen));
            }
        }
        return containers;
    }

    /** Compiles all of the container properties for the given generator. */
    private static List<NestedType> getNested(WorldGenProperties gen) {
        if (gen.containers.isEmpty()) {
            return Collections.emptyList();
        }
        final List<NestedType> nested = new ArrayList<>();
        for (NestedProperties container : gen.containers) {
            final OreProperties props = LazyRegistries.ORE_PROPERTIES.get(container.type);
            if (props != null) {
                nested.add(new NestedType(props, container.chance));
            } else if (container.required) {
                throw runExF("The variant type \"{}\" is required by another preset.", container.type);
            }
        }
        return nested;
    }

    /** Disables all applicable underground ore decorators. */
    private static void disableGenerators(List<Supplier<ConfiguredFeature<?, ?>>> features, ResourceLocation name) {
        final List<Supplier<ConfiguredFeature<?, ?>>> drain = new ArrayList<>();
        features.forEach(feature ->
            findOreConfig(feature.get()).ifPresent(ore -> {
                if (shouldDisable(ore)) {
                    log.debug("Removing {} from generation in {}.", ore, name);
                    drain.add(feature);
                }
            })
        );
        features.removeAll(drain);
    }

    /** Attempts to load a standard OreFeatureConfig from the input feature. */
    private static Optional<BlockState> findOreConfig(ConfiguredFeature<?, ?> feature) {
        final Iterator<ConfiguredFeature<?, ?>> features = feature.config.func_241856_an_().iterator();
        // Todo: introduce a foreign config helper to support modded types.
        while (features.hasNext()) {
            final IFeatureConfig config = features.next().config;
            if (config instanceof OreFeatureConfig) {
                return full(((OreFeatureConfig) config).state);
            } else if (config instanceof ReplaceBlockConfig) {
                return full(((ReplaceBlockConfig) config).state);
            }
        }
        return empty();
    }

    /** Determines whether the input block should be drained, per the current biome config. */
    private static boolean shouldDisable(BlockState state) {
        return (!Cfg.enableVanillaOres.get() && isOre(state.getBlock()))
            || (!Cfg.enableVanillaStone.get() && isStoneGen(state.getBlock()));
    }

    /** Determines if this block is one of the ores that we have enabled and thus should remove. */
    private static boolean isOre(Block block) {
        return ENABLED_ORES.containsValue(block);
    }

    /** Determines if this is a stone block that we are spawning and thus should remove. */
    private static boolean isStoneGen(Block block) {
        return ENABLED_STONE.containsValue(block);
    }

    /** Generates and registers all ore decorators with the appropriate biomes. */
    private static void registerVariantGenerators(BiomeGenerationSettingsBuilder generation, ResourceLocation name) {
        forEnabledProps((props, gen) -> {
            final List<NestedType> nested = NESTED_TYPES.get(gen);
            final VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height, gen.chance);
            final VariantFeatureConfig featureConfig = new VariantFeatureConfig(props, gen.size, gen.denseRatio, nested);
            final ConfiguredFeature<?, ?> configured = createFeature(featureConfig, placementConfig);
            if (gen.biomes.get().check(Biome::getRegistryName, name)) {
                nullable(generation.getFeatures(gen.stage)).ifPresent(f -> f.add(() -> configured));
            }
        });
    }

    /** Iterates through all WorldGenProperties and their respective BlockStates. */
    private static void forEnabledProps(BiConsumer<OreProperties, WorldGenProperties> fun) {
        for (OreProperties props : LazyRegistries.ORE_PROPERTIES) {
            for (WorldGenProperties gen : props.gen) {
                fun.accept(props, gen);
            }
        }
    }

    /** Generates and registers all stone decorators with the appropriate biomes. */
    private static void registerStoneGenerators(BiomeGenerationSettingsBuilder generation, ResourceLocation name) {
        forEnabledStone((block, rule, gen) -> {
            VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height, gen.chance);
            OreFeatureConfig stoneConfig = new OreFeatureConfig(rule, block, gen.size);
            final ConfiguredFeature<?, ?> configured = createFeature(stoneConfig, placementConfig);
            if (gen.biomes.get().check(Biome::getRegistryName, name)) {
                final GenerationStage.Decoration stage = checkDefer(gen.stage);
                nullable(generation.getFeatures(stage)).ifPresent(f -> f.add(() -> configured));
            }
        });
    }

    /** Iterates through all StoneProperties and their respective blocks and settings. */
    private static void forEnabledStone(TriConsumer<BlockState, RuleTest, WorldGenProperties> fun) {
        for (StoneProperties props : LazyRegistries.STONE_PROPERTIES) {
            for (WorldGenProperties gen : props.gen) {
                fun.accept(props.stone, props.source, gen);
            }
        }
    }

    /** Generates and registers a new ConfiguredFeature for ore variants. */
    private static ConfiguredFeature<?, ?> createFeature(VariantFeatureConfig featureConfig, VariantPlacementConfig placementConfig) {
        final ConfiguredFeature<?, ?> feature = VARIANT_FEATURE.withConfiguration(featureConfig)
            .withPlacement(VARIANT_PLACEMENT.configure(placementConfig));
        return registerRandomly(feature);
    }

    /** Generates and registers a new ConfiguredFeature for stone types. */
    private static ConfiguredFeature<?, ?> createFeature(OreFeatureConfig stoneConfig, VariantPlacementConfig placementConfig) {
        final ConfiguredFeature<?, ?> feature = Feature.ORE.withConfiguration(stoneConfig)
            .withPlacement(VARIANT_PLACEMENT.configure(placementConfig));
        return registerRandomly(feature);
    }

    /** A temporary solution for generating registries until I can figure out why mine aren't working. */
    private static ConfiguredFeature<?, ?> registerRandomly(ConfiguredFeature<?, ?> feature) {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, randomId(), feature);
    }

    /** Determines the actual generation stage for this ore based on config settings. */
    private static GenerationStage.Decoration checkDefer(GenerationStage.Decoration stage) {
        if (GenerationStage.Decoration.TOP_LAYER_MODIFICATION.equals(stage)) {
            return stage; // Cannot be deferred.
        }
        if (Cfg.deferOreGeneration()) {
            return GenerationStage.Decoration.values()[stage.ordinal() + 1];
        }
        return stage;
    }
}