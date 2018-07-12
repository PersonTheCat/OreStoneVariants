package personthecat.mod.objects.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import personthecat.mod.Main;
import personthecat.mod.init.BlockInit;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.handlers.BlockStateGenerator.State;
import personthecat.mod.util.interfaces.IChooseEnums;

public class BlockOresEnumerated extends BlockOresBase implements IChooseEnums
{	
	public static final Map<Block, List<State>> STATE_MAP = new HashMap<>();
	
	public BlockOresEnumerated(String name)
	{
		super(name);	

		this.bgBlockStates = new IBlockState[getEnum().getAllowedValues().size()];
		this.bgModelLocations = new ModelResourceLocation[getEnum().getAllowedValues().size()];
		
		for (State variant : getEnum().getAllowedValues())
		{
			if (variant.getMeta() == 0)
			{			
				setDefaultState(this.blockState.getBaseState().withProperty(getEnum(), variant));
			}
			
			IBlockState state = this.getDefaultState().withProperty(getEnum(), variant);
			BlockInit.BLOCKSTATE_STATE_MAP.put(state, variant);

			setBackgroundBlockState(variant.getBackgroundBlockState(), variant.getMeta());
			
			Main.proxy.setBackgroundModelLocation(this, variant);
		}
		
		STATE_MAP.put(this, BlockStateGenerator.State.getStateListForModName(getDependency()));
	}
	
	@Override
	public PropertyEnum<State> getEnum()
	{
		return BASE;
	}
	
	@Override
	public String getDependency()
	{
		return "base";
	}
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumerated(newName);
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return ((State) state.getValue(getEnum())).getMeta();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(getEnum(), BlockStateGenerator.State.byMetadata(getDependency(), meta));
	}
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
	{
		for (State variant : getEnum().getAllowedValues())
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
	public void registerModels()
	{
		for (State states : getEnum().getAllowedValues())
		{
			Main.proxy.registerVariantRenderer(getItem(), states.getMeta(), getOriginalName() + "_" + states.getName());
		}
	}
}
