package personthecat.mod.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class CommonProxy 
{
	public void createAndRegisterResourcePack() {}
	
	public void testTextureLocation(String path, String errorKey) {}
	
	public void setBackgroundModelLocation(BlockOresBase ore, int enumerate) {}
	public void setBackgroundModelLocation(BlockOresBase ore, State variant) {}
	
	public void registerItemRenderer(Item item, String filename) {}
	public void registerVariantRenderer(Item item, int meta, String filename) {}
}
