package personthecat.osv.client.texture;

import net.minecraft.core.Vec3i;
import personthecat.osv.preset.data.TextureSettings;

import java.awt.*;
import java.util.Objects;

import static personthecat.osv.client.texture.ImageUtils.EMPTY_PIXEL;
import static personthecat.osv.client.texture.ImageUtils.getDistance;
import static personthecat.osv.client.texture.ImageUtils.getRelativeDistance;
import static personthecat.osv.client.texture.ImageUtils.subtract;

public class ThresholdOverlayGenerator implements OverlayGenerator {

    public static final ThresholdOverlayGenerator INSTANCE = new ThresholdOverlayGenerator();

    @Override
    public Color getOrePixel(final TextureSettings cfg, final OverlayData data, final Color bg, final Color fg) {
        final double threshold = Objects.requireNonNull(cfg.getThreshold());
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
        // Then, compare the difference in colors in the
        // foreground with the average color of the
        // background, focusing especially on the differences
        // per channel.
        final Vec3i diff = subtract(data.bgAvg, fg);
        final double dist = getDistance(diff);
        final double relDist = getRelativeDistance(diff);
        if (dist + relDist * 10.0 > threshold) {
            return fg;
        }
        return EMPTY_PIXEL;
    }
}
