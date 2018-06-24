package personthecat.mod.objects.blocks.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import personthecat.mod.advancements.AdvancementMap;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.objects.blocks.BlockOresDynamic;
import personthecat.mod.objects.blocks.BlockOresEnumerated;
import personthecat.mod.util.IMetaName;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.ShortTrans;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class ItemBlockVariants extends ItemBlock
{
	public ItemBlockVariants(Block block)
	{
		super(block);
		
		if (useVariants()) setHasSubtypes(true);
		
		setMaxDamage(0);
		setRegistryName(block.getRegistryName());
	}
	
	private boolean useVariants()
	{
		return getBlock() instanceof BlockOresEnumerated;
	}
	
	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}
	
	@Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
		if (ConfigFile.enableAdvancements)
		{
			AdvancementMap.grantAdvancement(AdvancementMap.getAdvancementFromMap(NameReader.getOre(super.getRegistryName().getResourcePath()), worldIn), entityIn);
		}
    }
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (useVariants()) return getUnlocalizedName() + "_" + ((IMetaName)getBlock()).getSpecialName(stack);
		
		else return super.getUnlocalizedName();
	}
	
	@Override
    public String getItemStackDisplayName(ItemStack stack)
    {
		BlockOresBase block = (BlockOresBase) getBlock();
		
		IBlockState backgroundBlockState = block.getBackgroundBlockState();
		Block backgroundBlock = backgroundBlockState.getBlock();
		ItemStack backgroundStack = new ItemStack(backgroundBlock, 1, backgroundBlock.getMetaFromState(backgroundBlockState));
		
		String bgText = null;
		String oreText = block.getProperties().getLocalizedName();
		
		if (block.isDenseVariant())
		{
			oreText = ShortTrans.formatted("ore_stone_variants.denseKey") + " " + oreText;
		}

		if (useVariants())
		{
			List<State> myStates = BlockOresEnumerated.STATE_MAP.get(block);
			
			try
			{
				bgText = myStates.get(stack.getItemDamage()).getLocalizedName();
			}
			
			catch (IndexOutOfBoundsException e)	{bgText = "?";}
		}
		
		else if (getBlock() instanceof BlockOresDynamic)
		{
			if (ShortTrans.canTranslate(backgroundStack.getUnlocalizedName() + ".name"))
			{
				bgText = ShortTrans.formatted(backgroundStack.getUnlocalizedName() + ".name");				
			}
        }
			
		if (bgText != null) return oreText + " (" + bgText + ")";
		
		return ShortTrans.formatted(getUnlocalizedName() + "_" + backgroundBlock.getRegistryName().getResourcePath() + ".name");
    }
	
}
