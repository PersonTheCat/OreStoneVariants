package personthecat.mod.world.gen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.DefaultProperties;
import personthecat.mod.properties.DefaultProperties.DefaultWorldGenProperties;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.Reference;
import personthecat.mod.util.VariantOnly;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class WorldGenCustomOres implements IWorldGenerator
{
	private WorldGenerator dirt, gravel, andesite, diorite, granite;
	private int andesiteY1 = 0,	andesiteY2 = 0,	dioriteY1 = 0, dioriteY2 = 80, graniteY1 = 80, graniteY2 = 80;
	private int stoneCount;
	
	public WorldGenCustomOres()
	{				
		DefaultWorldGenProperties[] initWorldGenPropertiesHaxThisDevSux = DefaultWorldGenProperties.values();
		
		dirt = new WorldGenMinable(Blocks.DIRT.getDefaultState(), ConfigFile.dirtSize);
		gravel = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), ConfigFile.gravelSize);
		andesite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), ConfigFile.andesiteSize);
		diorite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), ConfigFile.dioriteSize);
		granite = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), ConfigFile.graniteSize);
		
		addNormalGeneration();
	}
	
	static final Map<IBlockState, WorldGenerator> WORLDGEN = new HashMap<IBlockState, WorldGenerator>();
	
	private static void addNormalGeneration()
	{
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			if (!NameReader.isLit(state.getBlock()))
			{
				WorldGenProperties gen_prop = BlockInit.BLOCK_PROPERTY_MAP.get(state.getBlock()).getWorldGenProperties();
				
				IBlockState backgroundBlockState = null;
				
				if (NameReader.isDynamic(state.getBlock())) 
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
				
				//This will get its own method once I fix dense ores for dynamic blocks.
				if (NameReader.isDense(state.getBlock()))
				{
					int metaIsTheSame = state.getBlock().getMetaFromState(state);
					IBlockState counterpart = NameReader.getNormalVariant(state.getBlock()).getStateFromMeta(metaIsTheSame);
					
					WORLDGEN.put(state, (new WorldGenMinable(state, 3, VariantOnly.forBlockState(counterpart))));
				}
				
				else WORLDGEN.put(state, (new WorldGenMinable(state, gen_prop.getBlockCount(), VariantOnly.forBlockState(backgroundBlockState))));
			}
		}
	}

	public void runGenerator(WorldGenerator gen, World world, Random rand, int chunkX, int chunkZ, int chance, int minHeight, int maxHeight)
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
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
	{
		//Currently only used for one dimension, the overworld. I'm not sure what kind of performance impact this has and so I may eventually remove it altogether. I just need to test that first.
		switch(world.provider.getDimension())
		{
		case 0:
			
			if (ConfigFile.replaceVanillaStoneGeneration)
			{
				if (ConfigFile.stoneInLayers)
				{
					andesiteY1 = ConfigFile.andesiteLayer == 1 ? 0 : ConfigFile.andesiteLayer == 2 ? 25 : ConfigFile.andesiteLayer == 3 ? 40 : 25;
					andesiteY2 = ConfigFile.andesiteLayer == 1 ? 20 : ConfigFile.andesiteLayer == 2 ? 45 : ConfigFile.andesiteLayer == 3 ? 80 : 45;
					dioriteY1 = ConfigFile.dioriteLayer == 1 ? 0 : ConfigFile.dioriteLayer == 2 ? 25 : ConfigFile.dioriteLayer == 3 ? 40 : 40;
					dioriteY2 = ConfigFile.dioriteLayer == 1 ? 20 : ConfigFile.dioriteLayer == 2 ? 45 : ConfigFile.dioriteLayer == 3 ? 80 : 80;
					graniteY1 = ConfigFile.graniteLayer == 1 ? 0 : ConfigFile.graniteLayer == 2 ? 25 : ConfigFile.graniteLayer == 3 ? 40 : 0;
					graniteY2 = ConfigFile.graniteLayer == 1 ? 20 : ConfigFile.graniteLayer == 2 ? 45 : ConfigFile.graniteLayer == 3 ? 80 : 20;
				}
					
				stoneCount = ConfigFile.stoneCount == -1 ? 5 : ConfigFile.stoneCount == 0 ? 10 : ConfigFile.stoneCount == 1 ? 20 : ConfigFile.stoneCount == 2 ? 40 : 10;
				
				runGenerator(dirt, world, random, chunkX, chunkZ, 10, 0, 256);
				runGenerator(gravel, world, random, chunkX, chunkZ, 8, 0, 256);
				runGenerator(andesite, world, random, chunkX, chunkZ, stoneCount, andesiteY1, andesiteY2);
				runGenerator(diorite, world, random, chunkX, chunkZ, stoneCount, dioriteY1, dioriteY2);
				runGenerator(granite, world, random, chunkX, chunkZ, stoneCount, graniteY1, graniteY2);
				
				Biome biomeName = world.getBiomeForCoordsBody(new BlockPos(chunkX * 16, 0, chunkZ * 16));
				
				if (ConfigFile.overworldGeneration)
				{				
					for (IBlockState state : BlockInit.BLOCKSTATES)
					{
						if (!state.getBlock().getLocalizedName().contains("lit_"))
						{
							BlockStateGenerator.State variant = BlockInit.BLOCKSTATE_STATE_MAP.get(state);
							WorldGenProperties gen_prop = OreProperties.propertiesOf(state.getBlock().getRegistryName().getResourcePath()).getWorldGenProperties();	
							
							if (gen_prop.getName().equals("thermalfoundation_copper_ore"))
							{
								if (world.getActualHeight() > 47)
								{
									gen_prop = WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_high");
								}
								
								if (BiomeDictionary.hasType(biomeName, Type.OCEAN))
								{
									gen_prop = WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_ocean");
								}
							}
							
							if (!NameReader.isDense(state.getBlock()))
							{
								if (gen_prop.getHasBiomeNameMatcher() && ConfigFile.biomeSpecificOres)
								{
									for (String biome : gen_prop.getBiomeNameList())
									{				
										if (biomeName == ForgeRegistries.BIOMES.getValue(new ResourceLocation(biome)))
										{
											runGenerator((WORLDGEN.get(state)), world, random, chunkX, chunkZ, gen_prop.getChance(), gen_prop.getMinHeight(), gen_prop.getMaxHeight());
										}
									}
								}
								
								else if (gen_prop.getHasBiomeTypeMatcher() && ConfigFile.biomeSpecificOres)
								{
									for (Type type : gen_prop.getBiomeTypeList())
									{																		
										if (BiomeDictionary.hasType(biomeName, type))
										{
											runGenerator((WORLDGEN.get(state)), world, random, chunkX, chunkZ, gen_prop.getChance(), gen_prop.getMinHeight(), gen_prop.getMaxHeight());
										}
									}
								}
								
								else runGenerator((WORLDGEN.get(state)), world, random, chunkX, chunkZ, gen_prop.getChance(), gen_prop.getMinHeight(), gen_prop.getMaxHeight());
							}
							
							//We need another if statement, not an else statement, because this needs to happen after and not instead of the first if. 
							if (NameReader.isDense(state.getBlock()))
							{
								runGenerator((WORLDGEN.get(state)), world, random, chunkX, chunkZ, 1400, gen_prop.getMinHeight(), gen_prop.getMaxHeight());
							}	
						}
					}
				}
			}
			
			break;
		}
		
		//For dynamic blocks with no biome matcher; for all dynamic blocks when biomeSpecificOres is/are disabled. 
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			WorldGenProperties gen_prop = BlockInit.BLOCK_PROPERTY_MAP.get(state.getBlock()).getWorldGenProperties();
			
			if ((NameReader.isDynamic(state.getBlock())) && ((!gen_prop.getHasBiomeNameMatcher() && !gen_prop.getHasBiomeTypeMatcher()) || !ConfigFile.biomeSpecificOres))
			{
				runGenerator(WORLDGEN.get(state), world, random, chunkX, chunkZ, gen_prop.getChance(), gen_prop.getMinHeight(), gen_prop.getMaxHeight());
			}
		}
	}		
}