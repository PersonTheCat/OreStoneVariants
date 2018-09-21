package personthecat.mod.world.gen;

import static personthecat.mod.Main.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import personthecat.mod.config.Cfg;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.CommonMethods;

/**
 * @author PersonTheCat
 * @edits pupnewfster
 */
@EventBusSubscriber
public class WorldGenCustomOres implements IWorldGenerator
{
	private static boolean DO_VANILLA_STONE_GEN;
	private static int ANDESITE_MIN, DIORITE_MIN, GRANITE_MIN, ANDESITE_INCR = 81, DIORITE_INCR = 81, GRANITE_INCR = 81, STONE_COUNT = 10;
	
	private static WorldGenerator dirt, gravel, andesite, diorite, granite;

	private static RandomChunkSelector chunkSelector;
	
	private static final Map<WorldGenProperties, WorldGenerator> ORE_WORLDGEN_MAP = new HashMap<>();
	
	public WorldGenCustomOres()
	{
		mapStoneGenerators();
		mapNormalGenerators();
	}

	private static void mapNormalGenerators()
	{
		for (WorldGenProperties genProp : WorldGenProperties.getWorldGenPropertyRegistry())
		{			
			if (!genProp.isValidProperty()) continue;
			
			handleMapping(genProp.getName(), genProp);
		}
	}
	
