package personthecat.osv.client.texture;

import lombok.experimental.UtilityClass;
import net.minecraft.core.Vec3i;
import personthecat.catlib.exception.UnreachableException;
import personthecat.osv.io.BufferOutputStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@UtilityClass
public class ImageUtils {

    /** Pixels with higher alpha levels are considered opaque. */
    static final int OPACITY_THRESHOLD = 50;

    /** Pixels with lower alpha levels are considered transparent. */
    static final int TRANSPARENCY_THRESHOLD = 17;

    /** The maximum "difference" between any two pixels. */
    static final double MAX_DIFFERENCE = 441.673;

    /** The maximum possible difference between three color channels. */
    static final double MAX_ADJUSTMENT = 510.0;

    /** A ratio of how much to bump the opacity when blending pixels. */
    static final double TEXTURE_SHARPEN_RATIO = 2.3;

    /** A pixel with no color. */
    static final Color EMPTY_PIXEL = new Color(0, 0, 0, 0);

    static boolean isTranslucent(final Color[][] image) {
        for (final Color[] colors : image) {
            for (final Color color : colors) {
                if (color.getAlpha() <= OPACITY_THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
    }

    static ImagePair matchWithFrames(Color[][] bg, Color[][] fg) {
        if (bg.length > fg.length) {
            fg = scaleToWidth(fg, bg.length);
        } else if (fg.length > bg.length) {
            bg = scaleToWidth(bg, fg.length);
        }
        if (bg[0].length > fg[0].length) {
            fg = repeatVertically(fg, bg[0].length);
        } else if (fg[0].length > bg[0].length) {
            bg = repeatVertically(bg, fg[0].length);
        }
        return new ImagePair(bg, fg);
    }

    static Color[][] scaleToWidth(final Color[][] image, final int width) {
        final int ratio = width / image.length;
        return scale(image, image.length * ratio, image[0].length * ratio);
    }

    static Color[][] repeatVertically(final Color[][] image, final int height) {
        final Color[][] scaled = new Color[image.length][height];
        final int original = image[0].length;
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < height; y++) {
                scaled[x][y] = image[x][y & (original - 1)];
            }
        }
        return scaled;
    }

    static Color[][] scaleWithFrames(final Color[][] image, final int x, final int y) {
        return addFrames(scale(image, x, x), y);
    }

    static Color[][] scale(final Color[][] colors, final int x, final int y) {
        return getColors(scale(getImage(colors), x, y));
    }

    static BufferedImage scale(final BufferedImage image, final int x, final int y) {
        final BufferedImage scaled = new BufferedImage(x, y, image.getType());
        final Graphics2D graphics = scaled.createGraphics();
        graphics.drawImage(image, 0, 0, x, y, null);
        graphics.dispose();
        return scaled;
    }

    static Color[][] addFrames(final Color[][] colors, final int nh) {
        final int w = colors.length;
        final int h = colors[0].length;
        final int frames = nh / h;
        final Color[][] scaled = new Color[w][h * frames];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int f = 0; f < frames; f++) {
                    scaled[x][(h * f) + y] = colors[x][y];
                }
            }
        }
        return scaled;
    }

    static Color fromIndex(final Color[][] image, final int x, final int y) {
        final int w = image.length;
        final int h = image[0].length;
        return x < 0 || x >= w || y < 0 || y >= h ? EMPTY_PIXEL : image[x][y];
    }

    static Color getAverage(final Color... colors) {
        return getAverage(new Color[][] { colors });
    }

    static Color getAverage(final Color[][] colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        int count = 0;
        for (final Color[] array : colors) {
            for (final Color color : array) {
                if (color.getAlpha() > OPACITY_THRESHOLD) {
                    r += color.getRed();
                    g += color.getGreen();
                    b += color.getBlue();
                    count++;
                }
            }
        }
        if (count == 0) {
            return EMPTY_PIXEL;
        }
        return new Color(r / count, g / count, b / count);
    }

    static Vec3i subtract(final Color lhs, final Color rhs) {
        final int r = lhs.getRed() - rhs.getRed();
        final int g = lhs.getGreen() - rhs.getGreen();
        final int b = lhs.getBlue() - rhs.getBlue();
        return new Vec3i(r, g, b);
    }

    static double getDistance(final Vec3i diff) {
        final int r = diff.getX();
        final int g = diff.getY();
        final int b = diff.getZ();
        return Math.sqrt((r * r) + (g * g) + (b * b)) / MAX_DIFFERENCE;
    }

    static double getTotalDistance(final Color[][] lhs, final Color[][] rhs) {
        double total = 0.0;
        for (int x = 0; x < lhs.length; x++) {
            for (int y = 0; y < rhs[0].length; y++) {
                total += getDistance(subtract(lhs[x][y], rhs[x][y]));
            }
        }
        return total;
    }

    static double getMaxDistance(final Color[][] lhs, final Color[][] rhs) {
        double max = 0.0;
        for (int x = 0; x < lhs.length; x++) {
            for (int y = 0; y < rhs[0].length; y++) {
                max = Math.max(max, getDistance(subtract(lhs[x][y], rhs[x][y])));
            }
        }
        return max;
    }

    static double getAverageDistance(final Color[][] colors) {
        double sum = 0.0;
        for (int x = 0; x < colors.length - 1; x++) {
            for (int y = 0; y < colors[0].length - 1; y++) {
                sum += getDistance(subtract(colors[x][y], colors[x + 1][y]));
                sum += getDistance(subtract(colors[x][y], colors[x][y + 1]));
            }
        }
        return sum / (colors.length * colors[0].length * 2);
    }

    static double getRelativeDistance(final Vec3i diff) {
        final int rO = diff.getX();
        final int gO = diff.getY();
        final int bO = diff.getZ();
        // Get lowest channel
        final int min = Math.min(Math.min(rO, gO), bO);
        // Get ratings of which channels are the most different.
        final int rS = rO - min;
        final int gS = gO - min;
        final int bS = bO - min;
        return Math.sqrt((rS * rS) + (gS * gS) + (bS * bS)) / MAX_ADJUSTMENT;
    }

    static double getMaxRelativeDistance(final Color[][] c1, final Color[][] c2) {
        double max = 0;
        for (int x = 0; x < c1.length; x++) {
            for (int y = 0; y < c1[0].length; y++) {
                max = Math.max(max, getRelativeDistance(subtract(c1[x][y], c2[x][y])));
            }
        }
        return max;
    }

    static Color darken(final Color color, final int amount) {
        final int r = Math.max(0, color.getRed() - amount);
        final int g = Math.max(0, color.getGreen() - amount);
        final int b = Math.max(0, color.getBlue() - amount);
        return new Color(r, g, b);
    }

    static Color brighten(final Color color, final int amount) {
        final int r = Math.min(255, color.getRed() + amount);
        final int g = Math.min(255, color.getGreen() + amount);
        final int b = Math.min(255, color.getBlue() + amount);
        return new Color(r, g, b);
    }

    static Color[][] solidColor(final Color color, final int x, final int y) {
        final Color[][] colors = new Color[x][y];
        for (int h = 0; h < y; h++) {
            for (int w = 0; w < x; w++) {
                colors[w][h] = color;
            }
        }
        return colors;
    }

    static boolean isForegroundDarker(final Color background, final Color foreground) {
        final int fgTotal = foreground.getRed() + foreground.getGreen() + foreground.getBlue();
        final int bgTotal = background.getRed() + background.getGreen() + background.getBlue();
        return fgTotal < bgTotal;
    }

    static Color[][] shift(final Color[][] colors, final int sX, final int sY) {
        final int w = colors.length;
        final int h = colors[0].length;
        final Color[][] shifted = blankImage(w, h);
        for (int x = 0; x < w; x++) {
            final int aX = x + sX;
            if (aX < 0 || aX >= w) {
                continue;
            }
            for (int y = 0; y < h; y++) {
                final int aY = y + sY;
                if (aY < 0 || aY >= h) {
                    continue;
                }
                shifted[aX][aY] = colors[x][y];
            }
        }
        return shifted;
    }

    static Color[][] cut(final Color[][] colors, final int a) {
        final int w = colors.length;
        final int h = colors[0].length;
        // image is framed
        if (((double) h) % ((double) w) == 0.0) {
            return cutFramed(colors, a, w, w, h / w);
        }
        return cutFramed(colors, a, w, h, 1);
    }

    static Color[][] cutFramed(final Color[][] colors, int a, int w, int h, int f) {
        final Color[][] cropped = clone(colors);
        for (int i = 0; i < f; i++) {
            int b = h * f; // bottom of frame
            int t = b - h; // top of frame
            for (int x = 0; x < a; x++) { // left
                for (int y = t; y < b; y++) {
                    cropped[x][y] = EMPTY_PIXEL;
                }
            }
            for (int y = t; y < t + a; y++) { // top
                for (int x = a; x < w; x++) {
                    cropped[x][y] = EMPTY_PIXEL;
                }
            }
            for (int x = w - a; x < w; x++) { // right
                for (int y = t; y < b; y++) {
                    cropped[x][y] = EMPTY_PIXEL;
                }
            }
            for (int y = b - a; y < b; y++) { // bottom
                for (int x = a; x < w - a; x++) {
                    cropped[x][y] = EMPTY_PIXEL;
                }
            }
        }
        return cropped;
    }

    static Color[][] outline(final Color[][] colors, final Color c, final int m) {
        final int h = colors.length;
        final int w = colors[0].length;
        final Color[][] outlined = clone(colors);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final Color color = colors[x][y];
                if (color == null || color.getAlpha() == 0) {
                    final boolean adjacent = isSolid(fromIndex(colors, x + 1, y), m)
                        || isSolid(fromIndex(colors, x, y + 1), m)
                        || isSolid(fromIndex(colors, x - 1, y), m)
                        || isSolid(fromIndex(colors, x, y - 1), m);
                    if (adjacent) {
                        outlined[x][y] = c;
                    }
                }
            }
        }
        return outlined;
    }

    static boolean isSolid(final Color color, final int m) {
        return color.getAlpha() > m;
    }

    static Color[][] blankImage(final int w, final int h) {
        final Color[][] colors = new Color[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                colors[x][y] = EMPTY_PIXEL;
            }
        }
        return colors;
    }

    static Color[][] clone(final Color[][] colors) {
        final int w = colors.length;
        final int h = colors[0].length;
        final Color[][] cloned = new Color[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                cloned[x][y] = colors[x][y];
            }
        }
        return cloned;
    }

    static Color[][] invertOpacity(final Color[][] colors) {
        final Color[][] inverted = new Color[colors.length][colors[0].length];
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[0].length; y++) {
                final Color color = colors[x][y];
                final int a = 255 - color.getAlpha();
                inverted[x][y] = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
            }
        }
        return inverted;
    }

    static Color[][] removeByMask(final Color[][] colors, final Color[][] mask) {
        final Color[][] filtered = new Color[colors.length][colors[0].length];
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[0].length; y++) {
                final Color color = colors[x][y];
                final int r = color.getRed();
                final int g = color.getGreen();
                final int b = color.getBlue();
                final int a = (int) ((double) color.getAlpha() * (1.0 - ((double) mask[x][y].getAlpha() / 255)));
                filtered[x][y] = new Color(r, g, b, limitRange(a));
            }
        }
        return filtered;
    }

    static int limitRange(int channel) {
        if (channel < 0) {
            return 0;
        } else if (channel > 255) {
            return 255;
        }
        return channel;
    }

    static Color[][] overlay(final Color[][] background, final Color[][] foreground, final boolean hard) {
        final Color[][] overlay = new Color[background.length][background[0].length];
        for (int x = 0; x < background.length; x++) {
            for (int y = 0; y < background[0].length; y++) {
                overlay[x][y] = blend(background[x][y], foreground[x][y], hard);
            }
        }
        return overlay;
    }

    /**
     * Gets the weighted average of each color relative to the foreground's alpha level.
     * Foreground gets alpha * its color, background gets the rest * its color. The final
     * alpha is the sum of both.
     *
     * @param bg   The background color being added into.
     * @param fg   The foreground color being overlaid onto the background.
     * @param hard Whether to carefully select fg pixels based on opacity.
     * @return The mixture of each color.
     */
    static Color blend(final Color bg, final Color fg, final boolean hard) {
        final int r, g, b;
        if (hard && fg.getAlpha() > OPACITY_THRESHOLD) {
            r = fg.getRed();
            g = fg.getGreen();
            b = fg.getBlue();
        } else {
            r = ((fg.getRed() * fg.getAlpha()) + (bg.getRed() * (255 - fg.getAlpha()))) / 255;
            g = ((fg.getGreen() * fg.getAlpha()) + (bg.getGreen() * (255 - fg.getAlpha()))) / 255;
            b = ((fg.getBlue() * fg.getAlpha()) + (bg.getBlue() * (255 - fg.getAlpha()))) / 255;
        }
        final int a = fg.getAlpha() + bg.getAlpha();
        if (!hard && a < TRANSPARENCY_THRESHOLD && r == 255 && g == 255 && b == 255) {
            return EMPTY_PIXEL; // Don't keep white pixels.
        }
        return new Color(r, g, b, limitRange((int) ((double) a * TEXTURE_SHARPEN_RATIO)));
    }

    static Color[][] getColors(final BufferedImage image) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        final Color[][] colors = new Color[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                colors[x][y] = new Color(image.getRGB(x, y), true);
            }
        }
        return colors;
    }

    static BufferedImage getImage(final Color[][] colors) {
        final int w = colors.length;
        final int h = colors[0].length;
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                image.setRGB(x, y, colors[x][y].getRGB());
            }
        }
        return image;
    }

    static InputStream stream(final Color[][] colors) {
        final BufferOutputStream os = new BufferOutputStream();
        try {
            ImageIO.write(getImage(colors), "png", os);
        } catch (final IOException ignored) {
            throw new UnreachableException();
        }
        return os.toInputStream();
    }
}
