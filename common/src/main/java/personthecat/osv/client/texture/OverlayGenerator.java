package personthecat.osv.client.texture;

import personthecat.osv.preset.data.TextureSettings;

import java.awt.*;

public interface OverlayGenerator {

    default Color[][] generate(final TextureSettings cfg, final Color[][] bg, final Color[][] fg) {
        final Color[][] overlay = new Color[bg.length][bg[0].length];
        final OverlayData data = new OverlayData(bg, fg);
        for (int x = 0; x < bg.length; x++) {
            for (int y = 0; y < bg[0].length; y++) {
                overlay[x][y] = this.getOrePixel(cfg, data, bg[x][y], fg[x][y]);
            }
        }
        return overlay;
    }

    Color getOrePixel(final TextureSettings cfg, final OverlayData data, final Color bg, final Color fg);
}
