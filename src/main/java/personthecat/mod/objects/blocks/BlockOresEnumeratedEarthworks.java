package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedEarthworks extends BlockOresEnumerated implements IChooseEnums
{
	public BlockOresEnumeratedEarthworks(String name)
	{
		super(name + "_earthworks");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return EARTHWORKS;
	}
	
	@Override
	public String getDependency()
	{
		return "earthworks";
	}
}