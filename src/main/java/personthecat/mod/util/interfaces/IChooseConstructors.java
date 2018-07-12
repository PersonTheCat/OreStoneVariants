package personthecat.mod.util.interfaces;

import personthecat.mod.objects.blocks.BlockOresBase;

public interface IChooseConstructors
{
	public default BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresBase(newName);
	}
}
