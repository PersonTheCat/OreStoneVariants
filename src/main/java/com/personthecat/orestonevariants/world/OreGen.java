package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.WorldGenProperties;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class OreGen {
    /** A cleaner reference to Decoration#UNDERGROUND_ORES. */
    private static final Decoration ORE_DEC = Decoration.UNDERGROUND_ORES;
    /** A cleaner reference to Placement#COUNT_RANGE. */
    private static final Placement COUNT_RNG = Placement.COUNT_RANGE;

    /** Generates and registers all ore decorators with the appropriate biomes. */
    public static void registerGenerators() {
        forEnabledProps((block, props, gen) -> {
            CountRangeConfig rangeConfig = new CountRangeConfig(gen.frequency, gen.height.min, 0, gen.height.max);
            VariantFeatureConfig variantConfig = new VariantFeatureConfig(props, gen.count, gen.denseRatio);
            gen.biomes.get().forEach(b -> b.addFeature(ORE_DEC, createFeature(variantConfig, rangeConfig)));
        });
    }

    /** Iterates through all WorldGenProperties and their respective BlockStates. */
    private static void forEnabledProps(TriConsumer<BlockState, OreProperties, WorldGenProperties> fun) {
        for (BaseOreVariant block : Main.BLOCKS) {
            for (WorldGenProperties gen : block.properties.gen) {
                fun.accept(block.getDefaultState(), block.properties, gen);
            }
        }
    }

    /** Shorthand for converting the input configs into a ConfiguredFeature. */
    private static ConfiguredFeature createFeature(VariantFeatureConfig variantConfig, CountRangeConfig rangeConfig) {
        return Biome.createDecoratedFeature(new VariantFeature(variantConfig), variantConfig, COUNT_RNG, rangeConfig);
    }

//            OreFeatureConfig featureConfig = new OreFeatureConfig(FillerBlockType.NATURAL_STONE, block, gen.frequency);
//            ConfiguredFeature f = Biome.createDecoratedFeature(Feature.ORE, featureConfig, COUNT_RNG, rangeConfig);
}