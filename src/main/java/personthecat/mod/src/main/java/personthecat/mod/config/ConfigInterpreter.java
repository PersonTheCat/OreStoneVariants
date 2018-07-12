package personthecat.mod.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ConfigInterpreter
{
	public static final List<String> DYNAMIC_BLOCK_ENTRIES = new ArrayList<>();
	
	public static void loadInterpreter()
	{		
		for (int i = 0; i < ConfigFile.dynamicBlocks.length; i++)
		{
			String entry = ConfigFile.dynamicBlocks[i].replace(" ", "");
			
			if (entry.endsWith(":*"))
			{
				String[] stringGetter = entry.substring(0, entry.length() - 2).split(",");
				Block backgroundBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(stringGetter[1]));

				for (IBlockState states : backgroundBlock.getBlockState().getValidStates())
				{
					DYNAMIC_BLOCK_ENTRIES.add(stringGetter[0] + "," + stringGetter[1] + ":" + backgroundBlock.getMetaFromState(states));
				}
			}
			
			else DYNAMIC_BLOCK_ENTRIES.add(entry);
			
			printBlocksThatArentFound(i);
		}

	}
	
	private static void printBlocksThatArentFound(int entryNumber)
	{
		try
		{
			IBlockState state = getBackgroundBlockState(entryNumber);
			
			if (state == Blocks.AIR.getDefaultState())
			{
				System.out.println("Entry: \"" + DYNAMIC_BLOCK_ENTRIES.get(entryNumber) + "\" refers to a block that could not be found. Hopefully I'll find it later.");
				//This doesn't necessarily indicate that the block doesn't exist or won't be retrievable later on. It still tries again later.
			}
		} 
		
		catch (Exception e) {/*Not going to fully throw this exception in case the block can be found later (i.e. Quark, usually)*/}
	}
	
	public static String getUnenumeratedName(int forNumber)
	{
		String[] s = DYNAMIC_BLOCK_ENTRIES.get(forNumber).split(",");
		
		return s[0];
	}
	
	public static String getFullEnumeratedName(int forNumber)
	{
		String s = getUnenumeratedName(forNumber);
		int i = getBackgroundBlockState(forNumber).getBlock().getMetaFromState(getBackgroundBlockState(forNumber));
		
		if (getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourceDomain().equals("minecraft"))
		{
			s = s + "_" + getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourcePath();
		}
		
		else s = s + "_" + getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourceDomain() + "_" + getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourcePath();
		
		if (i > 0) return s + "_" + i;
		
		else return s;		
	}
	
	public static String getFullCorrectedEnumeratedName(int enumerate, String fromName)
	{
		String fullName = getFullEnumeratedName(enumerate);
		
		if (!fullName.contains("_ore"))
		{
			String[] nameTester = fullName.split("_");
			
			fullName = fullName.replaceAll(nameTester[0], fromName);
		}
		
		//Hax. Do better than this.
		if (fromName.startsWith("dense_") && !fullName.startsWith("dense"))
		{
			fullName = "dense_" + fullName;
		}
		
		return fullName;
	}
	
	public static ModelResourceLocation getBackgroundModelLocation(int forNumber)
	{
		BlockStateMapper stateMapper = new BlockStateMapper();
		IBlockState state = null;

		state = getBackgroundBlockState(forNumber);
		
		Map<IBlockState, ModelResourceLocation> locationMapped = stateMapper.getVariants(state.getBlock());
		
		return locationMapped.get(state);
	}
	
	public static IBlockState getBackgroundBlockState(int forNumber)
	{
		String[] s = DYNAMIC_BLOCK_ENTRIES.get(forNumber).split(",");

		return getBlockStateFromString(s[1]);
	}
	
	public static IBlockState getBlockStateFromString(String input)
	{
		String[] split = input.split(":");
		
		ResourceLocation location = null;
		int meta = 0;
		
		if (StringUtils.isNumeric(split[split.length - 1]))
		{
			meta = Integer.parseInt(split[split.length - 1]);
			location = new ResourceLocation(input.replaceAll(":" + split[split.length - 1], ""));
		}
		
		else if (split.length == 1 || split.length == 2)
		{
			location = new ResourceLocation(input);
		}
		
		else System.err.println("Syntax error: Could not determine blockstate from " + input);
		
		return ForgeRegistries.BLOCKS.getValue(location).getStateFromMeta(meta);
	}	
}
