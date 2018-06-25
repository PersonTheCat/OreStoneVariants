package personthecat.mod.util;

import personthecat.mod.objects.blocks.BlockOresBase;

public interface IChooseConstructors
{
	public default BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresBase(newName);
	}
}
