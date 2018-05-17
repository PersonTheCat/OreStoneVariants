package personthecat.mod.config;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.util.ShortTrans;

@EventBusSubscriber
public class ConfigInterpreter
{

	public static final List<String> DYNAMIC_BLOCK_ENTRIES = new ArrayList<>();
	
	protected static void loadInterpreter()
	{		
		for (int i = 0; i < ConfigFile.dynamicBlocks.length; i++)
		{
			if (ConfigFile.dynamicBlocks[i].endsWith(":*"))
			{
				String[] stringGetter = ConfigFile.dynamicBlocks[i].replaceAll(":*", "").replace("*", "").split(",");
				Block backgroundBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(stringGetter[1]));
				
				for (IBlockState states : backgroundBlock.getBlockState().getValidStates())
				{
					DYNAMIC_BLOCK_ENTRIES.add(stringGetter[0] + "," + stringGetter[1] + ":" + backgroundBlock.getMetaFromState(states));
				}
			}
			
			else DYNAMIC_BLOCK_ENTRIES.add(ConfigFile.dynamicBlocks[i]);
			
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
	
	protected static void fixOldConfigEntries()
	{
		convertEntryFromStringToArray(ConfigFile.config.get(ConfigFile.ADD_BLOCKS, ShortTrans.unformatted("cfg.dynamicBlocks.adder.add"), "").getString(), ";");
		convertEntryFromStringToArray(ConfigFile.config.get(ConfigFile.MISCELLANEOUS, ShortTrans.unformatted("cfg.blocks.misc.shadeOverrides"), "").getString(), ",");
		convertEntryFromStringToArray(ConfigFile.config.get(ConfigFile.DISABLE_ORES, ShortTrans.unformatted("cfg.blocks.disable.names"), "").getString(), ",");
	}
	
	private static void convertEntryFromStringToArray(String originalText, String character)
	{
		if (!StringUtils.isEmpty(originalText))
		{
			try
			{
				String fullConfigContent = new String(Files.readAllBytes(ConfigFile.config.getConfigFile().toPath()), StandardCharsets.UTF_8);
				
				String newText = originalText.endsWith(character) ? originalText : originalText + character;
					
				newText = newText.replace(" ", "").replaceAll("_0", "").replaceAll(character, System.getProperty("line.separator") + "\t\t");

				FileWriter writer = new FileWriter(ConfigFile.config.getConfigFile());
				
				writer.write(fullConfigContent.replaceAll("=" + originalText, 
						
						" <" + System.getProperty("line.separator") + "\t\t" + newText.substring(0, newText.length() - 1) + ">"));
				
				writer.close();
			}
			
			catch (IOException e) {System.err.println("I broke your config file. Sorry. :(");}
		}
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
