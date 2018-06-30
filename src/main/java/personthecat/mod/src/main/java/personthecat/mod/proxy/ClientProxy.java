package personthecat.mod.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class ClientProxy extends CommonProxy 
{
	@Override
	public void createAndRegisterResourcePack()
	{
		ModelEventHandler.createAndRegisterResourcePack();
	}
	
	@Override
	public void setBackgroundModelLocation(BlockOresBase ore, int enumerate)
	{
		ore.setBackgroundModelLocation(ConfigInterpreter.getBackgroundModelLocation(enumerate));
	}
	
	@Override
	public void setBackgroundModelLocation(BlockOresBase ore, State variant)
	{
		ore.setBackgroundModelLocation(variant.getBackgroundModelLocation(), variant.getMeta());
	}
	
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
