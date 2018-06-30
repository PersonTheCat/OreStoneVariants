package personthecat.mod.objects.blocks;

import net.minecraft.block.properties.PropertyEnum;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.handlers.BlockStateGenerator;

public class BlockOresEnumeratedUndergroundBiomes2 extends BlockOresEnumerated
{
	public BlockOresEnumeratedUndergroundBiomes2(String name)
	{
		super(name + "_undergroundbiomes2");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return UNDERGROUNDBIOMES2;
	}
	
	@Override
	public String getDependency()
	{
		return "undergroundbiomes2";
	}
	
	@Override
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresEnumeratedUndergroundBiomes2(newName);
	}
}