package personthecat.mod;

import static personthecat.mod.Main.logger;
import static personthecat.mod.util.CommonMethods.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import personthecat.mod.config.Cfg;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.OrePropertiesDynamic;
import personthecat.mod.properties.RecipeProperties;

public class FurnaceRecipes
{	
	public static void addRecipes()
	{
		if (Cfg.blocksCat.miscCat.enableOreDictionary)
		{
			for (IBlockState state : BlockInit.BLOCKSTATES)
			{
				if (state.getBlock() instanceof BlockOresBase)
				{
					BlockOresBase asBob = (BlockOresBase) state.getBlock();
					
					if (asBob.getProperties() instanceof OrePropertiesDynamic)
					{
						oreDictDynamic(state);
					}
					else oreDictShort(state);
				}
			}
		}
		
		if (Cfg.blocksCat.miscCat.enableFurnaceRecipes)
		{
			for (BlockOresBase ore : BlockInit.BLOCKS)
			{		
				RecipeProperties property = ore.getProperties().getRecipeProperties();
				
				if (property != RecipeProperties.DO_NOTHING)
				{
					int quantMult = ore.isDenseVariant() ? Cfg.denseCat.generalDenseCat.smeltingMultiplier : 1;
					
					GameRegistry.addSmelting(ore, new ItemStack(property.getResult(), property.getQuantity() * quantMult, property.getResultMeta()), property.getXp());
				}
			}
		}
	}
	
	/**
	 * Gets all existing ore names for the state's original ore.
	 * Maps those names to the new ore.
	 */
	private static void oreDictDynamic(IBlockState state)
	{		
		BlockOresBase bob = (BlockOresBase) state.getBlock(); //Already cast.
		int bobMeta = bob.getMetaFromState(state);
		ItemStack bobStack = new ItemStack(bob, 1, bobMeta);
		
		IBlockState oreState = bob.getProperties().getOreState();
		int oreMeta = oreState.getBlock().getMetaFromState(oreState);
		ItemStack oreStack = new ItemStack(oreState.getBlock(), 1, oreMeta);
		
		for (int id : OreDictionary.getOreIDs(oreStack))
		{
			String oreName = OreDictionary.getOreName(id);
			
			if (bob.isDenseVariant())
			{
				OreDictionary.registerOre("dense" + oreName, bobStack);
				OreDictionary.registerOre(oreName + "Dense", bobStack);
			}
			else OreDictionary.registerOre(oreName, bobStack);
		}
	}
	
	private static void oreDictShort(IBlockState state)
	{	
		if (state.getBlock() instanceof BlockOresBase)
		{
			BlockOresBase ore = (BlockOresBase) state.getBlock();
			int meta = ore.getMetaFromState(state);
			ItemStack oreStack = new ItemStack(ore.getItem(), 1, meta);
			
			List<String> finalNameList = getNameList(ore);
			
			for (String name : finalNameList)
			{
				if (ore.isDenseVariant()) //Move this part into getNameList().
				{
					OreDictionary.registerOre("dense" + name, oreStack);
					OreDictionary.registerOre(name + "Dense", oreStack);
				}
				else OreDictionary.registerOre(name, oreStack);
			}
		}
		
		else logger.warn("Error: Could not cast to BlockOresBase. Can't register ore dictionary support for " + state);
	}
	
	private static List<String> getNameList(BlockOresBase ore)
	{
		String actualName = getActualName(ore.getOriginalName());
		
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