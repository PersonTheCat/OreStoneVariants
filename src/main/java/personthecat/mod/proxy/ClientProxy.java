package personthecat.mod.proxy;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.RegistryHandler;

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
