package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.Main;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class TestModelLoader implements ICustomModelLoader {
    @Override
    public boolean accepts(ResourceLocation location) {
        debug("Do I accept {}?", location);
        return location.getNamespace().equals(Main.MODID);
    }

    @Override
    public IUnbakedModel loadModel(ResourceLocation location) {
        debug("loading model for {}?", location);
        return new TestModel(location);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        debug("reloading resources...");
    }
}
