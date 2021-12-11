package personthecat.osv.client.texture;

import java.awt.*;

public class DenseOverlayModifier implements OverlayModifier {

    public static final DenseOverlayModifier INSTANCE = new DenseOverlayModifier();

    private DenseOverlayModifier() {}

    @Override
    public Color[][] modify(final Color[][] bg, final Color[][] fg, final Color[][] overlay) {
        // Figure out the width of effectively one "pixel" at 16x16
        final int a = overlay.length % 16 == 0 ? overlay.length / 16 : 0;

        final Color[][] shifted = ImageUtils.shift(overlay, 1, -1);
        final Color[][] cut = ImageUtils.cut(shifted, a);
        return ImageUtils.overlay(overlay, cut, true);
    }
}
