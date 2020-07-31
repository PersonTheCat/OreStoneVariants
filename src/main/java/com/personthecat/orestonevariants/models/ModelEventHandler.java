package com.personthecat.orestonevariants.models;

import com.google.common.collect.ImmutableMap;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.ZipTools;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.textures.SpriteHandler;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

@EventBusSubscriber
@SuppressWarnings("unused")
public class ModelEventHandler {
    /** Generates all overlays, registers all overlay locations. */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (OreProperties props : Main.ORE_PROPERTIES) {
            final ResourceLocation location = props.texture.overlayLocation;
            event.getMap().registerSprite(location);
            event.getMap().registerSprite(PathTools.ensureDense(location));
        }
    }

    /** Generates and places models for every block. Hopefully still temporary. */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelBake(ModelBakeEvent event) {
        final Map<OreProperties, ModelPair> overlayGetter = getOverlayModels();
        for (BaseOreVariant b : Main.BLOCKS) {
            final IBakedModel bgModel = event.getModelManager().getModel(findModel(event, b.bgBlock));
            final ModelPair overlays = overlayGetter.get(b.properties).onto(bgModel);
            placeVariants(event, b.getDefaultState(), overlays);
        }
    }

    /** Returns a map of all of the overlay models to be used when generating full, dynamic models. */
    private static Map<OreProperties, ModelPair> getOverlayModels() {
        ImmutableMap.Builder<OreProperties, ModelPair> builder = ImmutableMap.builder();
        final SimpleModelBaker baker = new SimpleModelBaker();
        for (OreProperties props : Main.ORE_PROPERTIES) {
            final ResourceLocation location = props.texture.overlayLocation;
            final TextureAtlasSprite normal = getSprite(location);
            final TextureAtlasSprite dense = getSprite(PathTools.ensureDense(location));
            final boolean shade = Cfg.shade(location);
            builder.put(props, new ModelPair(baker.bake(normal, shade), baker.bake(dense, shade)));
        }
        return builder.build();
    }

    /** Statically retrieves the texture for the specified location. */
    private static TextureAtlasSprite getSprite(ResourceLocation location) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
    }

    /** Shorthand for BlockModelShapes#getModelLocation. */
    private static ModelResourceLocation findModel(ModelBakeEvent event, IBlockState state) {
        final ModelResourceLocation location = event
            .getModelManager()
            .getBlockModelShapes()
            .getBlockStateMapper()
            .getVariants(state.getBlock())
            .get(state);
        info("Looking for {}; found: {}", state, location);
        return location;
    }

    /** Places the input model at all of the necessary locations. */
    private static void placeVariants(ModelBakeEvent event, IBlockState primary, ModelPair models) {
        final IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        final BlockStateMapper mapper = event.getModelManager().getBlockModelShapes().getBlockStateMapper();
        mapper.getVariants(primary.getBlock()).forEach((state, location) -> {
            if (state.getValue(BaseOreVariant.DENSE)) {
                registry.putObject(location, models.dense);
            } else {
                registry.putObject(location, models.normal);
            }
        });
        final ResourceLocation normal = primary.getBlock().getRegistryName();
        registry.putObject(mrl(normal, "inventory"), models.normal);
        registry.putObject(mrl(osvLocation("dense_" + normal.getPath()), "inventory"), models.dense);
    }

    /** Generates all possible variants of the input state, excluding dense variants. */
    private static Set<IBlockState> getNormalStates(IBlockState state) {
        final Set<IBlockState> states = new HashSet<>();
        states.add(state.getBlock().getDefaultState());
        state.getProperties().keySet().stream()
            .filter(property -> !property.equals(BaseOreVariant.DENSE))
            .map(property -> getStatesFor(state, property))
            .forEach(states::addAll);
        return states;
    }

    private static <T extends Comparable<T>> Set<IBlockState> getStatesFor(IBlockState state, IProperty<T> property) {
        final Set<IBlockState> states = new HashSet<>();
        states.add(state);
        for (T o : property.getAllowedValues()) {
            states.add(state = state.withProperty(property, o));
        }
        return states;
    }

    /** Registers the mod's resource pack as a default resource pack. */
    @SideOnly(Side.CLIENT)
    public static void enableResourcePack() {
        SpriteHandler.defaultPacks.get().add(new FileResourcePack(ZipTools.RESOURCE_PACK));
        Minecraft.getMinecraft().refreshResources();
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