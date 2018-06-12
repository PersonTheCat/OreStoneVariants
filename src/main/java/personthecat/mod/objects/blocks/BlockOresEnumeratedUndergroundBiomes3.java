package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedUndergroundBiomes3 extends BlockOresEnumerated implements IChooseEnums
{
	public BlockOresEnumeratedUndergroundBiomes3(String name)
	{
		super(name + "_undergroundbiomes3");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return UNDERGROUNDBIOMES3;
	}
	
	@Override
	public String getDependency()
	{
		return "undergroundbiomes3";
	}
}