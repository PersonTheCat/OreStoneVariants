package personthecat.mod.objects.blocks.item;

import java.io.IOException;
import java.util.List;

import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import personthecat.mod.advancements.DynamicTrigger;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.objects.blocks.BlockOresEnumerated;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.util.IMetaName;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class ItemBlockVariants extends ItemBlock
{
	private boolean isDynamic, useVariants;
	private static boolean advancementsMapped;
	private int enumerate;
	private String name;
	
	public ItemBlockVariants(Block block, boolean isDynamic, boolean useVariants, int enumerate)
	{
		super(block);
		
		if (useVariants) setHasSubtypes(true);
		
		setMaxDamage(0);
		setRegistryName(block.getRegistryName());

		this.name = super.getRegistryName().getResourcePath();
		this.useVariants = useVariants;
		this.enumerate = enumerate;
		this.isDynamic = isDynamic;
	}
	
	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}
	
	@Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {				
		Advancement advancement = DynamicTrigger.ADVANCEMENT_MAP.get(NameReader.getOre(name));
		
		if (advancement != null && entityIn instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP) entityIn;
			
			if (!player.getAdvancements().getProgress(advancement).isDone())
			{
				for (String criteria : player.getAdvancements().getProgress(advancement).getRemaningCriteria())
				{
					player.getAdvancements().grantCriterion(advancement, criteria);
				}
			}
		}
    }
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (useVariants) return super.getUnlocalizedName() + "_" + ((IMetaName)this.block).getSpecialName(stack);
		
		else return super.getUnlocalizedName();
	}
	
	//Needs to be updated - can be simplified some.
	@Override
    public String getItemStackDisplayName(ItemStack stack)
    {
    	String nameText = I18n.translateToLocal(this.getUnlocalizedName() + ".name");
		String oreText = OreProperties.propertiesOf(super.getRegistryName().getResourcePath()).getLocalizedName();
		if (NameReader.isDense(name)) oreText = I18n.translateToLocal("ore_stone_variants.denseKey") + " " + oreText;
		String bgText = null;
		
		if (useVariants)
		{
			List<BlockStateGenerator.State> myStates = BlockOresEnumerated.STATE_MAP.get(super.getBlock());
			
			try
			{
				bgText = myStates.get(stack.getItemDamage()).getLocalizedName();
			}
			
			catch (IndexOutOfBoundsException e)
			{
				bgText = "?";
			}
			
			nameText = oreText + " (" + bgText + ")";
		}
		
		if (isDynamic)
        {
			try 
			{
				IBlockState backgroundBlock = ConfigInterpreter.getBackgroundBlockState(enumerate);
				bgText = I18n.translateToLocal(backgroundBlock.getBlock().getUnlocalizedName() + ".name");
		    	
				if (I18n.canTranslate(backgroundBlock.getBlock().getUnlocalizedName() + ".name"))
				{
		    		nameText = oreText + " (" + bgText + ")";
				}
				
				//This effectively becomes the new unlocalized name.
				else nameText = I18n.translateToLocal(this.getUnlocalizedName().replaceAll("_custom", "") + "_" + backgroundBlock.getBlock().getRegistryName().getResourcePath() + ".name");
			} 
			
			catch (IOException e) {e.printStackTrace();}
        }
			
		return nameText;
    }
	
}
