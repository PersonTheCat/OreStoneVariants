package personthecat.mod.world.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.DefaultProperties.DefaultWorldGenProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.VariantOnly;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class WorldGenCustomOres implements IWorldGenerator
{
	private WorldGenerator dirt, gravel, andesite, diorite, granite;
	
	public WorldGenCustomOres()
	{				
		DefaultWorldGenProperties[] initWorldGenPropertiesHaxThisDevSux = DefaultWorldGenProperties.values();
		
		dirt = new WorldGenMinable(Blocks.DIRT.getDefaultState(), ConfigFile.dirtSize);
		gravel = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), ConfigFile.gravelSize);
		andesite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), ConfigFile.andesiteSize);
		diorite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), ConfigFile.dioriteSize);
		granite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), ConfigFile.graniteSize);

		mapWorldGenerators();
	}
	
	static final Map<WorldGenProperties, List<WorldGenerator>> NORMAL_WORLDGEN_MAP = new HashMap<WorldGenProperties, List<WorldGenerator>>();
	static final Map<WorldGenProperties, List<WorldGenerator>> DENSE_WORLDGEN_MAP = new HashMap<WorldGenProperties, List<WorldGenerator>>();
	
	//We're now trying to offload as much of the world generation process as we can to happen during init vs. on world generation in an effort to increase performance, where possible.
	private static void mapWorldGenerators()
	{		
		//Mapping all normal-case WorldGenerators.
		for (WorldGenProperties genProp : WorldGenProperties.getWorldGenPropertyRegistry())
		{
			if (genProp.getName().contains("lit_")) continue;
			
			List<WorldGenerator> normalGenList = new ArrayList<WorldGenerator>();
			List<WorldGenerator> denseGenList = new ArrayList<WorldGenerator>();
			
			for (IBlockState state : BlockInit.BLOCKSTATES)
			{
				if (NameReader.getOre(state.getBlock().getRegistryName().getResourcePath()).equals(genProp.getName()))
				{					
					normalGenList.add(new WorldGenMinable(state, genProp.getBlockCount(), VariantOnly.forBlockState(getBackgroundBlockState(state))));
				}
				
				if (NameReader.getOre(state.getBlock().getRegistryName().getResourcePath()).equals("dense_" + genProp.getName()))
				{
					int metaIsTheSame = state.getBlock().getMetaFromState(state);
					IBlockState counterpart = NameReader.getNormalVariant(state.getBlock()).getStateFromMeta(metaIsTheSame);
											
					denseGenList.add(new WorldGenMinable(state, 3, VariantOnly.forBlockState(counterpart)));
				}
			}
						
			if (!normalGenList.isEmpty()) NORMAL_WORLDGEN_MAP.put(genProp, normalGenList);
			if (!denseGenList.isEmpty()) DENSE_WORLDGEN_MAP.put(WorldGenProperties.getDenseProperties(genProp), denseGenList);
		}
		
		//Mapping all special-case WorldGenerators; these are not directly associated with any blocks. Will eventually add stone, gravel, and dirt here.
		if (PropertyGroup.getPropertyGroup("thermalfoundation").getConditions())
		{
			List<WorldGenProperties> specialGenProperties = new ArrayList<WorldGenProperties>();
			specialGenProperties.add(WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_ocean"));
			specialGenProperties.add(WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_high"));
			
			for (WorldGenProperties genProp : specialGenProperties)
			{
				List<WorldGenerator> genList = new ArrayList<WorldGenerator>();
				
				for (IBlockState state : BlockInit.BLOCKSTATES)
				{				
					//Basically just getting all of the blockstates which are of the type thermalfoundation_copper_ore.
					if (NameReader.getOre(state.getBlock().getRegistryName().getResourcePath()).equals("thermalfoundation_copper_ore"))
					{
						genList.add(new WorldGenMinable(state, genProp.getBlockCount(), VariantOnly.forBlockState(getBackgroundBlockState(state))));
					}
				}
				
				if (!genList.isEmpty()) NORMAL_WORLDGEN_MAP.put(genProp, genList);
			}
		}
	}
	
	private static IBlockState getBackgroundBlockState(IBlockState state)
	{
		IBlockState backgroundBlockState = null;
		
		if (!NameReader.isDynamic(state.getBlock())) 
		{
			BlockStateGenerator.State variant = BlockInit.BLOCKSTATE_STATE_MAP.get(state);
			backgroundBlockState = variant.getBackgroundBlockState();
		}
		
		else 
		{
			try
			{
				int i = BlockInit.DYNAMIC_BLOCKSTATES_NUMBER_MAP.get(state);
				backgroundBlockState = ConfigInterpreter.getBackgroundBlockState(i);
			}
			
			catch (IOException e) {System.err.println("If you're seeing this, one of your dynamic block entries may be incorrect."); e.getSuppressed();}
		}
		
		return backgroundBlockState;
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
	{
		Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX * 16, 0, chunkZ * 16));
		int dimension = world.provider.getDimension();
		
		//If the current dimension is not whitelisted, do nothing.
		if (!ArrayUtils.contains(ConfigFile.dimensionWhitelist, dimension)) return;
		
		if (dimension == 0 && ConfigFile.replaceVanillaStoneGeneration)
		{
			int andesiteY1 = 0, andesiteY2 = 80, dioriteY1 = 0, dioriteY2 = 80, graniteY1 = 0, graniteY2 = 80;
			
			if (ConfigFile.stoneInLayers)
			{				
				andesiteY1 = ConfigFile.andesiteLayer == 1 ? 0 : ConfigFile.andesiteLayer == 2 ? 25 : ConfigFile.andesiteLayer == 3 ? 40 : 25;
				andesiteY2 = ConfigFile.andesiteLayer == 1 ? 20 : ConfigFile.andesiteLayer == 2 ? 45 : ConfigFile.andesiteLayer == 3 ? 80 : 45;
				dioriteY1 = ConfigFile.dioriteLayer == 1 ? 0 : ConfigFile.dioriteLayer == 2 ? 25 : ConfigFile.dioriteLayer == 3 ? 40 : 40;
				dioriteY2 = ConfigFile.dioriteLayer == 1 ? 20 : ConfigFile.dioriteLayer == 2 ? 45 : ConfigFile.dioriteLayer == 3 ? 80 : 80;
				graniteY1 = ConfigFile.graniteLayer == 1 ? 0 : ConfigFile.graniteLayer == 2 ? 25 : ConfigFile.graniteLayer == 3 ? 40 : 0;
				graniteY2 = ConfigFile.graniteLayer == 1 ? 20 : ConfigFile.graniteLayer == 2 ? 45 : ConfigFile.graniteLayer == 3 ? 80 : 20;
			}
				
			int stoneCount = ConfigFile.stoneCount == -1 ? 5 : ConfigFile.stoneCount == 0 ? 10 : ConfigFile.stoneCount == 1 ? 20 : ConfigFile.stoneCount == 2 ? 40 : 10;
			
			runGenerator(dirt, world, random, chunkX, chunkZ, 10, 0, 256);
			runGenerator(gravel, world, random, chunkX, chunkZ, 8, 0, 256);
			runGenerator(andesite, world, random, chunkX, chunkZ, stoneCount, andesiteY1, andesiteY2);
			runGenerator(diorite, world, random, chunkX, chunkZ, stoneCount, dioriteY1, dioriteY2);
			runGenerator(granite, world, random, chunkX, chunkZ, stoneCount, graniteY1, graniteY2);
			
		}	
		
		//Do normal ore generation.
		for (WorldGenProperties genProp : NORMAL_WORLDGEN_MAP.keySet())
		{			
			if (canRunGenerator(genProp, biome, dimension))
			{				
				for (WorldGenerator generator : NORMAL_WORLDGEN_MAP.get(genProp))
				{
					runGenerator((generator), world, random, chunkX, chunkZ, genProp.getChance(), genProp.getMinHeight(), genProp.getMaxHeight());
				}
			}
		}
		
		//Do dense ore generation; must happen after normal ore generation.
		for (WorldGenProperties denseGenProp : DENSE_WORLDGEN_MAP.keySet())
		{
			if (canRunGenerator(denseGenProp, biome, dimension))
			{
				for (WorldGenerator generator : DENSE_WORLDGEN_MAP.get(denseGenProp))
				{
					runGenerator((generator), world, random, chunkX, chunkZ, denseGenProp.getChance(), denseGenProp.getMinHeight(), denseGenProp.getMaxHeight());
				}
			}
		}
	}
	
	private static boolean canRunGenerator(WorldGenProperties genProp, Biome biome, int dimension)
	{
		//If the current biome is blacklisted, stop.		
		if (genProp.getHasBiomeBlacklist() && genProp.getBiomeBlacklist().contains(biome.getRegistryName().toString())) return false;
		
		//If there is nothing to match, go ahead and generate and then iterate to avoid unnecessary calculations.
		if (!genProp.getHasBiomeMatcher()) return true;
		
		else for (String biomeName : genProp.getBiomeList())
		{
			//If the biome is also on the biomeList, go ahead and return true.
			if (biomeName.equals(biome.getRegistryName().toString())) return true;
		}

		return false;
	}
	
	private void runGenerator(WorldGenerator gen, World world, Random rand, int chunkX, int chunkZ, int chance, int minHeight, int maxHeight)
	{
		if (minHeight > maxHeight || minHeight < 0 || maxHeight > 256) throw new IllegalArgumentException("Ore generated our of bounds.");
		
		int heightDiff = maxHeight - minHeight + 1;
		for(int i = 0; i < chance; i++)
		{
			int x = chunkX * 16 + rand.nextInt(16);
			int y = minHeight + rand.nextInt(heightDiff);
			int z = chunkZ * 16 + rand.nextInt(16);
			
			gen.generate(world, rand, new BlockPos(x,y,z));
		}
	}
}