package com.personthecat.orestonevariants.world;

import com.google.common.collect.ImmutableMap;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.properties.WorldGenProperties;
import com.personthecat.orestonevariants.util.DualMap;
import com.personthecat.orestonevariants.util.Range;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;
import java.util.function.BiConsumer;

public class OreGen implements IWorldGenerator {

    // This is a very likely memory hog. Consider replacing it.
    /** A map of every generator and its configuration relative to each dimension and biome. */
    private final DualMap<Integer, Biome, List<GeneratorData>> worldGenData = generateData();
    /** A set of chunk selectors mapped to each world seed. */
    private final Long2ObjectMap<RandomChunkSelector> selectors = new Long2ObjectOpenHashMap<>();

    private DualMap<Integer, Biome, List<GeneratorData>> generateData() {
        final List<GeneratorData> list = new ArrayList<>();
        if (Cfg.WorldCat.enableOSVOres) {
            getOreGen(list);
        }
        if (Cfg.WorldCat.enableOSVStone) {
            getStoneGen(list);
        }
        return buildFeatureMap(list);
    }

    /** Adds all of the custom ore generators to the list. */
    private static void getOreGen(List<GeneratorData> list) {
        for (OreProperties ore : Main.ORE_PROPERTIES) {
            for (WorldGenProperties cfg : ore.gen) {
                final Map<IBlockState, IBlockState> genMap = getGenMap(ore);
                final VariantFeature feature = new VariantFeature(genMap, cfg.size);
                list.add(new GeneratorData(cfg, feature));
            }
        }
    }

    /** Generates and adds a set of standard stone generators to the list. */
    private static void getStoneGen(List<GeneratorData> list) {
        for (StoneProperties stone : Main.STONE_PROPERTIES) {
            for (WorldGenProperties cfg : stone.gen) {
                final WorldGenMinable feature = new WorldGenMinable(stone.stone.get(), cfg.size);
                list.add(new GeneratorData(cfg, feature));
            }
        }
    }

    /** Maps every background block to its ore candidates for the input properties. */
    private static Map<IBlockState, IBlockState> getGenMap(OreProperties ore) {
        final ImmutableMap.Builder<IBlockState, IBlockState> map = ImmutableMap.builder();
        for (BaseOreVariant block : Main.BLOCKS) {
            if (block.properties.equals(ore)) {
                map.put(block.bgBlock, block.getDefaultState());
            }
        }
        return map.build();
    }

    /** Maps every biome and dimension to its generator candidates. */
    private static DualMap<Integer, Biome, List<GeneratorData>> buildFeatureMap(List<GeneratorData> master) {
        final DualMap.Builder<Integer, Biome, List<GeneratorData>> map = new DualMap.Builder<>();
        for (GeneratorData data : master) {
            forAllConditions((dim, b) -> {
                if (data.cfg.canSpawn(dim, b)) {
                    add(map, dim, b, data);
                }
            });
        }
        return map.build();
    }

    private static void forAllConditions(BiConsumer<Integer, Biome> fun) {
        for (IntSortedSet dimSet : DimensionManager.getRegisteredDimensions().values()) {
            for (int dim : dimSet) {
                for (Biome b : ForgeRegistries.BIOMES) {
                    fun.accept(dim, b);
                }
            }
        }
    }

    @Override
    public void generate(Random rand, int x, int z, World world, IChunkGenerator chunkGen, IChunkProvider provider) {
        // Convert to block coords.
        final int blockX = x << 4;
        final int blockZ = z << 4;
        // Get location info.
        final BlockPos pos = new BlockPos(blockX, 0, blockZ);
        final int dim = world.provider.getDimension();
        final Biome b = world.getBiomeForCoordsBody(pos);

        for (GeneratorData gen : getData(dim, b)) {
            final WorldGenerator feature = gen.feature;
            final WorldGenProperties cfg = gen.cfg;
            final double chance = getChance(world, feature, cfg.ID.get(), x, z, cfg.chance);
            run(feature, world, rand, chance, cfg.count, blockX, blockZ, cfg.height);
        }
    }

    private List<GeneratorData> getData(int dim, Biome b) {
        final List<GeneratorData> data = worldGenData.get(dim, b);
        return data == null ? Collections.emptyList() : data;
    }

    /** Gets a probability specific to the current chunk, if applicable. */
    private double getChance(World world, WorldGenerator feature, int ID, int x, int z, double baseline) {
        if (world != null && feature instanceof VariantFeature && Cfg.WorldCat.largeClusters) {
            return getSelector(world).getProbability(ID, x, z);
        }
        return baseline;
    }

    /** Put if absent and get the current selector. */
    private RandomChunkSelector getSelector(World world) {
        final long seed = world.getSeed();
        final RandomChunkSelector fromMap = selectors.get(seed);
        if (fromMap != null) {
            return fromMap;
        }
        final RandomChunkSelector selector = new RandomChunkSelector(seed);
        selectors.put(seed, selector);
        return selector;
    }

    /** Runs the input generator at random coordinates in the current chunk. */
    private static void run(WorldGenerator gen, World world, Random rand, double chance, int count, int x, int z, Range height) {
        for (int i = 0; i < count; i++) {
            if (chance == 1.0 || rand.nextDouble() <= chance) {
                final int y = height.rand(rand);
                final int newX = x + rand.nextInt(16);
                final int newZ = z + rand.nextInt(16);
                gen.generate(world, rand, new BlockPos(newX, y, newZ));
            }
        }
    }

    private static void add(DualMap.Builder<Integer, Biome, List<GeneratorData>> map, int dim, Biome b, GeneratorData data) {
        if (!map.has(dim, b)) {
            map.put(dim, b, new ArrayList<>());
        }
        map.get(dim, b).add(data);
    }

    private static class GeneratorData {
        private final WorldGenProperties cfg;
        private final WorldGenerator feature;

        private GeneratorData(WorldGenProperties cfg, WorldGenerator feature) {
            this.cfg = cfg;
            this.feature = feature;
        }
    }
}