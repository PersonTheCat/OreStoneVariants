package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import com.personthecat.orestonevariants.util.unsafe.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** To-do: Completely remove the need to deserialize a model JSON. */
public class TestModel implements IUnbakedModel {
    private final ResourceLocation location;

    private static final Lazy<BlockModel> DYNAMIC_MODEL = new Lazy<>(TestModel::getDynamicModel);

    public TestModel(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format) {
        debug("Attempting to bake the model for {}.", location);
        return null;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
        final ResourceLocation dense = osvLocation(PathTools.ensureDense(location.getPath()));
        final ResourceLocation normal = shouldShade(location)
            ? osvLocation(PathTools.ensureShaded(location.getPath()))
            : location; // This should already not be shaded.
        return Arrays.asList(dense, normal);
    }

    private static boolean shouldShade(ResourceLocation location) {
        final boolean global = Cfg.shadeOverlays.get();
        return global != Cfg.shadeOverrides.get().contains(location.getPath());
    }

    private static BlockModel getDynamicModel() {
        return getUnbaked(osvLocation("models/blocks/dymamic_block.json"));
    }

    private static BlockModel getUnbaked(ResourceLocation location) {
        final IResourceManager rm = Minecraft.getInstance().getResourceManager();
        return Result.with(() -> rm.getResource(location), res -> {
            return Result.with(() -> new InputStreamReader(res.getInputStream()), reader -> {
                final BlockModel model = BlockModel.deserialize(reader);
                model.name = location.toString();
                return model;
            }).expect("Error parsing InputStream of block model.");
        }).expect("Error retrieving model from the jar.");
    }
}
