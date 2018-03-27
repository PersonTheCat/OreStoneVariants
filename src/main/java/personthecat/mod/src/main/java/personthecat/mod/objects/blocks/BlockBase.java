package personthecat.mod.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import personthecat.mod.CreativeTab;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.init.ItemInit;
import personthecat.mod.objects.blocks.item.ItemBlockVariants;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.util.IHasModel;

public class BlockBase extends Block implements IHasModel 
{	
	//This class is honestly unnecessary. Was just trying to reduce the amount of code in the other classes, so I just threw a bunch of random things all inside of the same constructor. 
	public BlockBase(String name, Material material, float hardness, String toolClass, int level, boolean isDynamic, boolean useVariants, int enumerate)
	{
		super(material);
		
		if (isDynamic)
		{
			String fullName = ConfigInterpreter.getFullEnumeratedName(enumerate);
			String[] nameTester = fullName.split("_");
			String realName = null;
			
			if (fullName.contains("_ore")) realName = name.startsWith("lit_") ? "lit_" + fullName : fullName;
			
			else realName = fullName.replaceAll(nameTester[0], name);
						
			setRegistryName(realName);
			setUnlocalizedName(name + "_custom"); //This is mainly a placeholder. See ItemBlockVariants.
		}
		else
		{
			setRegistryName(name);
			setUnlocalizedName(name);
		}
		
		OreProperties properties = OreProperties.propertiesOf(name);
		
		if (hardness == BlockOresBase.getHardness) setHardness(properties.getHardness());
		
		else setHardness(hardness);
		
		if (level == BlockOresBase.getLevel) setHarvestLevel(toolClass, properties.getLevel());
		
		else setHarvestLevel(toolClass, level);
		
		if (!name.contains("lit_"))
		{
			if (name.contains("dense_")) setCreativeTab(CreativeTab.DENSE_VARIANTS);
			
			else if (name.contains("_ore")) setCreativeTab(CreativeTab.ORE_VARIANTS);
		}
		
		ItemInit.ITEMS.add(new ItemBlockVariants(this, isDynamic, useVariants, enumerate));
	}
	
	@Override
	public void registerModels()
	{
		Main.proxy.registerItemRenderer(Item.getItemFromBlock(this), this.getRegistryName().getResourcePath());
	}	
}
