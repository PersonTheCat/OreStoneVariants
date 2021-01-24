package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.config.Cfg;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/**
 * Temporary substitute for a custom IModel implementation.
 * ICustomModelLoader is not working, as of this time.
 */
@OnlyIn(Dist.CLIENT)
class SimpleModelBaker {
    private static final FaceBakery faceBakery = new FaceBakery();
    private final Vector3f vecFrom = getVecFrom();
    private final Vector3f vecTo = getVecTo();

    /** Generates a dynamic IBakedModel based on another model. */
    IBakedModel bake(TextureAtlasSprite sprite, boolean shade) {
        final Map<Direction, List<BakedQuad>> quads = new HashMap<>();
        for (Direction d : Direction.values()) {
            final BlockPartFace face = getFace(d);
            final BlockPartRotation partRotation = getRotation(d);
            final ModelRotation rotation = ModelRotation.X0_Y0;
            final ResourceLocation location = osvLocation("dynamic_block");
            final BakedQuad quad = faceBakery.bakeQuad(vecFrom, vecTo, face, sprite, d, rotation, partRotation, shade, location);
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
        return new SimpleBakedModel(new ArrayList<>(), quads, true, true, true, sprite, null, null);
    }

    private static Vector3f getVecFrom() {
        final float min = 0.0f - (float) ((getOverlaySize() - 16.0) / 2.0);
        return new Vector3f(min, min, min);
    }

    private static Vector3f getVecTo() {
        final float max = 16.0f + (float) ((getOverlaySize() - 16.0) / 2.0);
        return new Vector3f(max, max, max);
    }

    private static double getOverlaySize() {
        return Cfg.modelScale.get() * 16.0;
    }
}