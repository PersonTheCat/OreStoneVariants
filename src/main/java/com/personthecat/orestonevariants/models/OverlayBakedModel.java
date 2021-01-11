package com.personthecat.orestonevariants.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** Multi-layer model implementation based on Forge's multi-layer baked model. */
public class OverlayBakedModel implements IBakedModel, IForgeBakedModel {
    private final ImmutableMap<TransformType, TransformationMatrix> transforms;
    private final IBakedModel background, overlay;

    public OverlayBakedModel(IBakedModel background, IBakedModel overlay) {
        this.transforms = PerspectiveMapWrapper.getTransforms(background.getItemCameraTransforms());
        this.background = background;
        this.overlay = overlay;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
        final RenderType layer = MinecraftForgeClient.getRenderLayer();
        final RenderType bgLayer = asVariant(state)
            .map(BaseOreVariant::getBgLayer)
            .orElse(RenderType.getSolid());
        if (layer == null || bgLayer == BaseOreVariant.getFgLayer()) {
            return getAllQuads(state, side, rand);
        } else if (layer == bgLayer) {
            return background.getQuads(state, side, rand);
        }
        return overlay.getQuads(state, side, rand);
    }

    private List<BakedQuad> getAllQuads(BlockState state, Direction side, Random rand) {
        return new ImmutableList.Builder<BakedQuad>()
            .addAll(background.getQuads(state, side, rand))
            .addAll(overlay.getQuads(state, side, rand))
            .build();
    }

    private static Optional<BaseOreVariant> asVariant(BlockState state) {
        if (state != null && state.getBlock() instanceof BaseOreVariant) {
            return full((BaseOreVariant) state.getBlock());
        }
        return empty();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return background.isAmbientOcclusion();
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return background.isAmbientOcclusion(state);
    }

    @Override
    public boolean isGui3d() {
        return background.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return background.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return background.getParticleTexture();
    }

    @Override
    public boolean isLayered() {
        return true;
    }

    @Override
    public List<Pair<IBakedModel, RenderType>> getLayerModels(ItemStack itemStack, boolean fabulous) {
        // Render item bg layer as item_entity_translucent_cull.
        // Render item fg layer as entity cutout, not standard cutout. Better lighting.
        return new ImmutableList.Builder<Pair<IBakedModel, RenderType>>()
            .add(Pair.of(background, Atlases.getSolidBlockType()))
            .add(Pair.of(overlay, RenderType.getEntitySmoothCutout(AtlasTexture.LOCATION_BLOCKS_TEXTURE)))
            .build();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return background.getItemCameraTransforms();
    }

    @Override
    public boolean doesHandlePerspectives() {
        return true;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType type, MatrixStack mat) {
        return PerspectiveMapWrapper.handlePerspective(this, transforms, type, mat);
    }

    @Override
    public boolean isSideLit() {
        return background.isSideLit();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}