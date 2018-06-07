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
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class WorldGenCustomOres implements IWorldGenerator
{
	private WorldGenerator dirt, gravel, andesite, diorite, granite;
	
	public WorldGenCustomOres()
	{
		dirt = new WorldGenMinable(Blocks.DIRT.getDefaultState(), ConfigFile.dirtSize);
		gravel = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), ConfigFile.gravelSize);
		andesite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), ConfigFile.andesiteSize);
		diorite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), ConfigFile.dioriteSize);
		granite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), ConfigFile.graniteSize);

		mapNormalGenerators();
	}
	
	static final Map<WorldGenProperties, WorldGenerator[]> NORMAL_WORLDGEN_MAP = new HashMap<>();
	static final Map<WorldGenProperties, WorldGenerator[]> DENSE_WORLDGEN_MAP = new HashMap<>();
	
	//We're now trying to offload as much of the world generation process as we can to happen during init vs. on world generation in an effort to increase performance, where possible.	
	private static void mapNormalGenerators()
	{
		for (WorldGenProperties genProp : WorldGenProperties.getWorldGenPropertyRegistry())
		{			
			if (genProp.getName().contains("lit_")) continue;
			
			PropertyGroup group = PropertyGroup.getGroupByProperties(genProp.getOreProperties());
			
			if (group == null || !group.getConditions()) continue;
			
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
	
	private static void handleMapping(String originalName, WorldGenProperties genProp)
	{
		if (!ArrayUtils.isEmpty(getWorldGenArray(originalName, genProp)))
		{
			NORMAL_WORLDGEN_MAP.put(genProp, getWorldGenArray(originalName, genProp));
		}
		
		if (!ArrayUtils.isEmpty(getWorldGenArray("dense_" + originalName, genProp)))
		{
			DENSE_WORLDGEN_MAP.put(WorldGenProperties.getDenseProperties(genProp), getDenseWorldGenArray(genProp.getName(), genProp));
		}
	}
	
	private static WorldGenerator[] getWorldGenArray(String nameMatcher, WorldGenProperties genProp)
	{
		List<WorldGenerator> genList = new ArrayList<>();
		
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			if (NameReader.getOre(state.getBlock().getRegistryName().getResourcePath()).equals(nameMatcher))
			{
				genList.add(new WorldGenMinableMod(state, genProp.getBlockCount(), getBackgroundBlockState(state)));
			}
		}

		return genList.toArray(new WorldGenerator[genList.size()]);
	}
	
	private static WorldGenerator[] getDenseWorldGenArray(String nameMatcher, WorldGenProperties genProp)
	{
		List<WorldGenerator> genList = new ArrayList<>();
		
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			if (NameReader.getOre(state.getBlock().getRegistryName().getResourcePath()).equals("dense_" + nameMatcher))
			{
				int metaIsTheSame = state.getBlock().getMetaFromState(state);
				IBlockState counterpart = NameReader.getNormalVariant(state.getBlock()).getStateFromMeta(metaIsTheSame);
				
				genList.add(new WorldGenMinableMod(state, 3, counterpart));
			}
		}
		
		return genList.toArray(new WorldGenerator[genList.size()]);
	}	
	
	private static IBlockState getBackgroundBlockState(IBlockState state)
	{
		IBlockState backgroundBlockState = null;
		
		if (!NameReader.isDynamic(state.getBlock())) 
		{
			State variant = BlockInit.BLOCKSTATE_STATE_MAP.get(state);
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
	
	@SubscribeEvent
	public void onOreGenEvent(OreGenEvent.Post event)
	{
		World world = event.getWorld();
		
		ChunkProviderServer chunkProviderServer = world.getMinecraftServer().getWorld(world.provider.getDimension()).getChunkProvider();
		
		generate(event.getRand(), (event.getPos().getX() / 16), (event.getPos().getZ() / 16), world, chunkProviderServer.chunkGenerator, chunkProviderServer);
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
				//Normal for loops are usually faster than for each loops. Could be important for world generation.
				for (int i = 0; i < NORMAL_WORLDGEN_MAP.get(genProp).length; i++) 
				{
					runGenerator((NORMAL_WORLDGEN_MAP.get(genProp)[i]), world, random, chunkX, chunkZ, genProp.getChance(), genProp.getMinHeight(), genProp.getMaxHeight());
				}
			}
		}
		
		//Do dense ore generation; must happen after normal ore generation.
		for (WorldGenProperties denseGenProp : DENSE_WORLDGEN_MAP.keySet())
		{
			if (canRunGenerator(denseGenProp, biome, dimension))
			{
				for (int i = 0; i < DENSE_WORLDGEN_MAP.get(denseGenProp).length; i++) 
				{
					runGenerator((DENSE_WORLDGEN_MAP.get(denseGenProp)[i]), world, random, chunkX, chunkZ, denseGenProp.getChance(), denseGenProp.getMinHeight(), denseGenProp.getMaxHeight());
				}
			}
		}
	}
	
	private static boolean canRunGenerator(WorldGenProperties genProp, Biome biome, int dimension)
	{
		//If the current dimension is blacklisted, stop.
		if (genProp.hasDimensionBlacklist() && genProp.getDimensionBlacklist().contains(dimension)) return false;
		
		//If the current biome is blacklisted, stop.		
		if (genProp.hasBiomeBlacklist() && genProp.getBiomeBlacklist().contains(biome.getRegistryName().toString())) return false;
		
		boolean dimensionIsListed = false, biomeIsListed = false;
		
		if (genProp.hasDimensionMatcher())
		{
			for (int dimNumber : genProp.getDimensionList())
			{
				if (dimNumber == dimension) dimensionIsListed = true;
			}
		}
		
		else dimensionIsListed = true;
		
		if (genProp.hasBiomeMatcher())
		{
			for (String biomeName : genProp.getBiomeList())
			{
				if (biomeName.equals(biome.getRegistryName().toString())) biomeIsListed = true;
			}
		}
		
		else biomeIsListed = true;
		
		return dimensionIsListed && biomeIsListed;
	}
	
	private void runGenerator(WorldGenerator gen, World world, Random rand, int chunkX, int chunkZ, int chance, int minHeight, int maxHeight)
	{
		if (minHeight > maxHeight || minHeight < 0 || maxHeight > 256) throw new IllegalArgumentException("Ore generated out of bounds.");
		
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