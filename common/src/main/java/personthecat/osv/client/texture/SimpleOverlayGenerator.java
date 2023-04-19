package personthecat.osv.client.texture;

import net.minecraft.core.Vec3i;
import personthecat.osv.preset.data.TextureSettings;

import java.awt.*;

import static personthecat.osv.client.texture.ImageUtils.EMPTY_PIXEL;
import static personthecat.osv.client.texture.ImageUtils.darken;
import static personthecat.osv.client.texture.ImageUtils.getDistance;
import static personthecat.osv.client.texture.ImageUtils.getRelativeDistance;
import static personthecat.osv.client.texture.ImageUtils.subtract;

public class SimpleOverlayGenerator implements OverlayGenerator {

    public static final SimpleOverlayGenerator INSTANCE = new SimpleOverlayGenerator();

    @Override
    public Color getOrePixel(final TextureSettings cfg, final OverlayData data, final Color bg, final Color fg) {
        // First, check to remove any pixels that are almost
        // the same in both images, keeping any that are
        // clearly very different.
        final Vec3i stdDiff = subtract(bg, fg);
        final double stdDist = getDistance(stdDiff);
        if (stdDist > 0.7 * data.maxDist) {
            return fg;
        } else if (stdDist < 0.1 * data.maxDist) {
            return EMPTY_PIXEL;
        }
        // Next, filter out any pixels that are specifically
        // darker versions of the background image.
        final Color darkened = darken(bg, 45);
        final Vec3i darkDiff = subtract(darkened, fg);
        final double darkDist = getDistance(darkDiff);
        if (darkDist < 0.125 * (data.maxRel + 0.001 / data.bgDist + 0.001)) {
            return EMPTY_PIXEL;
        }
        // Then, compare the difference in colors in the
        // foreground with the average color of the
        // background, focusing especially on the differences
        // per channel.
        final Vec3i diff = subtract(data.bgAvg, fg);
        final double dist = getDistance(diff);
        final double relDist = getRelativeDistance(diff);
        // Colorful backgrounds are consistently more difficult
        // to extract, while still having enough flexibility
        // that a single value can be a blanket fix.
        final double threshold = data.bgDist > 0.05 ? 1.2 : 0.2;
        if (dist + relDist * 10.0 > threshold) {
            return fg;
        }
        return EMPTY_PIXEL;
    }
}
