package com.personthecat.orestonevariants.textures;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageTools {
    /** Pixels with higher alpha levels are considered opaque. */
    private static final int OPACITY_THRESHOLD = 50;
    /** Pixels with lower alpha levels are considered transparent. */
    private static final int TRANSPARENCY_THRESHOLD = 17;
    /** A pixel with no color. */
    private static final Color EMPTY_PIXEL = new Color(0, 0, 0, 0);
    /** The maximum "difference" between any two pixels. */
    private static final double MAXIMUM_DIFFERENCE = 441.673;
    /** Multiplies the alpha levels for push and pull. */
    private static final double TEXTURE_SHARPEN_RATIO = 2.3;

    /** Variant of getAverageColor() which accepts a matrix. */
    public static Color getAverageColor(Color[][] image) {
        return getAverageColor(matrixToArray(image));
    }

    /** Gets the average color from an array of colors. */
    public static Color getAverageColor(Color... colors) {
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
    public static double getAverageDifference(Color[][] image, Color from) {
        double avg = 0.0;
        int count = 0;
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[0].length; y++) {
                if (image[x][y].getAlpha() > TRANSPARENCY_THRESHOLD) {
                    avg += getDifference(image[x][y], from);
                    count++;
                }
            }
        }
        return avg / count;
    }

    /**
     * Determines whether the foreground is different enough from
     * the background. If so, returns it.
     */
    public static Color getOrePixel(Color background, Color foreground, double threshold) {
        if (getDifference(background, foreground) > threshold) {
            return foreground;
        }
        return EMPTY_PIXEL;
    }

    /** Calculates the distance between two colors. */
    public static double getDifference(Color background, Color foreground) {
        final int r = foreground.getRed() - background.getRed();
        final int g = foreground.getGreen() - background.getGreen();
        final int b = foreground.getBlue() - background.getBlue();
        return Math.sqrt((r * r) + (g * g) + (b * b)) / MAXIMUM_DIFFERENCE;
    }

    /** Fills an entire image with a single color. */
    public static Color[][] fillColors(Color[][] image, Color color) {
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[0].length; y++) {
                image[x][y] = color;
            }
        }
        return image;
    }

    /** Repeats the background image until it is the height of the foreground. */
    public static Color[][] addFramesToBackground(Color[][] background, Color[][] foreground) {
        final int w = background.length, h = background[0].length, nh = foreground.length;
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
     * Uses getDifference() to determine the alpha level for each pixel.
     * Uses isPixelDarker() to determine whether each pixel should be
     * black or white (push or pull).
     */
    public static Color[][] convertToPushAndPull(Color[][] background, Color[][] foreground) {
        final Color[][] image = new Color[foreground.length][foreground[0].length];
        for (int x = 0; x < foreground.length; x++) {
            for (int y = 0; y < foreground[0].length; y++) {
                int alpha = (int) (255 * getDifference(foreground[x][y], background[x][y]));
                if (alpha > 200) {
                    alpha = 200;
                } else if (alpha < 0) {
                    alpha = 0;
                }
                if (isPixelDarker(foreground[x][y], background[x][y])) {
                    image[x][y] = new Color(0, 0, 0, alpha);
                } else {
                    image[x][y] = new Color(255, 255, 255, alpha);
                }
            }
        }
        return image;
    }

    /** Determines whether the foreground is lighter than the background. */
    public static boolean isPixelDarker(Color background, Color foreground) {
        final int fgTotal = Math.abs(foreground.getRed() + foreground.getGreen() + foreground.getBlue());
        final int bgTotal = Math.abs(background.getRed() + background.getGreen() + background.getBlue());
        return fgTotal < bgTotal;
    }

    /** Uses a mask to fade pixels out of an image. */
    public static Color[][] removePixels(Color[][] image, Color[][] mask) {
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
    public static Color[][] overlay(Color[][] background, Color[][] foreground) {
        for (int x = 0; x < foreground.length; x++) {
            for (int y = 0; y < foreground[0].length; y++) {
                foreground[x][y] = blendPixels(foreground[x][y], background[x][y]);
            }
        }
        return background;
    }

    /**
     * Gets the weighted average of each color relative to the foreground's
     * alpha level. Foreground gets alpha * its color, background gets the
     * rest * its color. The final alpha is the sum of both.
     */
    public static Color blendPixels(Color bg, Color fg) {
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

    /** Pupnewfster's original algorithm for generating dense ore sprites. */
    public static Color[][] shiftImage(Color[][] image) {
        final int w = image.length, h = image[0].length;
        final Color[][] shifted = new Color[w][h];
        final int frames = h / w;
        assert(1.0 * h / w == frames);
        for (int f = 0; f < frames; f++) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
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

    private static Color fromIndex(Color[][] image, int x, int y , int frame) {
        final int w = image.length;
        return ((x < 0) || (y < frame * w) || (x >= w) || (y >= (frame + 1) * w) || (image[x][y].getAlpha() == 34)) ?
            EMPTY_PIXEL : image[x][y];
    }

    public static BufferedImage scale(BufferedImage image, int x, int y) {
        BufferedImage scaled = new BufferedImage(x, y, image.getType());
        Graphics2D graphics = scaled.createGraphics();
        graphics.drawImage(image, 0, 0, x, y, null);
        graphics.dispose();
        return scaled;
    }

    /** Corrects the channel value if it is outside of the accepted range. */
    private static int limitRange(int channel) {
        return channel < 0 ? 0 : channel > 255 ? 255 : channel;
    }
}