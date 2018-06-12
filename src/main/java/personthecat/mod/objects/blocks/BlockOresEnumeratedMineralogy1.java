package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedMineralogy1 extends BlockOresEnumerated implements IChooseEnums
{
	public BlockOresEnumeratedMineralogy1(String name)
	{
		super(name + "_mineralogy");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return MINERALOGY;
	}
	
	@Override
	public String getDependency()
	{
		return "mineralogy";
	}
}