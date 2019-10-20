package com.personthecat.orestonevariants.models;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.List;
import java.util.Random;

/** Unused: delete me */
public class ItemVariantModel implements IBakedModel {
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final IBakedModel normal;

    public ItemVariantModel(IBakedModel normal) {
        this.transforms = PerspectiveMapWrapper.getTransforms(normal.getItemCameraTransforms());
        this.normal = normal;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
        if (state == null) {
            return normal.getQuads(state, side, rand);
        }
        final ItemStack stack = state.getBlock().getPickBlock(state, null, null, null, null);
        final CompoundNBT tag = stack.getTag();
        return tag == null
            ? normal.getQuads(state, side, rand)
            : findModel(state.getBlock(), tag).getQuads(state, side, rand);
    }

    private static IBakedModel findModel(Block block, CompoundNBT tag) {
        final ModelResourceLocation location = new ModelResourceLocation(block.getRegistryName(), getId(tag));
        return Minecraft.getInstance().getModelManager().getModel(location);
    }

    private static String getId(CompoundNBT tag) {
        final String toString = tag.toString();
        return toString.substring(1, toString.length() - 1)
            .replace(':', '=')
            .replace("\"", "")
            .replace(" ", "");
    }

    @Override
    public boolean isAmbientOcclusion() {
        return normal.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return normal.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return normal.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return normal.getParticleTexture();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return PerspectiveMapWrapper.handlePerspective(this, transforms, cameraTransformType);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}