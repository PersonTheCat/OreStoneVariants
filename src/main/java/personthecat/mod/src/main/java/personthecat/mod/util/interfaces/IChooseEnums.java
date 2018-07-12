package personthecat.mod.util.interfaces;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public interface IChooseEnums
{
 	public static final PropertyEnum<State> BASE = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("base"));
	public static final PropertyEnum<State> QUARK = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("quark"));	
	public static final PropertyEnum<State> MINERALOGY = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("mineralogy"));
	public static final PropertyEnum<State> MINERALOGY2 = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("mineralogy2"));
	public static final PropertyEnum<State> UNDERGROUNDBIOMES1 = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("undergroundbiomes1"));
	public static final PropertyEnum<State> UNDERGROUNDBIOMES2 = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("undergroundbiomes2"));
	public static final PropertyEnum<State> UNDERGROUNDBIOMES3 = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("undergroundbiomes3"));
	public static final PropertyEnum<State> EARTHWORKS = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("earthworks"));
	public static final PropertyEnum<State> CHISEL = PropertyEnum.<State>create("variant", State.class, State.getStatesForModName("chisel"));
	
	public default PropertyEnum<State> getEnum()
	{
		return BASE;
	}
	
	public default String getDependency()
	{
		return "base";
	}
}