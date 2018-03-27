package personthecat.mod.proxy;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.objects.blocks.BlockOresDynamic;

public class CommonProxy 
{	
	public void registerItemRenderer(Item item, String filename) {}
	public void registerVariantRenderer(Item item, int meta, String filename) {}
}
