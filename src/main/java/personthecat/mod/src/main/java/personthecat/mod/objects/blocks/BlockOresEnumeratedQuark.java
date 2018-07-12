package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.interfaces.IChooseEnums;

public class BlockOresEnumeratedQuark extends BlockOresEnumerated
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
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumeratedQuark(newName);
	}
}