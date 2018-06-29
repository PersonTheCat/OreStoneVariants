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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import personthecat.mod.Main;
import personthecat.mod.init.BlockInit;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.IMetaName;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class BlockOresEnumerated extends BlockOresBase implements IMetaName, IChooseEnums
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
			Main.proxy.registerVariantRenderer(getItem(), states.getMeta(), this.getRegistryName().getResourcePath() + "_" + states.getName());
		}
	}
}
