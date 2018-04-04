package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedQuark extends BlockOresEnumerated implements IChooseEnums
{
	public BlockOresEnumeratedQuark(String name)
	{
		super(name + "_quark");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return QUARK;
	}
	
	@Override
	public String getDependency()
	{
		return "quark";
	}
}