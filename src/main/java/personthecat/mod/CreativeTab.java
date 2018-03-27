package personthecat.mod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.init.BlockInit;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class CreativeTab
{
	public static CreativeTabs ORE_VARIANTS = new CreativeTabs("ore_variants")
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ore_stone_variants:gold_ore"))), 1, BlockStateGenerator.State.ANDESITE.getMeta());
		};
	};
	
	public static CreativeTabs DENSE_VARIANTS = new CreativeTabs("dense_variants")
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ore_stone_variants:dense_diamond_ore"))), 1, BlockStateGenerator.State.GRANITE.getMeta());
		};
	};
}