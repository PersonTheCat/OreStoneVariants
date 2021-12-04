package personthecat.osv.client.texture;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.Optional;

public class BackgroundSelector {

    public static final ResourceLocation STONE_ID = new ResourceLocation("block/stone");
    public static final ResourceLocation NETHERRACK_ID = new ResourceLocation("block/netherrack");
    public static final ResourceLocation END_STONE_ID = new ResourceLocation("block/end_stone");

    private static final Color[][] STONE = loadRequired(STONE_ID);
    private static final Color[][] NETHERRACK = loadRequired(NETHERRACK_ID);
    private static final Color[][] END_STONE = loadRequired(END_STONE_ID);

    public static ResourceLocation getClosestMatch(final ResourceLocation image) {
        final Optional<Color[][]> load = ImageLoader.loadColors(image);
        if (!load.isPresent()) return STONE_ID;

        return getLowest(
            getSimilarity(STONE_ID, load.get(), STONE),
            getSimilarity(NETHERRACK_ID, load.get(), NETHERRACK),
            getSimilarity(END_STONE_ID, load.get(), END_STONE)
        );
    }

    private static Pair<ResourceLocation, Double> getSimilarity(final ResourceLocation id, final Color[][] a, final Color[][] b) {
        final Color[][] c = ImageUtils.scaleWithFrames(b, a.length, a[0].length);
        return Pair.of(id, ImageUtils.getTotalDistance(a, c));
    }

    @SafeVarargs
    private static ResourceLocation getLowest(final Pair<ResourceLocation, Double>... data) {
        double min = 0.0;
        ResourceLocation id = STONE_ID;
        for (final Pair<ResourceLocation, Double> p : data) {
            if (p.getRight() < min) {
                min = p.getRight();
                id = p.getLeft();
            }
        }
        return id;
    }

    private static Color[][] loadRequired(final ResourceLocation id) {
        return ImageLoader.loadColors(id).orElseThrow(NullPointerException::new);
    }
}
