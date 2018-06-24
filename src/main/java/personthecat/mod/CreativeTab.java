package personthecat.mod;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.BlockStateGenerator;


public class CreativeTab
{
	public static CreativeTabs ORE_VARIANTS, DENSE_VARIANTS;
	public static ItemStack ORE_STACK, DENSE_STACK;
	
	static
	{
		ORE_STACK = new ItemStack(Blocks.AIR);
		DENSE_STACK = new ItemStack(Blocks.AIR);
		
		ORE_VARIANTS = new CreativeTabs("ore_variants") 
		{
			@Override 
			public ItemStack getTabIconItem() 
			{
				return ORE_STACK;
			}
		};
		
		if (ConfigFile.denseVariants)
		{
			DENSE_VARIANTS = new CreativeTabs("dense_variants")
			{
				@Override 
				public ItemStack getTabIconItem() 
				{
					return DENSE_STACK;
				}
			};
		}
	}

	public static void postBlockInit()
	{
		if (ConfigFile.disableVanillaVariants())
		{
			setCreativeTabIcons(BlockInit.BLOCKS.get(0));
		}

		//Couldn't use a registry name here if I tried. c:
		else for (BlockOresBase ore : BlockInit.BLOCKS)
		{
			if (ore.getOriginalName().equals("coal_ore"))
			{
				setCreativeTabIcons(ore, 2);
			}
		}
	}
	
	private static void setCreativeTabIcons(BlockOresBase fromBlock)
	{
		setCreativeTabIcons(fromBlock, 0);
	}
	
	private static void setCreativeTabIcons(BlockOresBase fromBlock, int meta)
	{
		ORE_STACK = new ItemStack(fromBlock.getItem(), 1, meta);
		
		if (ConfigFile.denseVariants)
		{
			assert fromBlock.getDenseVariant().getBlock() instanceof BlockOresBase;
			
			BlockOresBase denseOre = (BlockOresBase) fromBlock.getDenseVariant().getBlock();
			
			DENSE_STACK = new ItemStack(denseOre.getItem(), 1, meta);
		}
	}
}