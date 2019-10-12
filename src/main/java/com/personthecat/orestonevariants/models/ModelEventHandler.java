package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.textures.SpriteHandler;
import com.personthecat.orestonevariants.util.PathTools;
import com.personthecat.orestonevariants.util.ZipTools;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.FilePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.Map;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class ModelEventHandler {
    /** Generates all overlays, registers all overlay locations. */
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        SpriteHandler.generateOverlays();
        for (OreProperties props : Main.ORE_PROPERTIES) {
            final ResourceLocation location = props.getOverlayResourceLocation();
            event.addSprite(location);
            if (Cfg.denseOres.get()) {
                event.addSprite(PathTools.ensureDense(location));
            }
        }
        enableResourcePack();
    }

    /** Generates and places models for every block. Hopefully still temporary. */
    public static void onModelBake(ModelBakeEvent event) {
        info("Placing all models via ModelBakeEvent until ICustomModelLoaders get updated.");
        final DynamicModelBaker baker = new DynamicModelBaker();
        for (BaseOreVariant b : Main.BLOCKS) {
            final TextureAtlasSprite sprite = getSprite(b.properties.getOverlayResourceLocation());
            final ResourceLocation oreLocation = b.getRegistryName();
            final boolean shade = Cfg.shade(oreLocation);
            final IBakedModel bgModel = event.getModelManager().getModel(findModel(b.bgBlock));
            final IBakedModel oreModel = baker.bake(bgModel, b.bgBlock, sprite, shade);
            placeVariants(event.getModelRegistry(), oreLocation, oreModel);
            // To-do: include this in placeVariants().
            event.getModelRegistry().put(findModel(b.getDefaultState()), oreModel);
        }
    }

    /** Statically retrieves the a texture for the specified location. */
    private static TextureAtlasSprite getSprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureMap().getSprite(location);
    }

    /** Shorthand for BlockModelShapes#getModelLocation. */
    private static ModelResourceLocation findModel(BlockState state) {
        return BlockModelShapes.getModelLocation(state);
    } 

    /** Places the input model at all of the necessary locations. */
    private static void placeVariants(Map<ResourceLocation, IBakedModel> registry, ResourceLocation primary, IBakedModel model) {
        registry.put(mrl(primary, "inventory"), model);
    }

    /** Registers the mod's resource pack with ResourceManager. */
    public static void enableResourcePack() {
        Minecraft.getInstance().getResourceManager().addResourcePack(new FilePack(ZipTools.RESOURCE_PACK));
    }
}