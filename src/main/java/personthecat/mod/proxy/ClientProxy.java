package personthecat.mod.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import personthecat.mod.objects.blocks.BlockGroup;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.util.CommonMethods;
import personthecat.mod.util.Reference;

import static personthecat.mod.Main.logger;

public class ClientProxy extends CommonProxy
{
    @Override
    public void createAndRegisterResourcePack()
    {
        ModelEventHandler.createAndRegisterResourcePack();
    }

    @Override
    public void testTextureLocation(String path, String errorKey)
    {
        try
        {
            Minecraft.class.getClassLoader().getResourceAsStream(path);
        }
        catch (NullPointerException e)
        {
            throw new RuntimeException("Error: A texture for " + errorKey + " is invalid. Path: " + path + " does not exist.");
        }
    }

    @Override
    public void setBackgroundModels(BlockOresBase ore, BlockGroup blocks)
    {
        ore.setBackgroundModels(blocks);
    }

    @Override
    public void registerVariantRenderer(Item item, int meta, String filename)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(new ResourceLocation(Reference.MODID, filename), "inventory"));
    }

    @Override
    public void registerVariantRenderer(Item item, int meta, ModelResourceLocation mrl)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, mrl);
    }
}
