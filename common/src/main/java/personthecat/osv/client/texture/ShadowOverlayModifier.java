package personthecat.osv.client.texture;

import java.awt.*;

public class ShadowOverlayModifier implements OverlayModifier {

    public static final ShadowOverlayModifier INSTANCE = new ShadowOverlayModifier();
    private static final Color SHADOW = new Color(0, 0, 0, 30);
    private static final int ORE_THRESHOLD = 50;

    private ShadowOverlayModifier() {}

    @Override
    public Color[][] modify(final Color[][] bg, final Color[][] fg, final Color[][] overlay) {
        return ImageUtils.outline(overlay, SHADOW, ORE_THRESHOLD);
    }
}
