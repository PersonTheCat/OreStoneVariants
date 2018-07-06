package personthecat.mod.objects.blockstates;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockStateContainer.StateImplementation;
import net.minecraftforge.common.property.IUnlistedProperty;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.OreProperties;

public class BlockStatesBase extends BlockStateContainer
{
	BlockOresBase ore;
	
	public BlockStatesBase(BlockOresBase ore, IProperty<?>[] iProps)
	{
		super(ore, iProps);
		
		this.ore = ore;
	}

	@Override
    protected StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> iPropsMap, @Nullable ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties)
    {
        return new StateImplementationBase(ore, iPropsMap);
    }
	
	public static class StateImplementationBase extends StateImplementation
	{
		BlockOresBase ore;
		
		protected StateImplementationBase(BlockOresBase ore, ImmutableMap<IProperty<?>, Comparable<?>> iPropsMap)
		{
			super(ore, iPropsMap);
			
			this.ore = ore;
		}
		
		public BlockOresBase getOre()
		{
			return ore;
		}
		
		
	}
}