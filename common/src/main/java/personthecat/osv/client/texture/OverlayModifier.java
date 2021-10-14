package personthecat.osv.client.texture;

import java.awt.*;

public interface OverlayModifier {
    Color[][] modify(final Color[][] bg, final Color[][] fg, final Color[][] overlay);
}