	private static void mapStoneGenerators()
	{
		//Only needs to calculate this once instead of every generate call
		if (Cfg.worldCat.vanillaOverridesCat.replaceVanillaStoneGeneration && !Cfg.disableVanillaVariants()) 
		{
			DO_VANILLA_STONE_GEN = true;

			dirt = new WorldGenMinable(Blocks.DIRT.getDefaultState(), Cfg.worldCat.stoneGenVarsCat.dirtSize);
			gravel = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), Cfg.worldCat.stoneGenVarsCat.gravelSize);
			andesite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), Cfg.worldCat.stoneGenVarsCat.andesiteSize);
			diorite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), Cfg.worldCat.stoneGenVarsCat.dioriteSize);
			granite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), Cfg.worldCat.stoneGenVarsCat.graniteSize);
						
			if (Cfg.worldCat.stoneGenVarsCat.stoneInLayers)
			{
				ANDESITE_MIN = Cfg.worldCat.stoneGenVarsCat.andesiteLayer == 1 ? 0 : Cfg.worldCat.stoneGenVarsCat.andesiteLayer == 2 ? 25 : Cfg.worldCat.stoneGenVarsCat.andesiteLayer == 3 ? 40 : 25;
				ANDESITE_INCR = (Cfg.worldCat.stoneGenVarsCat.andesiteLayer == 1 ? 20 : Cfg.worldCat.stoneGenVarsCat.andesiteLayer == 2 ? 45 : Cfg.worldCat.stoneGenVarsCat.andesiteLayer == 3 ? 80 : 45) - ANDESITE_MIN + 1;
				DIORITE_MIN = Cfg.worldCat.stoneGenVarsCat.dioriteLayer == 1 ? 0 : Cfg.worldCat.stoneGenVarsCat.dioriteLayer == 2 ? 25 : Cfg.worldCat.stoneGenVarsCat.dioriteLayer == 3 ? 40 : 40;
				DIORITE_INCR = (Cfg.worldCat.stoneGenVarsCat.dioriteLayer == 1 ? 20 : Cfg.worldCat.stoneGenVarsCat.dioriteLayer == 2 ? 45 : Cfg.worldCat.stoneGenVarsCat.dioriteLayer == 3 ? 80 : 80) - DIORITE_MIN + 1;
				GRANITE_MIN = Cfg.worldCat.stoneGenVarsCat.graniteLayer == 1 ? 0 : Cfg.worldCat.stoneGenVarsCat.graniteLayer == 2 ? 25 : Cfg.worldCat.stoneGenVarsCat.graniteLayer == 3 ? 40 : 0;
				GRANITE_INCR = (Cfg.worldCat.stoneGenVarsCat.graniteLayer == 1 ? 20 : Cfg.worldCat.stoneGenVarsCat.graniteLayer == 2 ? 45 : Cfg.worldCat.stoneGenVarsCat.graniteLayer == 3 ? 80 : 20) - GRANITE_MIN + 1;
			}

			STONE_COUNT = Cfg.worldCat.stoneGenVarsCat.stoneCount == -1 ? 5 : Cfg.worldCat.stoneGenVarsCat.stoneCount == 0 ? 10 : Cfg.worldCat.stoneGenVarsCat.stoneCount == 1 ? 20 : Cfg.worldCat.stoneGenVarsCat.stoneCount == 2 ? 40 : 10;
		}
	}
	
	private static void handleMapping(String originalName, WorldGenProperties genProp)
	{
		Map<IBlockState, IBlockState> genStateMap = getWorldGenMap(originalName);
		
		if (!genStateMap.isEmpty())
		{
			Map<IBlockState, IBlockState> denseStateMap = getWorldGenMap("dense_" + originalName);

			WorldGenerator generator;
			
			if (denseStateMap.isEmpty())
			{
				generator = new WorldGenMinableMod(genStateMap, genProp.getBlockCount());
			} 

			else generator = new WorldGenMinableMod(genStateMap, genProp.getBlockCount(), denseStateMap, Cfg.denseCat.generalDenseCat.denseVariantFrequency * genProp.getDenseVariantRatio());

			ORE_WORLDGEN_MAP.put(genProp, generator);
		}
	}
	
	public static Map<IBlockState, IBlockState> getWorldGenMap(String nameMatcher)
	{
		Map<IBlockState, IBlockState> genStateMap = new HashMap<>();
		
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			if (state.getBlock() instanceof BlockOresBase)
			{
				BlockOresBase asBOB = (BlockOresBase) state.getBlock();

				if (CommonMethods.getOre(asBOB.getOriginalName()).equals(nameMatcher))
				{
					IBlockState backgroundBlockState = asBOB.getBackgroundBlockState(state);
					
					if (!backgroundBlockState.getBlock().equals(Blocks.AIR))
					{
						genStateMap.put(backgroundBlockState, state);
					}
				}
			}
			
			else logger.warn("Error: Could not cast to BlockOresBase. Background blockstate not retrieved.");
		}
		
		return genStateMap;
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
	{
		setupChunkSelector(world);
		
		int dimension = world.provider.getDimension();
		
		//If the current dimension is not whitelisted, do nothing.
		if (!ArrayUtils.isEmpty(Cfg.worldCat.genDimensionsCat.dimensionWhitelist) && 
			!ArrayUtils.contains(Cfg.worldCat.genDimensionsCat.dimensionWhitelist, dimension))
		{
			return;
		}		

		int blockX = chunkX * 16, blockZ = chunkZ * 16;

		if (dimension == 0 && DO_VANILLA_STONE_GEN)
		{
			runGenerator(dirt, world, random, blockX, blockZ, 10, 0, 257);
			runGenerator(gravel, world, random, blockX, blockZ, 8, 0, 257);
			runGenerator(andesite, world, random, blockX, blockZ, STONE_COUNT, ANDESITE_MIN, ANDESITE_INCR);
			runGenerator(diorite, world, random, blockX, blockZ, STONE_COUNT, DIORITE_MIN, DIORITE_INCR);
			runGenerator(granite, world, random, blockX, blockZ, STONE_COUNT, GRANITE_MIN, GRANITE_INCR);
		}

		Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX * 16, 0, chunkZ * 16));

		//Do ore generation.
		for (Map.Entry<WorldGenProperties, WorldGenerator> genEntry : ORE_WORLDGEN_MAP.entrySet())
		{
			WorldGenProperties genProp = genEntry.getKey();
			
			if (canRunGenerator(genProp, biome, dimension, random, chunkX, chunkZ))
			{
				int minHeight = genProp.getMinHeight(), maxHeight = genProp.getMaxHeight();
				
				if (minHeight > maxHeight || minHeight < 0 || maxHeight > 256)
				{
					throw new IllegalArgumentException("Ore generated out of bounds.");
				}
				
				if (Cfg.worldCat.oreGenCat.largeOreClusters)
				{
					double probability = chunkSelector.getProbabilityForCoordinates(genProp.getUniqueId(), chunkX, chunkZ);

					runGeneratorByChance(genEntry.getValue(), world, random, blockX, blockZ, genProp.getFrequency(), minHeight, maxHeight - minHeight + 1, probability);
				}
				
				else runGenerator(genEntry.getValue(), world, random, blockX, blockZ, genProp.getFrequency(), minHeight, maxHeight - minHeight + 1);				
			}
		}
	}
	
	private static boolean canRunGenerator(WorldGenProperties genProp, Biome biome, int dimension, Random random, int x, int z)
	{
		//If the current dimension is blacklisted, stop.
		if (genProp.hasDimensionBlacklist() && genProp.getDimensionBlacklist().contains(dimension))
		{
			return false;
		}

		//If the current biome is blacklisted, stop.		
		if (genProp.hasBiomeBlacklist() && genProp.getBiomeBlacklist().contains(biome.getRegistryName().toString()))
		{
			return false;
		}

		//If it isn't on the dimension list and biome list, stop.
		return (!genProp.hasDimensionMatcher() || genProp.getDimensionList().contains(dimension)) &&
			   (!genProp.hasBiomeMatcher() || genProp.getBiomeList().contains(biome.getRegistryName().toString()));

	}

	private void runGenerator(WorldGenerator gen, World world, Random rand, int blockX, int blockZ, int frequency, int minHeight, int maxHeight)
	{
		for (int i = 0; i < frequency; i++)
		{
			gen.generate(world, rand, new BlockPos(blockX + rand.nextInt(16), minHeight + rand.nextInt(maxHeight), blockZ + rand.nextInt(16)));
		}
	}
	
	/*
	 * Slower
	 */
	private void runGeneratorByChance(WorldGenerator gen, World world, Random rand, int blockX, int blockZ, int frequency, int minHeight, int maxHeight, double chance)
	{
		if (chance != 0.0)
		{
			for (int i = 0; i < frequency; i++)
			{
				if (rand.nextInt(100) < chance)
				{
					gen.generate(world, rand, new BlockPos(blockX + rand.nextInt(16), minHeight + rand.nextInt(maxHeight), blockZ + rand.nextInt(16)));
				}
			}
		}
	}
	
	private static World previousWorld;
	
	private void setupChunkSelector(World world)
	{
		if (!world.equals(previousWorld))
		{
			previousWorld = world;
			
			if (Cfg.worldCat.oreGenCat.largeOreClusters)
			{
				chunkSelector = new RandomChunkSelector(world.getSeed());
			}
		}
	}
}