package com.personthecat.orestonevariants.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.List;

/** Multi-layer model implementation based on Forge's multi-layer baked model. */
public class OverlayBakedModel implements IBakedModel {
    private final ImmutableMap<TransformType, TRSRTransformation> transforms;
    private final IBakedModel background, overlay;

    public OverlayBakedModel(IBakedModel background, IBakedModel overlay) {
        this.transforms = PerspectiveMapWrapper.getTransforms(background.getItemCameraTransforms());
        this.background = background;
        this.overlay = overlay;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        final BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            return getAllQuads(state, side, rand);
        } else if (layer == BlockRenderLayer.SOLID) {
            return background.getQuads(state, side, rand);
        }
        return overlay.getQuads(state, side, rand);
    }

    private List<BakedQuad> getAllQuads(IBlockState state, EnumFacing side, long rand) {
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
    public boolean isAmbientOcclusion(IBlockState state) {
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
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        return PerspectiveMapWrapper.handlePerspective(this, transforms, type);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}