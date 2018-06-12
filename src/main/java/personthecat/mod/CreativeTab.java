package personthecat.mod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.init.BlockInit;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.BlockStateGenerator;

/*
 * Can't fix dense icon when UB is loaded. I would at least need the all blocks
 * to be registered before looking at this class. Might need to create an 
 * icon-only block.
 */
public class CreativeTab
{
	static ResourceLocation location = Main.isMineralogyLoaded() ? new ResourceLocation(Reference.MODID, "gold_ore_mineralogy") :
	                                   Main.isUndergroundBiomesLoaded() ? Blocks.GOLD_ORE.getRegistryName() : 
	                                   new ResourceLocation(Reference.MODID, "gold_ore");
	
	public static CreativeTabs ORE_VARIANTS = new CreativeTabs("ore_variants")
	{
		@Override
		public ItemStack getTabIconItem()
		{			
			return new ItemStack(Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(location)), 1, BlockStateGenerator.State.ANDESITE.getMeta());
		};
	};
	
	public static CreativeTabs DENSE_VARIANTS = new CreativeTabs("dense_variants")
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Reference.MODID, "dense_" + location.getResourcePath()))), 1, BlockStateGenerator.State.GRANITE.getMeta());
		};
	};
}