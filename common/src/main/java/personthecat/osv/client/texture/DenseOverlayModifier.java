package personthecat.osv.client.texture;

import java.awt.*;

import static personthecat.osv.client.texture.ImageUtils.getAverage;

public class DenseOverlayModifier implements OverlayModifier {

    public static final DenseOverlayModifier INSTANCE = new DenseOverlayModifier();

    private DenseOverlayModifier() {}

    @Override
    public Color[][] modify(final Color[][] bg, final Color[][] fg, final Color[][] overlay) {
        final int w = overlay.length;
        final int h = overlay[0].length;
        final int frames = h / w;

        final Color[][] shifted = new Color[w][h];
        for (int f = 0; f < frames; f++) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int overlayY = f * w + y;
                    shifted[x][overlayY] = getAverage(
                        overlay[x][overlayY],
                        fromIndex(overlay, x - 1, overlayY, f),
                        fromIndex(overlay, x + 1, overlayY, f),
                        fromIndex(overlay, x, overlayY - 1, f),
                        fromIndex(overlay, x, overlayY + 1, f)
                    );
                }
            }
        }
        return shifted;
    }

    private static Color fromIndex(Color[][] image, int x, int y , int frame) {
        final int w = image.length;
        return ((x < 0) || (y < frame * w) || (x >= w) || (y >= (frame + 1) * w) || (image[x][y].getAlpha() == 34))
            ? ImageUtils.EMPTY_PIXEL : image[x][y];
    }
}
