package personthecat.mod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.DefaultProperties.DefaultRecipeProperties;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.RecipeProperties;

public class FurnaceRecipes
{	
	private static void oreDictShort(IBlockState state)
	{		
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		String oreName = block.getRegistryName().getResourcePath();
		String[] nameSplit = oreName.split("_");
		String actualName = null;
		
		//Should get the part of the name that comes before "ore."		
		for (int i = 1; i < 10; i++)
		{
			if (nameSplit[nameSplit.length - i].equals("ore"))
			{
				actualName = nameSplit[nameSplit.length - (i + 1)];
				
				break;
			}
		}
		
		//Capitalize the name to derive the proper ore dictionary name. 
		actualName = actualName.substring(0, 1).toUpperCase() + actualName.substring(1);
		
		List<String> finalNameList = new ArrayList<String>();
		
		if (actualName.startsWith("Adamant"))
		{
			finalNameList.add("oreAdamantium");
			finalNameList.add("oreAdamantine");
			finalNameList.add("oreAdamantite");
		}
		
		else if (actualName.startsWith("M") && actualName.contains("thr"))
		{
			finalNameList.add("oreMythril");
			finalNameList.add("oreMithril");
		}
		
		else finalNameList.add("ore" + actualName);
		
		for (String name : finalNameList)
		{
			if (oreName.contains("dense"))
			{
				OreDictionary.registerOre("dense" + name, new ItemStack(block, 1, meta));
				OreDictionary.registerOre(name + "Dense", new ItemStack(block, 1, meta));
			}
			
			else OreDictionary.registerOre(name, new ItemStack(block, 1, meta));
		}
	}
	
	public static void addRecipes()
	{
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			oreDictShort(state);
		}
		
		for (Block ore : BlockInit.BLOCKS)
		{		
			RecipeProperties property = OreProperties.propertiesOf(ore.getRegistryName().getResourcePath()).getRecipeProperties();
			
			if (property != RecipeProperties.DO_NOTHING)
			{			
				GameRegistry.addSmelting(ore, new ItemStack(property.getResult(), property.getQuantity(), property.getResultMeta()), property.getXp());
			}
		}
	}
}
