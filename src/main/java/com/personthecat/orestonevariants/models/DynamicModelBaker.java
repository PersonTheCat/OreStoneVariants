package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/**
 * Temporary substitute for a custom IModel implementation.
 * ICustomModelLoader is not working, as of this time.
 */
@OnlyIn(Dist.CLIENT)
public class DynamicModelBaker {
    private static final FaceBakery faceBakery = new FaceBakery();
    private static final Vector3f vecFrom = new Vector3f(0, 0, 0);
    private static final Vector3f vecTo = new Vector3f(16, 16, 16);

    /** The standard transforms used by most blocks. */
    private static final Lazy<ItemCameraTransforms> standardTransforms = new Lazy<>(DynamicModelBaker::getStandardTransforms);

    /** Generates a dynamic IBakedModel based on another model. */
    public IBakedModel bake(IBakedModel original, BlockState from, TextureAtlasSprite sprite, boolean shade) {
        final Map<Direction, List<BakedQuad>> quads = cloneQuads(original, from);
        for (Direction d : Direction.values()) {
            final BlockPartFace face = getFace(d, getBottomQuad(quads, d));
            final BlockPartRotation partRotation = getRotation(d);
            final ModelRotation rotation = ModelRotation.X0_Y0;
            final BakedQuad quad = faceBakery.makeBakedQuad(vecFrom, vecTo, face, sprite, d, rotation, partRotation, shade);
            quads.get(d).add(quad);
        }
        return getSimpleModel(quads, original, sprite);
    }

    /** Clones all of the quads from the input IBakedModel. */
    private Map<Direction, List<BakedQuad>> cloneQuads(IBakedModel original, BlockState from) {
        Map<Direction, List<BakedQuad>> newQuads = new HashMap<>();
        for (Direction d : Direction.values()) {
            final List<BakedQuad> originalQuads = original.getQuads(from, d, new Random());
            newQuads.put(d, cloneList((ArrayList<BakedQuad>) originalQuads));
        }
        return newQuads;
    }

    /** For syntactic clarity. Retrieves the quad at the bottom layer of the list. */
    private BakedQuad getBottomQuad(Map<Direction, List<BakedQuad>> quads, Direction d) {
        return quads.get(d).get(0);
    }

    /** Generates a new BlockPartFace using standard UV settings. */
    private BlockPartFace getFace(Direction d, BakedQuad quad) {
        final BlockFaceUV uv = new BlockFaceUV(new float[] {0, 0, 16, 16}, 0);
        return new BlockPartFace(d, quad.getTintIndex(), "overlay", uv);
    }

    /** Generates a new BlockPartRotation with standard settings for dynamic models. */
    private BlockPartRotation getRotation(Direction d) {
        return new BlockPartRotation(vecFrom, d.getAxis(), 0, false);
    }

    private static ItemCameraTransforms getStandardTransforms() {
        return new ItemCameraTransforms(
            transform(vec(75, 225, 0), vec(0, 2.5F, 0), vec(0.375F, 0.375F, 0.375F)), // 3rd left?
            transform(vec(75, 45, 0), vec(0, 2.5F, 0), vec(0.375F, 0.375F, 0.375F)), // 3rd right
            transform(vec(0, 255, 0), vec(0, 0, 0), vec(0.4F, 0.4F, 0.4F)), // 1st left
            transform(vec(0, 45, 0), vec(0, 0, 0), vec(0.4F, 0.4F, 0.4F)), // 1st right
            transform(vec(0, 255, 0), vec(0, 0, 0), vec(0.4F, 0.4F, 0.4F)), // head?
            transform(vec(30, 225, 0), vec(0, 0, 0), vec(0.625F, 0.625F, 0.625F)), // gui
            transform(vec(0, 0, 0), vec(0, 3, 0), vec(0.25F, 0.25F, 0.25F)), // ground
            transform(vec(0, 0, 0), vec(0, 0, 0), vec(0.5F, 0.5F, 0.5F)) // fixed
        );
    }

    private static ItemTransformVec3f transform(Vector3f rot, Vector3f trans, Vector3f scale) {
        return new ItemTransformVec3f(rot, trans, scale);
    }

    private static Vector3f vec(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    /** Variant of SimpleBakedModel#new with fewer parameters. */
    private IBakedModel getSimpleModel(Map<Direction, List<BakedQuad>> quads, IBakedModel original, TextureAtlasSprite sprite) {
        final boolean ao = original.isAmbientOcclusion();
        final boolean gui3d = original.isGui3d();
        final ItemOverrideList overrides = original.getOverrides();
        return new SimpleBakedModel(new ArrayList<>(), quads, ao, gui3d, sprite, standardTransforms.get(), overrides);
    }
}