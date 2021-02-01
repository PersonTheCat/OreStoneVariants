package com.personthecat.orestonevariants.models;

import com.google.common.collect.ImmutableMap;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.ZipTools;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.textures.SpriteHandler;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.FilePack;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.info;
import static com.personthecat.orestonevariants.util.CommonMethods.mrl;
import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;

public class ModelEventHandler {

    /** Generates all overlays, registers all overlay locations. */
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            SpriteHandler.generateOverlays();
            info("Updating sprites with OSV textures.");
            for (OreProperties props : Main.ORE_PROPERTIES) {
                final ResourceLocation location = props.texture.overlayLocation.get();
                event.addSprite(location);
                if (Cfg.denseOres.get()) {
                    event.addSprite(PathTools.ensureDense(location));
                }
            }
            enableResourcePack();
        }
    }

    /** Generates and places models for every block. Hopefully still temporary. */
    public static void onModelBake(ModelBakeEvent event) {
        info("Placing all variant models via ModelBakeEvent.");
        final Map<OreProperties, ModelPair> overlayGetter = getOverlayModels(event.getModelManager());
        for (BaseOreVariant b : Main.BLOCKS) {
            final IBakedModel bgModel = event.getModelManager().getModel(findModel(b.bgState));
            final ModelPair overlays = overlayGetter.get(b.properties).onto(bgModel);
            placeVariants(event.getModelRegistry(), b.getDefaultState(), overlays);
        }
    }

    /** Returns a map of all of the overlay models to be used when generating full, dynamic models. */
    private static Map<OreProperties, ModelPair> getOverlayModels(ModelManager models) {
        final ImmutableMap.Builder<OreProperties, ModelPair> builder = ImmutableMap.builder();
        final SimpleModelBaker baker = new SimpleModelBaker();
        for (OreProperties props : Main.ORE_PROPERTIES) {
            final ResourceLocation location = props.texture.overlayLocation.get();
            final TextureAtlasSprite normal = getSprite(models, location);
            final TextureAtlasSprite dense = getSprite(models, PathTools.ensureDense(location));
            final boolean shade = Cfg.shade(location);
            builder.put(props, new ModelPair(baker.bake(normal, shade), baker.bake(dense, shade)));
        }
        return builder.build();
    }

    /** Statically retrieves the a texture for the specified location. */
    private static TextureAtlasSprite getSprite(ModelManager models, ResourceLocation location) {
        return models.getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).getSprite(location);
    }

    /** Shorthand for BlockModelShapes#getModelLocation. */
    private static ModelResourceLocation findModel(BlockState state) {
        return BlockModelShapes.getModelLocation(state);
    } 

    /** Places the input model at all of the necessary locations. */
    private static void placeVariants(Map<ResourceLocation, IBakedModel> registry, BlockState primary, ModelPair models) {
        final ResourceLocation location = primary.getBlock().getRegistryName();
        registry.put(mrl(location, "inventory"), models.normal);
        registry.put(mrl(osvLocation("dense_" + location.getPath()), "inventory"), models.dense);
        for (BlockState variant : getNormalStates(primary)) {
            registry.put(findModel(variant), models.normal);
            registry.put(findModel(variant.with(BaseOreVariant.DENSE, true)), models.dense);
        }
    }

    /** Generates all possible variants of the input state, excluding dense variants. */
    private static Set<BlockState> getNormalStates(BlockState state) {
        final Set<BlockState> states = new HashSet<>();
        states.add(state.getBlock().getDefaultState());
        state.getValues().keySet().stream()
            .filter(property -> !property.equals(BaseOreVariant.DENSE))
            .map(property -> getStatesFor(state, property))
            .forEach(states::addAll);
        return states;
    }

    private static <T extends Comparable<T>> Set<BlockState> getStatesFor(BlockState state, Property<T> property) {
        final Set<BlockState> states = new HashSet<>();
        states.add(state);
        for (T o : property.getAllowedValues()) {
            states.add(state = state.with(property, o));
        }
        return states;
    }

    /** Registers the mod's resource pack with ResourceManager. */
    private static void enableResourcePack() {
        info("Enabling the resource pack.");
        final SimpleReloadableResourceManager resourceManager =
            (SimpleReloadableResourceManager) Minecraft.getInstance()
                .getResourceManager();
        synchronized (Minecraft.getInstance().getResourceManager()) {
            resourceManager.addResourcePack(new FilePack(ZipTools.RESOURCE_PACK));
        }
    }

    /** A neater way of passing around multiple baked models. */
    private static class ModelPair {
        private final IBakedModel normal, dense;
        private ModelPair(IBakedModel normal, IBakedModel dense) {
            this.normal = normal;
            this.dense = dense;
        }

        /** Overlays each of the models onto the input background. */
        private ModelPair onto(IBakedModel background) {
            return new ModelPair(
                new OverlayBakedModel(background, normal),
                new OverlayBakedModel(background, dense)
            );
        }
    }
}