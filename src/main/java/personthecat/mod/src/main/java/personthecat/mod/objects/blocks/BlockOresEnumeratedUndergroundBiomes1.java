package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedUndergroundBiomes1 extends BlockOresEnumerated implements IChooseEnums
{
	public BlockOresEnumeratedUndergroundBiomes1(String name)
	{
		super(name + "_undergroundbiomes");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return UNDERGROUNDBIOMES1;
	}
	
	@Override
	public String getDependency()
	{
		return "undergroundbiomes1";
	}
}