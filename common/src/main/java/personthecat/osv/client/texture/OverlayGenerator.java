package personthecat.osv.client.texture;

import personthecat.osv.preset.data.TextureSettings;

import java.awt.*;

public interface OverlayGenerator {
    void generate(final TextureSettings cfg, final Color[][] fg, final Color[][] bg);
}
