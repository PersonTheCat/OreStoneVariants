package personthecat.osv.client.texture;

import personthecat.osv.util.Reference;

import java.awt.*;

import static personthecat.catlib.exception.Exceptions.resourceEx;
import static personthecat.catlib.util.Shorthand.f;

public class ShadedOverlayModifier implements OverlayModifier {

    private static final String MASK_LOCATION = f("/assets/{}/textures/mask.png", Reference.MOD_ID);
    private static final Color[][] MASK = ImageLoader.loadColors(MASK_LOCATION)
        .orElseThrow(() -> resourceEx("Mask file not in jar: {}", MASK_LOCATION));

    /** The maximum level of opacity used by the shading algorithm. */
    private static final int SHADE_OPACITY = 160;

    /** Opacities above this value will be dropped down to it. */
    private static final int SHADE_CUTOFF = 108;

    public static final ShadedOverlayModifier INSTANCE = new ShadedOverlayModifier();

    private ShadedOverlayModifier() {}

    @Override
    public Color[][] modify(final Color[][] bg, final Color[][] fg, final Color[][] overlay) {
        final Color[][] maskScaled = ImageUtils.scaleWithFrames(MASK, fg.length, fg[0].length);
        final Color[][] bgScaled = ImageUtils.scaleWithFrames(bg, fg.length, fg[0].length);

        // This is old logic that looks like a bug, but it works, and I'm keeping it.
        final Color[][] bgFilled = ImageUtils.solidColor(ImageUtils.getAverage(bg), bgScaled.length, bgScaled[0].length);
        final Color[][] texture = pushAndPull(bgFilled, fg);
        final Color[][] masked = ImageUtils.removeByMask(texture, maskScaled);

        return ImageUtils.overlay(masked, overlay);
    }

    private static Color[][] pushAndPull(final Color[][] bg, final Color[][] fg) {
        final Color[][] colors = new Color[fg.length][fg[0].length];
        for (int x = 0; x < fg.length; x++) {
            for (int y = 0; y < fg[0].length; y++) {
                final int alpha = (int) (SHADE_OPACITY * ImageUtils.getDistance(ImageUtils.subtract(fg[x][y], bg[x][y])));
                if (ImageUtils.isForegroundDarker(bg[x][y], fg[x][y])) {
                    colors[x][y] = new Color(0, 0, 0, limitShade(alpha));
                } else {
                    colors[x][y] = new Color(255, 255, 255, limitShade(alpha));
                }
            }
        }
        return colors;
    }

    private static int limitShade(final int alpha) {
        if (alpha > SHADE_CUTOFF) {
            return SHADE_OPACITY;
        } else if (alpha < 0) {
            return 0;
        }
        return alpha;
    }
}
