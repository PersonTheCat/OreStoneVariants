package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.properties.WorldGenProperties;
import com.personthecat.orestonevariants.util.CommonMethods;
import net.minecraft.block.*;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.Placement;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.full;

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
     */

    /** A cleaner reference to VariantFeature#INSTANCE. */
    private static final Feature<VariantFeatureConfig> VARIANT_FEATURE = VariantFeature.INSTANCE;
    /** A cleaner reference to VariantPlacement#INSTANCE. */
    private static final Placement<VariantPlacementConfig> VARIANT_PLACEMENT = VariantPlacement.INSTANCE;

    /** Handles all ore generation features for this mod. */
    public static void setupOreFeatures() {

        // These features are currently not working due to biome features now being immutable.
        // Still need to do some more research.

//        if (!(Cfg.enableVanillaOres.get() && Cfg.enableVanillaStone.get())) {
//            disableGenerators();
//        }
//        if (Cfg.enableOSVStone.get()) {
//            registerStoneGenerators();
//        }
//        if (Cfg.enableOSVOres.get()) {
//            registerVariantGenerators();
//        }
    }

    /** Disables all applicable underground ore decorators. */
    private static void disableGenerators() {
        for (Biome b : WorldGenRegistries.field_243657_i) {
            // Todo: verify 242496_b
            final List<ConfiguredFeature<?, ?>> ores = b.func_242440_e().func_242496_b();//b.getFeatures(ORE_DEC);
            final List<ConfiguredFeature<?, ?>> drain = new ArrayList<>();
            ores.forEach(ore ->
                findOreConfig(ore).ifPresent(config -> {
                    if (shouldDisable(config.state)) {
                        CommonMethods.info("Discovered the following ore: {}", config.state);
                        drain.add(ore);
                    }
                })
            );
            ores.removeAll(drain);
        }
    }

    /** Attempts to load a standard OreFeatureConfig from the input feature. */
    private static Optional<OreFeatureConfig> findOreConfig(ConfiguredFeature<?, ?> feature) {
        if (feature.config instanceof DecoratedFeatureConfig) {
            final DecoratedFeatureConfig decorated = (DecoratedFeatureConfig) feature.config;
            final IFeatureConfig config = decorated.feature.get().config;
            if (config instanceof OreFeatureConfig) {
                return full((OreFeatureConfig) config);
            }
        }
        return empty();
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
        forEnabledProps((block, props, gen) -> {
            VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height.min, gen.height.max, gen.chance);
            VariantFeatureConfig featureConfig = new VariantFeatureConfig(props, gen.size, gen.denseRatio);
            gen.biomes.get().forEach(b -> b.func_242440_e().func_242496_b().add(createFeature(featureConfig, placementConfig)));
        });
        //  gen.biomes.get().forEach(b -> b.addFeature(ORE_DEC, createFeature(variantConfig, rangeConfig)));
    }

    /** Iterates through all WorldGenProperties and their respective BlockStates. */
    private static void forEnabledProps(TriConsumer<BlockState, OreProperties, WorldGenProperties> fun) {
        for (BaseOreVariant block : Main.BLOCKS) {
            for (WorldGenProperties gen : block.properties.gen) {
                fun.accept(block.getDefaultState(), block.properties, gen);
            }
        }
    }

    /** Generates and registers all stone decorators with the appropriate biomes. */
    private static void registerStoneGenerators() {
        forEnabledProps((block, gen) -> {
            VariantPlacementConfig placementConfig = new VariantPlacementConfig(gen.count, gen.height.min, gen.height.max, gen.chance);
            OreFeatureConfig stoneConfig = new OreFeatureConfig(FillerBlockType.field_241882_a, block, gen.size);
            gen.biomes.get().forEach(b -> b.func_242440_e().func_242496_b().add(createFeature(stoneConfig, placementConfig)));
        });
    }

    /** Iterates through all StoneProperties and their respective blocks and settings. */
    private static void forEnabledProps(BiConsumer<BlockState, WorldGenProperties> fun) {
        for (StoneProperties props : Main.STONE_PROPERTIES) {
            for (WorldGenProperties gen : props.gen) {
                fun.accept(props.stone, gen);
            }
        }
    }

    /** Shorthand for converting the input configs into a ConfiguredFeature. */
    private static ConfiguredFeature createFeature(VariantFeatureConfig featureConfig, VariantPlacementConfig placementConfig) {
        return VARIANT_FEATURE.withConfiguration(featureConfig)
            .withPlacement(VARIANT_PLACEMENT.configure(placementConfig));
    }

    /** Shorthand for converting the input configs into a ConfiguredFeature. */
    private static ConfiguredFeature createFeature(OreFeatureConfig stoneConfig, VariantPlacementConfig placementConfig) {
        return Feature.ORE.withConfiguration(stoneConfig)
            .withPlacement(VARIANT_PLACEMENT.configure(placementConfig));
    }
}