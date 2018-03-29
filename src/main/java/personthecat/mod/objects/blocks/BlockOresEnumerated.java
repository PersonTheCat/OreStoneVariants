package personthecat.mod.objects.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import personthecat.mod.Main;
import personthecat.mod.init.BlockInit;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.IMetaName;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class BlockOresEnumerated extends BlockOresBase implements IHasModel, IMetaName, IChooseEnums
{	
	public static final Map<Block, List<BlockStateGenerator.State>> STATE_MAP = new HashMap<Block, List<BlockStateGenerator.State>>();
	
	public BlockOresEnumerated(String name)
	{
		super(name, false, true, 0);	
		
		for (State variant : getEnum().getAllowedValues())
		{
			if (variant.getMeta() == 0)
			{			
				setDefaultState(this.blockState.getBaseState().withProperty(getEnum(), variant));
			}
			
			IBlockState state = this.getDefaultState().withProperty(getEnum(), variant);
			BlockInit.BLOCKSTATES.add(state);
			BlockInit.BLOCKSTATE_MAP.put(state, variant);
		}
		
		STATE_MAP.put(this, BlockStateGenerator.State.getStateListForModName(getDependency()));
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return BASE;
	}
	
	@Override
	public String getDependency()
	{
		return "base";
	}
	
	@Override
	public int damageDropped(IBlockState state)
	{
		int fortune = 0;
		Random rand = null;
		
		if (this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this))
		{
			return props.getDropMeta();
		}
		else
		{
			return ((BlockStateGenerator.State)state.getValue(getEnum())).getMeta();
		}
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return ((BlockStateGenerator.State)state.getValue(getEnum())).getMeta();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(getEnum(), BlockStateGenerator.State.byMetadata(getDependency(), meta));
	}
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
	{
		for (BlockStateGenerator.State variant : getEnum().getAllowedValues())
		{
			if (variant.isDependencyMet())
			{
				items.add(new ItemStack(this, 1, variant.getMeta()));
			}
		}
	}
	
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {getEnum()});
	}
	
	@Override
	public String getSpecialName(ItemStack stack)
	{
		State state = null;
		
		try
		{
			state = (State) getEnum().getAllowedValues().toArray()[stack.getItemDamage()];
		}
		catch (IndexOutOfBoundsException e)
		{			
			System.err.println("Ahh, using the old ass-hat WILDCARD_VALUE to look me up, I see. We'll see about that! You get a question mark, instead!");
			
			return "?";
		}
		
		return state.getName();
	}

	@Override
	public void registerModels()
	{
		for (State states : getEnum().getAllowedValues())
		{
			Main.proxy.registerVariantRenderer(Item.getItemFromBlock(this), states.getMeta(), this.getRegistryName().getResourcePath() + "_" + states.getName());
		}
	}
}
