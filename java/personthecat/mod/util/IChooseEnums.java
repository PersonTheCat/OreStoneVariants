package personthecat.mod.util;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.handlers.BlockStateGenerator;

public interface IChooseEnums
{
 	public static final PropertyEnum<BlockStateGenerator.State> BASE = PropertyEnum.<BlockStateGenerator.State>create("variant", BlockStateGenerator.State.class, BlockStateGenerator.State.getStatesForModName("base"));
	public static final PropertyEnum<BlockStateGenerator.State> QUARK = PropertyEnum.<BlockStateGenerator.State>create("variant", BlockStateGenerator.State.class, BlockStateGenerator.State.getStatesForModName("quark"));	
	
	public default PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return BASE;
	}
	
	public default String getDependency()
	{
		return "base";
	}
}