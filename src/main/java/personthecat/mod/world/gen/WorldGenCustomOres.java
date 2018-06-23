package personthecat.mod.world.gen;

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
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.ArrayUtils;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.NameReader;

import java.util.*;

public class WorldGenCustomOres implements IWorldGenerator
{
	private static boolean DO_VANILLA_STONE_GEN;
	private static int ANDESITE_MIN, DIORITE_MIN, GRANITE_MIN, ANDESITE_MAX = 81, DIORITE_MAX = 81, GRANITE_MAX = 81, STONE_COUNT = 10;
	
	private WorldGenerator dirt, gravel, andesite, diorite, granite;
	
	private static final Map<WorldGenProperties, WorldGenerator> ORE_WORLDGEN_MAP = new HashMap<>();
	
	public WorldGenCustomOres()
	{
		dirt = new WorldGenMinable(Blocks.DIRT.getDefaultState(), ConfigFile.dirtSize);
		gravel = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), ConfigFile.gravelSize);
		andesite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), ConfigFile.andesiteSize);
		diorite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), ConfigFile.dioriteSize);
		granite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), ConfigFile.graniteSize);

		mapNormalGenerators();
		mapStoneGenerators();
	}
	
	//We're now trying to offload as much of the world generation process as we can to happen during init vs. on world generation in an effort to increase performance, where possible.	
	private static void mapNormalGenerators()
	{
		for (WorldGenProperties genProp : WorldGenProperties.getWorldGenPropertyRegistry())
		{			
			if (genProp.getName().contains("lit_")) continue;
			
			PropertyGroup group = PropertyGroup.getGroupByProperties(genProp.getOreProperties());
			
			if (group == null || !Loader.isModLoaded(group.getModName())) continue;

			handleMapping(genProp.getName(), genProp);		
			
			if (genProp.hasAdditionalProperties())
			{				
				for (WorldGenProperties moreProps : genProp.getAdditionalProperties())
				{
					handleMapping(genProp.getName(), moreProps);
				}
			}
		}
	}
	
	private static void mapStoneGenerators()
	{
		//Only needs to calculate this once instead of every generate call
		if (ConfigFile.replaceVanillaStoneGeneration && !ConfigFile.disableVanillaVariants()) 
		{
			DO_VANILLA_STONE_GEN = true;

			if (ConfigFile.stoneInLayers)
			{
				ANDESITE_MIN = ConfigFile.andesiteLayer == 1 ? 0 : ConfigFile.andesiteLayer == 2 ? 25 : ConfigFile.andesiteLayer == 3 ? 40 : 25;
				ANDESITE_MAX = (ConfigFile.andesiteLayer == 1 ? 20 : ConfigFile.andesiteLayer == 2 ? 45 : ConfigFile.andesiteLayer == 3 ? 80 : 45) - ANDESITE_MIN + 1;
				DIORITE_MIN = ConfigFile.dioriteLayer == 1 ? 0 : ConfigFile.dioriteLayer == 2 ? 25 : ConfigFile.dioriteLayer == 3 ? 40 : 40;
				DIORITE_MAX = (ConfigFile.dioriteLayer == 1 ? 20 : ConfigFile.dioriteLayer == 2 ? 45 : ConfigFile.dioriteLayer == 3 ? 80 : 80) - DIORITE_MIN + 1;
				GRANITE_MIN = ConfigFile.graniteLayer == 1 ? 0 : ConfigFile.graniteLayer == 2 ? 25 : ConfigFile.graniteLayer == 3 ? 40 : 0;
				GRANITE_MAX = (ConfigFile.graniteLayer == 1 ? 20 : ConfigFile.graniteLayer == 2 ? 45 : ConfigFile.graniteLayer == 3 ? 80 : 20) - GRANITE_MIN + 1;
			}

			STONE_COUNT = ConfigFile.stoneCount == -1 ? 5 : ConfigFile.stoneCount == 0 ? 10 : ConfigFile.stoneCount == 1 ? 20 : ConfigFile.stoneCount == 2 ? 40 : 10;
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
			
			//Technically this also would allow genProp to have a custom dense replacement chance set
			else generator = new WorldGenMinableMod(genStateMap, genProp.getBlockCount(), denseStateMap, 0.125); //on average 1 in every 8 becomes dense

			ORE_WORLDGEN_MAP.put(genProp, generator);
		}
	}
	
	private static Map<IBlockState, IBlockState> getWorldGenMap(String nameMatcher)
	{
		Map<IBlockState, IBlockState> genStateMap = new HashMap<>();
		
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			if (NameReader.getOre(state.getBlock().getRegistryName().getResourcePath()).equals(nameMatcher))
			{
				if (state.getBlock() instanceof BlockOresBase)
				{
					BlockOresBase asBOB = (BlockOresBase) state.getBlock();
					
					if (asBOB.isLitRedstone()) continue; //Really have no idea why this is still necessary here...

					IBlockState backgroundBlockState = asBOB.getBackgroundBlockState(asBOB.getMetaFromState(state));
					
					if (!backgroundBlockState.getBlock().equals(Blocks.AIR))
					{
						genStateMap.put(backgroundBlockState, state);
					}
				}
				
				else System.err.println("Error: Could not cast to BlockOresBase. Background blockstate not retrieved.");
			}
		}
		
		return genStateMap;
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
	{
		int dimension = world.provider.getDimension();
		
		//If the current dimension is not whitelisted, do nothing.
		if (!ArrayUtils.contains(ConfigFile.dimensionWhitelist, dimension)) return;

		int blockX = chunkX * 16, blockZ = chunkZ * 16;

		if (dimension == 0 && DO_VANILLA_STONE_GEN)
		{
			runGenerator(dirt, world, random, blockX, blockZ, 10, 0, 257);
			runGenerator(gravel, world, random, blockX, blockZ, 8, 0, 257);
			runGenerator(andesite, world, random, blockX, blockZ, STONE_COUNT, ANDESITE_MIN, ANDESITE_MAX);
			runGenerator(diorite, world, random, blockX, blockZ, STONE_COUNT, DIORITE_MIN, DIORITE_MAX);
			runGenerator(granite, world, random, blockX, blockZ, STONE_COUNT, GRANITE_MIN, GRANITE_MAX);
		}

		Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX * 16, 0, chunkZ * 16));

		//Do ore generation.
		for (Map.Entry<WorldGenProperties, WorldGenerator> genEntry : ORE_WORLDGEN_MAP.entrySet())
		{
			WorldGenProperties genProp = genEntry.getKey();
			
			if (canRunGenerator(genProp, biome, dimension))
			{	//no ore at y = 20 and lower why
				int minHeight = genProp.getMinHeight(), maxHeight = genProp.getMaxHeight();
				
				if (minHeight > maxHeight || minHeight < 0 || maxHeight > 256)
				{
					throw new IllegalArgumentException("Ore generated out of bounds.");
				}
				
				runGenerator(genEntry.getValue(), world, random, blockX, blockZ, genProp.getChance(), minHeight, maxHeight - minHeight + 1);
			}
		}
	}
	
	private static boolean canRunGenerator(WorldGenProperties genProp, Biome biome, int dimension)
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
		
		return (!genProp.hasDimensionMatcher() || genProp.getDimensionList().contains(dimension)) &&
			   (!genProp.hasBiomeMatcher() || genProp.getBiomeList().contains(biome.getRegistryName().toString()));
	}

	private void runGenerator(WorldGenerator gen, World world, Random rand, int blockX, int blockZ, int chance, int minHeight, int maxHeight)
	{
		for (int i = 0; i < chance; i++)
		{
			gen.generate(world, rand, new BlockPos(blockX + rand.nextInt(16), minHeight + rand.nextInt(maxHeight), blockZ + rand.nextInt(16)));
		}
	}
}
