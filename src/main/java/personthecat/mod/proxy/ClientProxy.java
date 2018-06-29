package personthecat.mod.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.util.Reference;

public class ClientProxy extends CommonProxy 
{	
	@Override
	public void registerItemRenderer(Item item, String filename)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(new ResourceLocation(Reference.MODID, filename), "inventory"));
	}
	
	@Override
	public void registerVariantRenderer(Item item, int meta, String filename)
	{
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(new ResourceLocation(Reference.MODID, filename), "inventory"));
	}
}
