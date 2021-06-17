package com.personthecat.orestonevariants.textures;

import com.personthecat.orestonevariants.io.BufferOutputStream;
import net.minecraft.util.math.vector.Vector3i;
import personthecat.fresult.Result;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ImageTools {

    /** Pixels with higher alpha levels are considered opaque. */
    private static final int OPACITY_THRESHOLD = 50;

    /** Pixels with lower alpha levels are considered transparent. */
    private static final int TRANSPARENCY_THRESHOLD = 17;

    /** A pixel with no color. */
    private static final Color EMPTY_PIXEL = new Color(0, 0, 0, 0);

    /** The maximum "difference" between any two pixels. */
    private static final double MAX_DIFFERENCE = 441.673;

    /** The maximum possible difference between three color channels. */
    // Edit: this value is not actually the max now that Math.abs is removed. Careful.
    private static final double MAX_ADJUSTMENT = 510.0;

    /** Multiplies the alpha levels for push and pull. */
    private static final double TEXTURE_SHARPEN_RATIO = 2.3;

    /** The maximum level of opacity used by the shading algorithm. */
    private static final int SHADE_OPACITY = 160;

    /** Opacities above this value will be dropped down to it. */
    private static final int SHADE_CUTOFF = 108;

    /**
     * The final version of the algorithm which works by comparing two images
     * and applying various tests to determine the most likely ore pixels based
     * on the best statistics I personally know how to come up with. It is not
     * perfect, but it is successful in the vast majority of cases for ore sprites
     * using the default texture pack and derived art styles.
     */
    public static Color[][] getOverlay(Color[][] bg, Color[][] fg) {
        final Color[][] overlay = new Color[bg.length][bg[0].length];
        final OverlayData data = new OverlayData(bg, fg);
        for (int x = 0; x < bg.length; x++) {
            for (int y = 0; y < bg[0].length; y++) {
                overlay[x][y] = getOrePixel(bg[x][y], fg[x][y], data);
            }
        }
        return overlay;
    }

    /**
     * Determines whether the foreground is different enough from
     * the background. If so, returns it.
     */
    private static Color getOrePixel(Color bg, Color fg, OverlayData data) {
        // First, check to remove any pixels that are almost
        // the same in both images, keeping any that are
        // clearly very different.
        final Vector3i stdDiff = subtract(bg, fg);
        final double stdDist = getDistance(stdDiff);
        if (stdDist > 0.7 * data.maxDist) {
            return fg;
        } else if (stdDist < 0.1 * data.maxDist) {
            return EMPTY_PIXEL;
        }
        // Next, filter out any pixels that are specifically
        // darker versions of the background image.
        final Color darkened = darken(bg, 45);
        final Vector3i darkDiff = subtract(darkened, fg);
        final double darkDist = getDistance(darkDiff);
        if (darkDist < 0.125 * (data.maxRel + 0.001 / data.bgDist + 0.001)) {
            return EMPTY_PIXEL;
        }
        // Then, compare the difference in colors in the
        // foreground with the average color of the
        // background, focusing especially on the differences
        // per channel.
        final Vector3i diff = subtract(data.bgAvg, fg);
        final double dist = getDistance(diff);
        final double relDist = getRelativeDistance(diff);
        // Colorful backgrounds are consistently more difficult
        // to extract, while still having enough flexibility
        // that a single value can be a blanket fix.
        final double threshold = data.bgDist > 0.05 ? 1.2 : 0.2;
        if (dist + relDist * 10.0 > threshold) {
            return fg;
        }
        return EMPTY_PIXEL;
    }

    /**
     * Variant of #getOverlay which places less emphasis on stats and more
     * on a known level of difference to achieve.
     */
    public static Color[][] getOverlayManual(Color[][] bg, Color[][] fg, double threshold) {
        final Color[][] overlay = new Color[bg.length][bg[0].length];
        final OverlayData data = new OverlayData(bg, fg);
        for (int x = 0; x < bg.length; x++) {
            for (int y = 0; y < bg[0].length; y++) {
                overlay[x][y] = getOrePixelManual(bg[x][y], fg[x][y], data, threshold);
            }
        }
        return overlay;
    }

    /** Variant of #getOrePixel which places more importance on the threshold. */
    private static Color getOrePixelManual(Color bg, Color fg, OverlayData data, double threshold) {
        // First, check to remove any pixels that are almost
        // the same in both images, keeping any that are
        // clearly very different.
        final Vector3i stdDiff = subtract(bg, fg);
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
        final Vector3i diff = subtract(data.bgAvg, fg);
        final double dist = getDistance(diff);
        final double relDist = getRelativeDistance(diff);
        if (dist + relDist * 10.0 > threshold) {
            return fg;
        }
        return EMPTY_PIXEL;
    }

    /** Determines whether any pixels in this image are less than opaque. */
    public static boolean isTranslucent(Color[][] image) {
        for (Color[] colors : image) {
            for (Color color : colors) {
                if (color.getAlpha() <= OPACITY_THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This algorithm takes an already generated overlay and applies the
     * texture of the original background behind it using a sort of push
     * and pull method.
     */
    public static Color[][] shadeOverlay(Color[][] overlay, Color[][] bg, Color[][] fg, Color[][] mask) {
        final Color[][] maskScaled = ensureSizeParity(mask, fg);
        final Color[][] bgScaled = ensureSizeParity(bg, fg);
        // This is an old line that looks like a bug, but it
        // works and I'm keeping it.
        final Color[][] bgFilled = fillColors(bgScaled, getAverageColor(bg));
        final Color[][] texturePixels = convertToPushAndPull(bgFilled, fg);
        final Color[][] maskedTexture = removePixels(texturePixels, maskScaled);
        return overlay(maskedTexture, overlay);
    }

    /** Scales the background to the width of the foreground, repeating it for additional frames. */
    public static Color[][] ensureSizeParity(Color[][] background, Color[][] foreground) {
        // Todo: this can no longer account for multi-frame backgrounds.
        final int w = foreground.length;
        background = getColors(ImageTools.scale(getImage(background), w, w));
        background = addFramesToBackground(background, foreground);
        return background;
    }

    /** Generates a matrix of colors from the input BufferedImage. */
    public static Color[][] getColors(BufferedImage image) {
        final int w = image.getWidth(), h = image.getHeight();
        final Color[][] colors = new Color[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                colors[x][y] = new Color(image.getRGB(x, y), true);
            }
        }
        return colors;
    }

    /** Generates a BufferedImage from the input color matrix. */
    public static BufferedImage getImage(Color[][] image) {
        final int w = image.length, h = image[0].length;
        final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                bi.setRGB(x, y, image[x][y].getRGB());
            }
        }
        return bi;
    }

    /** Generates a faux InputStream from the input color matrix. */
    public static InputStream getStream(Color[][] image) {
        BufferOutputStream os = new BufferOutputStream();
        Result.of(() -> ImageIO.write(getImage(image), "png", os))
            .expect("Unable to generate faux InputStream from color matrix.");
        return os.toInputStream();
    }

    /** Pupnewfster's original algorithm for generating dense ore sprites. */
    public static Color[][] shiftImage(Color[][] image) {
        final int w = image.length, h = image[0].length;
        final Color[][] shifted = new Color[w][h];
        final int frames = h / w;
        assert(1.0 * h / w == frames);
        for (int f = 0; f < frames; f++) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < w; y++) {
                    int imageY = f * w + y;
                    shifted[x][imageY] = getAverageColor(
                        image[x][imageY],
                        fromIndex(image, x - 1, imageY, f),
                        fromIndex(image, x + 1, imageY, f),
                        fromIndex(image, x, imageY - 1, f),
                        fromIndex(image, x, imageY + 1, f)
                    );
                }
            }
        }
        return shifted;
    }

    /** Variant of getAverageColor() which accepts a matrix. */
    private static Color getAverageColor(Color[][] image) {
        return getAverageColor(matrixToArray(image));
    }

    /** Gets the average color from an array of colors. */
    private static Color getAverageColor(Color... colors) {
        int r = 0, g = 0, b = 0;
        int count = 0;
        for (Color color : colors) {
            if (color.getAlpha() > OPACITY_THRESHOLD) {
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
                count++;
            }
        }
        if (count == 0) {
            return EMPTY_PIXEL;
        }
        return new Color(r / count, g / count, b / count);
    }

    /** Converts a 2D array of colors to a 1D array. */
    private static Color[] matrixToArray(Color[][] matrix) {
        final Color[] array = new Color[matrix.length * matrix[0].length];
        int index = 0;
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                array[index++] = matrix[x][y];
            }
        }
        return array;
    }

    /** Determines the average difference from the input color. */
    private static double getMaxDistance(Color[][] image, Color[][] from) {
        double num = 0.0;
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[0].length; y++) {
                if (image[x][y].getAlpha() > TRANSPARENCY_THRESHOLD) {
                    num = Math.max(num, getDistance(image[x][y], from[x][y]));
                }
            }
        }
        return num;
    }

    /** Determines the average difference from the input color. */
    private static double getAverageDistance(Color[][] image) {
        double sum = 0.0;
        for (int x = 0; x < image.length - 1; x++) {
            for (int y = 0; y < image[0].length - 1; y++) {
                sum += getDistance(image[x][y], image[x+1][y]);
                sum += getDistance(image[x][y], image[x][y+1]);
            }
        }
        return sum / (image.length * image[0].length * 2);
    }

    private static Vector3i subtract(Color background, Color foreground) {
        final int r = foreground.getRed() - background.getRed();
        final int g = foreground.getGreen() - background.getGreen();
        final int b = foreground.getBlue() - background.getBlue();
        return new Vector3i(r, g, b);
    }

    private static double getDistance(Vector3i difference) {
        final int r = difference.getX();
        final int g = difference.getY();
        final int b = difference.getZ();
        return Math.sqrt((r * r) + (g * g) + (b * b)) / MAX_DIFFERENCE;
    }

    private static double getRelativeDistance(Vector3i difference) {
        final int rO = difference.getX();
        final int gO = difference.getY();
        final int bO = difference.getZ();
        // Get lowest number.
        final int min = Math.min(Math.min(rO, gO), bO);
        // Get ratings of which channels are the most different;
        final int rS = rO - min;
        final int gS = gO - min;
        final int bS = bO - min;
        // Get a 0-1 indicator of channel differences;
        return Math.sqrt((rS * rS) + (gS * gS) + (bS * bS)) / MAX_ADJUSTMENT;
    }

    private static double getMaxRelDist(Color[][] background, Color[][] foreground) {
        double num = 0;
        for (int x = 0; x < background.length; x++) {
            for (int y = 0; y < background[0].length; y++) {
                final Vector3i diff = subtract(background[x][y], foreground[x][y]);
                num = Math.max(num, getRelativeDistance(diff));
            }
        }
        return num;
    }

    /** Returns a darker version of the input Color. */
    private static Color darken(Color c, int amount) {
        int r = c.getRed() - amount;
        int g = c.getGreen() - amount;
        int b = c.getBlue() - amount;
        r = Math.max(r, 0);
        g = Math.max(g, 0);
        b = Math.max(b, 0);
        return new Color(r, g, b);
    }

    /** Calculates the distance between two colors. */
    private static double getDistance(Color background, Color foreground) {
        final int r = foreground.getRed() - background.getRed();
        final int g = foreground.getGreen() - background.getGreen();
        final int b = foreground.getBlue() - background.getBlue();
        return Math.sqrt((r * r) + (g * g) + (b * b)) / MAX_DIFFERENCE;
    }

    /** Fills an entire image with a single color. */
    private static Color[][] fillColors(Color[][] image, Color color) {
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[0].length; y++) {
                image[x][y] = color;
            }
        }
        return image;
    }

    /** Repeats the background image until it is the height of the foreground. */
    private static Color[][] addFramesToBackground(Color[][] background, Color[][] foreground) {
        final int w = background.length, h = background[0].length, nh = foreground[0].length;
        final int frames = nh / h;
        final Color[][] newBackground = new Color[w][h * frames];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < frames; i++) {
                    newBackground[x][(h * i) + y] = background[x][y];
                }
            }
        }
        return newBackground;
    }

    /**
     * Uses getDistance() to determine the alpha level for each pixel.
     * Uses isPixelDarker() to determine whether each pixel should be
     * black or white (push or pull).
     */
    private static Color[][] convertToPushAndPull(Color[][] background, Color[][] foreground) {
        final Color[][] image = new Color[foreground.length][foreground[0].length];
        for (int x = 0; x < foreground.length; x++) {
            for (int y = 0; y < foreground[0].length; y++) {
                int alpha = (int) (SHADE_OPACITY * getDistance(foreground[x][y], background[x][y]));
                if (alpha > SHADE_CUTOFF) {
                    alpha = SHADE_CUTOFF;
                } else if (alpha < 0) {
                    alpha = 0;
                }
                if (isPixelDarker(background[x][y], foreground[x][y])) {
                    image[x][y] = new Color(0, 0, 0, alpha);
                } else {
                    image[x][y] = new Color(255, 255, 255, alpha);
                }
            }
        }
        return image;
    }

    /** Determines whether the foreground is lighter than the background. */
    private static boolean isPixelDarker(Color background, Color foreground) {
        final int fgTotal = Math.abs(foreground.getRed() + foreground.getGreen() + foreground.getBlue());
        final int bgTotal = Math.abs(background.getRed() + background.getGreen() + background.getBlue());
        return fgTotal < bgTotal;
    }

    /** Uses a mask to fade pixels out of an image. */
    private static Color[][] removePixels(Color[][] image, Color[][] mask) {
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[0].length; y++) {
                final int r = image[x][y].getRed();
                final int g = image[x][y].getGreen();
                final int b = image[x][y].getBlue();
                int a = (int) ((double) image[x][y].getAlpha() * (1.0 - ((double) mask[x][y].getAlpha() / 255)));
                if (a < 0) {
                    a = 0;
                } else if (a > 255) {
                    a = 255;
                }
                image[x][y] = new Color(r, g, b, a);
            }
        }
        return image;
    }

    /** Blends the foreground above the background. */
    private static Color[][] overlay(Color[][] background, Color[][] foreground) {
        for (int x = 0; x < foreground.length; x++) {
            for (int y = 0; y < foreground[0].length; y++) {
                foreground[x][y] = blendPixels(background[x][y], foreground[x][y]);
            }
        }
        return foreground;
    }

    /**
     * Gets the weighted average of each color relative to the foreground's
     * alpha level. Foreground gets alpha * its color, background gets the
     * rest * its color. The final alpha is the sum of both.
     */
    private static Color blendPixels(Color bg, Color fg) {
        final int r, g, b;
        if (fg.getAlpha() > OPACITY_THRESHOLD) {
            r = fg.getRed();
            g = fg.getGreen();
            b = fg.getBlue();
        } else {
            r = ((fg.getRed() * fg.getAlpha()) + (bg.getRed() * (255 - fg.getAlpha()))) / 255;
            g = ((fg.getGreen() * fg.getAlpha()) + (bg.getGreen() * (255 - fg.getAlpha()))) / 255;
            b = ((fg.getBlue() * fg.getAlpha()) + (bg.getBlue() * (255 - fg.getAlpha()))) / 255;
        }
        int a = fg.getAlpha() + bg.getAlpha();
        if (a < TRANSPARENCY_THRESHOLD && r == 255 && g == 255 && b == 255) {
            return EMPTY_PIXEL; // Don't keep white pixels.
        }
        a = limitRange((int) ((double) a * TEXTURE_SHARPEN_RATIO));

        return new Color(r, g, b, a);
    }

    private static Color fromIndex(Color[][] image, int x, int y , int frame) {
        final int w = image.length;
        return ((x < 0) || (y < frame * w) || (x >= w) || (y >= (frame + 1) * w) || (image[x][y].getAlpha() == 34)) ?
                EMPTY_PIXEL : image[x][y];
    }

    private static BufferedImage scale(BufferedImage image, int x, int y) {
        BufferedImage scaled = new BufferedImage(x, y, image.getType());
        Graphics2D graphics = scaled.createGraphics();
        graphics.drawImage(image, 0, 0, x, y, null);
        graphics.dispose();
        return scaled;
    }

    /** Corrects the channel value if it is outside of the accepted range. */
    private static int limitRange(int channel) {
        return channel < 0 ? 0 : Math.min(channel, 255);
    }

    private static class OverlayData {
        private final Color bgAvg;
        private final double bgDist;
        private final double maxDist;
        private final double maxRel;

        OverlayData(Color[][] bg, Color[][] fg) {
            // Repeated calculations could be mitigated by storing
            // bg data in a separate object. This would require
            this.bgAvg = getAverageColor(bg);
            this.bgDist = getAverageDistance(bg);
            this.maxDist = getMaxDistance(bg, fg);
            this.maxRel = getMaxRelDist(bg, fg);
        }
    }
}