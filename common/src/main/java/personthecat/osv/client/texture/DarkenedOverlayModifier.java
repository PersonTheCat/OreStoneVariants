package personthecat.osv.client.texture;

import java.awt.*;

public class DarkenedOverlayModifier implements OverlayModifier {

    public static final DarkenedOverlayModifier INSTANCE = new DarkenedOverlayModifier();

    private DarkenedOverlayModifier() {}

    @Override
    public Color[][] modify(final Color[][] bg, final Color[][] fg, final Color[][] overlay) {
        final Color[][] modified = new Color[overlay.length][overlay[0].length];
        for (int x = 0; x < overlay.length; x++) {
            for (int y = 0; y < overlay[0].length; y++) {
                modified[x][y] = ImageUtils.darken(overlay[x][y], 15);
            }
        }
        return modified;
    }
}
