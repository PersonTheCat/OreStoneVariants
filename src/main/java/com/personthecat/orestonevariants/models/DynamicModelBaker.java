package com.personthecat.orestonevariants.models;

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
        final BlockFaceUV uv = new BlockFaceUV(new float[] {0, 0, 0, 0}, 0);
        return new BlockPartFace(d, quad.getTintIndex(), "overlay", uv);
    }

    /** Generates a new BlockPartRotation with standard settings for dynamic models. */
    private BlockPartRotation getRotation(Direction d) {
        return new BlockPartRotation(vecFrom, d.getAxis(), 0, false);
    }

    private ItemCameraTransforms getTransforms() {
        // To-do: standard transforms != default.
        return ItemCameraTransforms.DEFAULT;
    }

    /** Variant of SimpleBakedModel#new with fewer parameters. */
    private IBakedModel getSimpleModel(Map<Direction, List<BakedQuad>> quads, IBakedModel original, TextureAtlasSprite sprite) {
        final boolean ao = original.isAmbientOcclusion();
        final boolean gui3d = original.isGui3d();
        final ItemOverrideList overrides = original.getOverrides();
        return new SimpleBakedModel(new ArrayList<>(), quads, ao, gui3d, sprite, getTransforms(), overrides);
    }
}