package personthecat.mod.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import personthecat.mod.objects.blocks.BlockGroup;
import personthecat.mod.objects.blocks.BlockOresBase;

public class CommonProxy 
{
	public void createAndRegisterResourcePack() {}
	
	public void testTextureLocation(String path, String errorKey) {}
	
	public void setBackgroundModels(BlockOresBase ore, BlockGroup blocks) {}
	
	public void registerVariantRenderer(Item item, int meta, String filename) {}
	
	public void registerVariantRenderer(Item item, int meta, ModelResourceLocation mrl) {}
}
