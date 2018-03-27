package personthecat.mod.config;

import java.io.IOException;
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

public static final List<String> DYNAMIC_BLOCK_ENTRIES = new ArrayList<String>();
public static String[] disabledOres = null;
	
	public static void loadInterpreter()
	{
		String s = ConfigFile.dynamicBlocks.replace(" ", "").replaceAll("\n", "").replaceAll("_0", "");	
		
		if (StringUtils.isEmpty(s)) return;
		
		String[] temporaryEntryArray = s.split(";");
		
		for (int i = 0; i < temporaryEntryArray.length; i++)
		{
			if (temporaryEntryArray[i].endsWith(":*"))
			{
				String[] stringGetter = temporaryEntryArray[i].replaceAll(":*", "").replace("*", "").split(",");
				Block backgroundBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(stringGetter[1]));
				
				for (IBlockState states : backgroundBlock.getBlockState().getValidStates())
				{
					DYNAMIC_BLOCK_ENTRIES.add(stringGetter[0] + "," + stringGetter[1] + ":" + backgroundBlock.getMetaFromState(states));
				}
			}
			
			else DYNAMIC_BLOCK_ENTRIES.add(temporaryEntryArray[i]);
			
			try
			{
				IBlockState state = getBackgroundBlockState(i);
				
				if (state == Blocks.AIR.getDefaultState())
				{
					throw new Exception ("Block not found: one or more of your entries may not have been entered correctly.");
				}
			} 
			
			catch (Exception e) {e.printStackTrace();}
		}
	}
	
	public static void loadDisabledOres()
	{
		String s2 = ConfigFile.disableOres.replace(" ", "");
		disabledOres = s2.split(",");
	}
	
	
	public static String getUnenumeratedName(int forNumber)
	{
		String[] s = DYNAMIC_BLOCK_ENTRIES.get(forNumber).split(",");
		
		return s[0];
	}
	
	public static String getFullEnumeratedName(int forNumber)
	{
		String s = getUnenumeratedName(forNumber);
		int i = 0;
		
		try 
		{
			i = getBackgroundBlockState(forNumber).getBlock().getMetaFromState(getBackgroundBlockState(forNumber));
			
			if (getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourceDomain().equals("minecraft"))
			{
				s = s + "_" + getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourcePath();
			}
			
			else s = s + "_" + getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourceDomain() + "_" + getBackgroundBlockState(forNumber).getBlock().getRegistryName().getResourcePath();
		} 
		
		catch (IOException e) {e.printStackTrace();}
		
		if (i > 0) return s + "_" + i;
		
		else return s;		
	}
	
	public static ModelResourceLocation getBackgroundModelLocation(int forNumber)
	{
		BlockStateMapper stateMapper = new BlockStateMapper();
		IBlockState state = null;
		
		try 
		{
			state = getBackgroundBlockState(forNumber);
		} 
		
		catch (IOException e) {e.printStackTrace();}
		
		Map<IBlockState, ModelResourceLocation> locationMapped = stateMapper.getVariants(state.getBlock());
		
		return locationMapped.get(state);
	}
	
	public static IBlockState getBackgroundBlockState(int forNumber) throws IOException
	{
		String[] s = DYNAMIC_BLOCK_ENTRIES.get(forNumber).split(",");
		String[] s1 = s[1].split(":");
		
		ResourceLocation location = null;
		int meta = 0;
		
		if (StringUtils.isNumeric(s1[s1.length - 1]))
		{
			meta = Integer.parseInt(s1[s1.length - 1]);
			location = new ResourceLocation(s[1].replaceAll(":" + s1[s1.length - 1], ""));
		}
		
		else if (s1.length == 1 || s1.length == 2)
		{
			location = new ResourceLocation(s[1]);
		}
		
		else throw new IOException("Syntax error: one or more of the background blocks you entered was not typed correctly.");
		
		return ForgeRegistries.BLOCKS.getValue(location).getStateFromMeta(meta);
	}
	
}
