package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.interfaces.IChooseEnums;

public class BlockOresEnumeratedEarthworks extends BlockOresEnumerated
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
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumeratedEarthworks(newName);
	}
}