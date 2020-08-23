package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.properties.WorldGenProperties;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import net.minecraft.block.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.Placement;
import org.apache.logging.log4j.util.BiConsumer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class OreGen {

    /*
     * Temporary deobfuscation notes:
     * * NoPlacementConfig#field_236556_b_ -> INSTANCE
     * * IDecoratable#func_242729_a(int) -> setChance
     *   * higher value -> lower chance; (inverseChance)
     * * IDecoratable#func_242733_d(int) -> setMaxHeight
     * * IDecoratable#func_242728_a -> Spawn using SquarePlacement
     *   * SquarePlacement means 8 x (BlockPos) -> new BlockPos(randX, sameY, randZ)
     * * FeatureSpread::new(int, int) -> base, spread
     * * IDecoratable#func_242731_b(int) -> setBase; spread = 0
     * * IDecoratable#func_242732_c(int, int) setBaseAndSpread
     * * IDecoratable#func_242730_a(FeatureSpread) -> count placement with spread
     *   * This must be responsible for setting the count.
     * * IDecoratable#func_242732_c(int) -> count placement with spread (base = 0, spread = val)
     * * TopSolidRangeConfig(int, int, int) -> bottom_offset, top_offset, maximum
     * * WorldDecoratingHelper#func_242891_a -> getWorldHeight via ChunkGenerator
     * * Biome# -> generation settings
     * * BiomeGenerationSettings.field_242484_f -> a list of *lists* of features, ordered by stage.
     *
     * Todo: Investigate mutable forge registries. Where is the documentation?
     * Todo: Figure out why manual registry names aren't working.
     *  * They are confirmed to not be redundant.
     */

    /** A cleaner reference to VariantFeature#INSTANCE. */
    private static final Feature<VariantFeatureConfig> VARIANT_FEATURE = VariantFeature.INSTANCE;
    /** A cleaner reference to VariantPlacement#INSTANCE. */
    private static final Placement<VariantPlacementConfig> VARIANT_PLACEMENT = VariantPlacement.INSTANCE;
    /** The index pointing to the list of underground ores in all generation features. */
    private static final int UNDERGROUND_ORES = GenerationStage.Decoration.UNDERGROUND_ORES.ordinal();

    /** Handles all ore generation features for this mod. */
    public static void setupOreFeatures() {
        enableBiomeMods();

        if (!(Cfg.enableVanillaOres.get() && Cfg.enableVanillaStone.get())) {
            disableGenerators();
        }
        if (Cfg.enableOSVStone.get()) {
            registerStoneGenerators();
        }
        if (Cfg.enableOSVOres.get()) {
            registerVariantGenerators();
        }
    }

    /** It seems that modded ores can only be spawned programatically via reflection... */
    private static void enableBiomeMods() {
        final Field features = ReflectionTools.getField(BiomeGenerationSettings.class, "field_242484_f", 7);
        ReflectionTools.setMutable(features);
        synchronized (WorldGenRegistries.field_243657_i) {
            updateAllFeatures(features);
        }
        info("Successfully enabled biome feature mods.");
    }

    /** Replace all feature arrays with mutable copies. */
    private static void updateAllFeatures(Field features) {
        for (Biome b : WorldGenRegistries.field_243657_i) {
            final BiomeGenerationSettings settings = b.func_242440_e();
            final List<List<Supplier<ConfiguredFeature<?, ?>>>> current = ReflectionTools.getValue(features, settings);
            final List<List<Supplier<ConfiguredFeature<?, ?>>>> values = Collections.synchronizedList(new LinkedList<>());
            current.forEach(list -> values.add(new LinkedList<>(list)));
            ReflectionTools.setValue(features, settings, values);
        }
    }

    /** Disables all applicable underground ore decorators. */
    private static void disableGenerators() {
        for (Biome b : WorldGenRegistries.field_243657_i) {
            final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = b.func_242440_e().func_242498_c();
            if (features.size() < UNDERGROUND_ORES) {
                continue;
            }
            final List<Supplier<ConfiguredFeature<?, ?>>> ores = features.get(UNDERGROUND_ORES);
            final List<Supplier<ConfiguredFeature<?, ?>>> drain = new ArrayList<>();
            ores.forEach(ore ->
                findOreConfig(ore.get()).ifPresent(config -> {
                    if (shouldDisable(config.state)) {
                        info("Removing {} from generation in {}.", config.state, b);
                        drain.add(ore);
                    }
                })
            );
            ores.removeAll(drain);
        }
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

    private static boolean isStoneGen(Block block) {
        return block == Blocks.STONE
            || block == Blocks.ANDESITE
            || block == Blocks.DIORITE
            || block == Blocks.GRANITE
            || block == Blocks.DIRT
            || block == Blocks.GRAVEL;
    }

    /** Generates and registers all ore decorators with the appropriate biomes. */
    private static void registerVariantGenerators() {
        forEnabledProps((props, gen) -> {
            VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height.min, gen.height.max, gen.chance);
            VariantFeatureConfig featureConfig = new VariantFeatureConfig(props, gen.size, gen.denseRatio);
            final ConfiguredFeature<?, ?> configured = createFeature(featureConfig, placementConfig);
            gen.biomes.get().forEach(b -> getOreFeatures(b).add(() -> configured));
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
    private static void registerStoneGenerators() {
        forEnabledStone((block, gen) -> {
            VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height.min, gen.height.max, gen.chance);
            OreFeatureConfig stoneConfig = new OreFeatureConfig(FillerBlockType.field_241882_a, block, gen.size);
            final ConfiguredFeature<?, ?> configured = createFeature(stoneConfig, placementConfig);
            gen.biomes.get().forEach(b -> getOreFeatures(b).add(() -> configured));
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

    /** Retrieves the current set of features in the ore phase. */
    private static List<Supplier<ConfiguredFeature<?, ?>>> getOreFeatures(Biome b) {
        final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = b.func_242440_e().func_242498_c();
        while (features.size() <= UNDERGROUND_ORES) {
            features.add(new LinkedList<>());
        }
        return features.get(UNDERGROUND_ORES);
    }

    /** A temporary solution for generating registries until I can figure out why mine aren't working. */
    private static ConfiguredFeature<?, ?> registerRandomly(ConfiguredFeature<?, ?> feature) {
        return Registry.register(WorldGenRegistries.field_243653_e, randID(), feature);
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