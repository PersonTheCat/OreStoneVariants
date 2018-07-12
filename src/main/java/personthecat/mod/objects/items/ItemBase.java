package personthecat.mod.objects.items;

import net.minecraft.item.Item;
import personthecat.mod.Main;
import personthecat.mod.init.ItemInit;
import personthecat.mod.util.interfaces.IHasModel;

public class ItemBase extends Item implements IHasModel
{

private String name;
	
	public ItemBase(String name)
	{
		setUnlocalizedName(name);
		setRegistryName(name);
		
		this.name = name;
		
		ItemInit.ITEMS.add(this);
	}
	
	@Override
	public void registerModels()
	{
		Main.proxy.registerItemRenderer((this), name);
	}
}
