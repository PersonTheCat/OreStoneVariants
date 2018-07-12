package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.interfaces.IChooseEnums;

public class BlockOresEnumeratedChisel extends BlockOresEnumerated
{
	public BlockOresEnumeratedChisel(String name)
	{
		super(name + "_chisel");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return CHISEL;
	}
	
	@Override
	public String getDependency()
	{
		return "chisel";
	}
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumeratedChisel(newName);
	}
}