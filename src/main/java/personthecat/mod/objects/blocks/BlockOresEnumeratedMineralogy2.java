package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.interfaces.IChooseEnums;

public class BlockOresEnumeratedMineralogy2 extends BlockOresEnumerated
{
	public BlockOresEnumeratedMineralogy2(String name)
	{
		super(name + "_mineralogy2");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return MINERALOGY2;
	}
	
	@Override
	public String getDependency()
	{
		return "mineralogy2";
	}
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumeratedMineralogy2(newName);
	}
}