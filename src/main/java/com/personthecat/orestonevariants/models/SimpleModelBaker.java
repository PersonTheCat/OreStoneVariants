package com.personthecat.orestonevariants.models;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

/**
 * Temporary substitute for a custom IModel implementation.
 * ICustomModelLoader is not working, as of this time.
 */
@OnlyIn(Dist.CLIENT)
public class SimpleModelBaker {
    private static final FaceBakery faceBakery = new FaceBakery();
    private static final Vector3f vecFrom = new Vector3f(-0.05F, -0.05F, -0.05F);
    private static final Vector3f vecTo = new Vector3f(16.05F, 16.05F, 16.05F);

    /** Generates a dynamic IBakedModel based on another model. */
    public IBakedModel bake(TextureAtlasSprite sprite, boolean shade) {
        final Map<Direction, List<BakedQuad>> quads = new HashMap<>();
        for (Direction d : Direction.values()) {
            final BlockPartFace face = getFace(d);
            final BlockPartRotation partRotation = getRotation(d);
            final ModelRotation rotation = ModelRotation.X0_Y0;
            final BakedQuad quad = faceBakery.makeBakedQuad(vecFrom, vecTo, face, sprite, d, rotation, partRotation, shade);
            quads.put(d, Collections.singletonList(quad));
        }
        return getSimpleModel(quads, sprite);
    }

    /** Generates a new BlockPartFace using standard UV settings. */
    private BlockPartFace getFace(Direction d) {
        final BlockFaceUV uv = new BlockFaceUV(new float[] {0, 0, 16, 16}, 0);
        return new BlockPartFace(d, 0, "normal", uv);
    }

    /** Generates a new BlockPartRotation with standard settings for dynamic models. */
    private BlockPartRotation getRotation(Direction d) {
        return new BlockPartRotation(vecFrom, d.getAxis(), 0, false);
    }

    /** Variant of SimpleBakedModel#new which ignores some unused parameters. */
    private IBakedModel getSimpleModel(Map<Direction, List<BakedQuad>> quads, TextureAtlasSprite sprite) {
        return new SimpleBakedModel(new ArrayList<>(), quads, true, true, sprite, null, null);
    }
}