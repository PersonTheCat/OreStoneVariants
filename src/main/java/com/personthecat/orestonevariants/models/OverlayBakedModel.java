package com.personthecat.orestonevariants.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;

import java.util.List;
import java.util.Random;

/** Multi-layer model implementation based on Forge's multi-layer baked model. */
public class OverlayBakedModel implements IBakedModel, IForgeBakedModel {
    // Still needed?
    private final ImmutableMap<TransformType, TransformationMatrix> transforms;
    private final IBakedModel background, overlay;

    public OverlayBakedModel(IBakedModel background, IBakedModel overlay) {
        this.transforms = PerspectiveMapWrapper.getTransforms(background.getItemCameraTransforms());
        this.background = background;
        this.overlay = overlay;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
        RenderType layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            return getAllQuads(state, side, rand);
        } else if (layer == RenderType.getSolid()) {
            return background.getQuads(state, side, rand);
        }
        return overlay.getQuads(state, side, rand);
    }

    private List<BakedQuad> getAllQuads(BlockState state, Direction side, Random rand) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        builder.addAll(background.getQuads(state, side, rand));
        builder.addAll(overlay.getQuads(state, side, rand));
        return builder.build();
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
        return new ImmutableList.Builder<Pair<IBakedModel, RenderType>>()
            .add(Pair.of(background, RenderType.getSolid()))
            .add(Pair.of(overlay, BaseOreVariant.LAYER))
            .build();
    }

    @Override
    public boolean func_230044_c_() {
        return background.func_230044_c_(); // ?
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return background.getItemCameraTransforms();
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType type, MatrixStack mat) {
        return PerspectiveMapWrapper.handlePerspective(this, transforms, type, mat);
    }

    @Override
    public ItemOverrideList getOverrides() {
        //return ItemOverrideList.EMPTY;
        return background.getOverrides();
    }
}