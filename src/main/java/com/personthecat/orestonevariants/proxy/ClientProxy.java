package com.personthecat.orestonevariants.proxy;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.models.ModelEventHandler;
import com.personthecat.orestonevariants.textures.SpriteHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerVariantRenderer(Item item, int meta, String filename) {
        final ResourceLocation location = new ResourceLocation(Main.MODID, filename);
        ModelLoader.setCustomModelResourceLocation(item, meta, mrl(location, "inventory"));
    }

    @Override
    public void registerVariantRenderer(Item item, int meta, ModelResourceLocation mrl) {
        ModelLoader.setCustomModelResourceLocation(item, meta, mrl);
    }

    @Override
    public void enableResourcePack() {
        ModelEventHandler.enableResourcePack();
    }

    @Override
    public void generateOverlays() {
        SpriteHandler.generateOverlays();
    }
}
