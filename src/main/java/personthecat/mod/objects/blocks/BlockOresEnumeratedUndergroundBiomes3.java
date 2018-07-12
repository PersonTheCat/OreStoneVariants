package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.interfaces.IChooseEnums;

public class BlockOresEnumeratedUndergroundBiomes3 extends BlockOresEnumerated
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
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumeratedUndergroundBiomes3(newName);
	}
}