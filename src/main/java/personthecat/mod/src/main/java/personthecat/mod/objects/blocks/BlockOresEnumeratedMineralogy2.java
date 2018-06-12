package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedMineralogy2 extends BlockOresEnumerated implements IChooseEnums
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
}