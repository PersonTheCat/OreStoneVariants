package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedUndergroundBiomes1 extends BlockOresEnumerated
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
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumeratedUndergroundBiomes1(newName);
	}
}