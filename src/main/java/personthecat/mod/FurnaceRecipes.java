package personthecat.mod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.RecipeProperties;
import personthecat.mod.util.NameReader;

public class FurnaceRecipes
{	
	public static void addRecipes()
	{
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			oreDictShort(state);
		}
		
		for (BlockOresBase ore : BlockInit.BLOCKS)
		{		
			RecipeProperties property = OreProperties.propertiesOf(ore.getOriginalName()).getRecipeProperties();
			
			if (property != RecipeProperties.DO_NOTHING)
			{
				GameRegistry.addSmelting(ore, new ItemStack(property.getResult(), property.getQuantity(), property.getResultMeta()), property.getXp());
			}
		}
	}
	
	private static void oreDictShort(IBlockState state)
	{	
		if (state.getBlock() instanceof BlockOresBase)
		{
			BlockOresBase ore = (BlockOresBase) state.getBlock();
			int meta = ore.getMetaFromState(state);
			
			List<String> finalNameList = getNameList(ore);
			
			for (String name : finalNameList)
			{
				if (ore.isDenseVariant()) //Move this part into getNameList().
				{
					OreDictionary.registerOre("dense" + name, new ItemStack(ore.getItem(), 1, meta));
					OreDictionary.registerOre(name + "Dense", new ItemStack(ore.getItem(), 1, meta));
				}
				
				else OreDictionary.registerOre(name, new ItemStack(ore.getItem(), 1, meta));
			}
		}
		
		else System.out.println("Error: Could not cast to BlockOresBase. Can't register ore dictionary support for " + state);
	}
	
	private static List<String> getNameList(BlockOresBase ore)
	{
		String actualName = NameReader.getActualName(ore.getOriginalName());
		
		//Capitalize the name to derive the proper ore dictionary name. 
		actualName = actualName.substring(0, 1).toUpperCase() + actualName.substring(1);
		
		List<String> nameList = new ArrayList<>();
		List<String> oreKeys = getOreKeys(ore);
		
		for (String oreKey : oreKeys)
		{
			if (actualName.startsWith("Adamant"))
			{
				nameList.add(oreKey + "Adamantium");
				nameList.add(oreKey + "Adamantine");
				nameList.add(oreKey + "Adamantite");
			}
			
			else if (actualName.startsWith("M") && actualName.contains("thr"))
			{
				nameList.add(oreKey + "Mythril");
				nameList.add(oreKey + "Mithril");
			}
			
			else if (actualName.equals("Aluminumbrass"))
			{
				nameList.add(oreKey + "AluminumBrass");
				nameList.add(oreKey + "AluminiumBrass");
				nameList.add(oreKey + "Aluminumbrass");
				nameList.add(oreKey + "Aluminiumbrass");
				nameList.add(oreKey + "AluBrass");
				nameList.add(oreKey + "Alubrass");
			}
			
			else if (actualName.equals("Aluminum"))
			{
				nameList.add(oreKey + "Aluminum");
				nameList.add(oreKey + "Aluminium");
				nameList.add(oreKey + "Bauxite");
			}
			
			else if (actualName.equals("Chromium"))
			{
				nameList.add(oreKey + "Chromium");
				nameList.add(oreKey + "Chrome");
			}
			
			else if (actualName.equals("Galvanizedsteel"))
			{
				nameList.add(oreKey + "GalvanizedSteel");
				nameList.add(oreKey + "Galvanizedsteel");
			}
			
			else if (actualName.equals("Stainlesssteel"))
			{
				nameList.add(oreKey + "StainlessSteel");
				nameList.add(oreKey + "Stainlesssteel");
			}
			
			else if (actualName.equals("Tungsten"))
			{
				nameList.add(oreKey + "Tungsten");
				nameList.add(oreKey + "Wolfram");
			}
			
			else nameList.add(oreKey + actualName);
		}
		
		return nameList;
	}
	
	private static List<String> getOreKeys(BlockOresBase ore)
	{
		List<String> oreKeys = new ArrayList<>();
		
		oreKeys.add("ore");
		
		if (ore.getBackgroundBlockState().getBlock().equals(Blocks.NETHERRACK))
		{
			oreKeys.add("oreNether");
		}
		
		if (ore.getBackgroundBlockState().getBlock().equals(Blocks.END_STONE))
		{
			oreKeys.add("oreEnd");
		}
		
		return oreKeys;
	}
}