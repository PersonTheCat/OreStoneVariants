package com.personthecat.orestonevariants.textures;

import com.google.common.collect.Lists;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.*;
import com.personthecat.orestonevariants.util.unsafe.Result;
import com.personthecat.orestonevariants.util.unsafe.Void;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ClientResourcePackInfo;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.SafeFileIO.*;

public class SpriteHandler {
    /** A list of all currently-enabled ResourcePacks. */
    private static final Lazy<Collection<IResourcePack>> enabledPacks = new Lazy<>(
        SpriteHandler::getEnabledPacks
    );

    /** Generates overlay sprites for all ore properties. */
    public static void generateOverlays() {
        for (OreProperties p : Main.ORE_PROPERTIES) {
            handleVariants(p.getBackgroundMatcher(), p.getOriginalTexture(), p.getOverlayPath());
        }
    }

    /** Generates the main overlays, copying any .mcmeta files in the process. */
    private static void handleVariants(String background, String foreground, String output) {
        loadImage(foreground).ifPresent(fg ->
            loadImage(background).ifPresent(bg -> {
                // Generate paths.
                final String normal = PathTools.ensureNormal(output);
                final String shaded = PathTools.ensureShaded(output);
                final String dense = PathTools.ensureDense(output);
                // Test whether all textures already exist.
                if (!allPathsInResources(normal, shaded, dense)) {
                    // Get colors.
                    final Color[][] fgColors = getColorsFromImage(fg);
                    final Color[][] bgColors = ensureSizeParity(getColorsFromImage(bg), fgColors);
                    // Generate overlays.
                    final Color[][] overlayNormal = OverlayExtractor.extractOverlay(bgColors, fgColors);
                    final Color[][] overlayShaded = OverlayExtractor.shadeOverlay(overlayNormal, bgColors, fgColors);
                    final Color[][] overlayDense = ImageTools.shiftImage(overlayNormal);
                    // Write overlays.
                    writeImageToResources(overlayNormal, normal);
                    writeImageToResources(overlayShaded, shaded);
                    writeImageToResources(overlayDense, dense);
                    // Copy any .mcmeta files.
                    handleMcMeta(foreground, normal, shaded, dense);
                }
            })
        );
    }

    /** Attempts to load an image file from the jar, then from the enabled resource packs. */
    private static Optional<BufferedImage> loadImage(String path) {
        Optional<InputStream> is = locateResource(path);
        if (is.isPresent()) {
            return Result.of(() -> ImageIO.read(is.get())).get(Result::IGNORE);
        }
        return empty();
    }

    /** Scans all loaded jars and enabled resource packs for a file. */
    private static Optional<InputStream> locateResource(String path) {
        final Optional<InputStream> resource = getResource(path);
        if (resource.isPresent()) {
            return resource;
        }
        if (Cfg.overlaysFromRp.get()) {
            final ResourcePackType typeRef = ResourcePackType.CLIENT_RESOURCES;
            final ResourceLocation asRL = PathTools.getResourceLocation(path);
            for (IResourcePack rp : enabledPacks.get()) {
                if (rp.resourceExists(typeRef, asRL)) {
                    try {
                        return full(rp.getResourceStream(typeRef, asRL));
                    } catch (IOException ignored) {}
                }
            }
        }
        return empty();
    }

    /** Scales the background to the width of the foreground, repeating it for additional frames. */
    private static Color[][] ensureSizeParity(Color[][] background, Color[][] foreground) {
        final int w = foreground.length, h = foreground[0].length;
        background = getColorsFromImage(ImageTools.scale(getImageFromColors(background), w, h));
        background = ImageTools.addFramesToBackground(background, foreground);
        return background;
    }

    /** Ensures that all paths exist in the mod's resource pack. */
    private static boolean allPathsInResources(String... paths) {
        for (String path : paths) {
            if (!ZipTools.fileInZip(ZipTools.RESOURCE_PACK, path)) {
                return false;
            }
        }
        return true;
    }

    /** Reuses any original .mcmeta files for all overlay variants. */
    private static void handleMcMeta(String forImage, String normal, String shaded, String dense) {
        locateResource(forImage + ".mcmeta").ifPresent(mcmeta -> {
            Result.of(() -> {
                File tmp = toTempFile(mcmeta, "image", ".mcmeta");
                ZipTools.copyToResources(tmp, normal + ".mcmeta").throwIfErr();
                ZipTools.copyToResources(tmp, shaded + ".mcmeta").throwIfErr();
                ZipTools.copyToResources(tmp, dense + ".mcmeta").throwIfErr();
            }).handle(e -> warn("Error when reusing .mcmeta file: {}", e));
        });
    }

    /** Copies the input stream into a temporary file. */
    private static File toTempFile(InputStream is, String prefix, String suffix) throws IOException {
        final File tmp = File.createTempFile(prefix, suffix);
        tmp.deleteOnExit();
        Result.with(() -> new FileOutputStream(tmp.getPath()), fos -> {
            copyStream(is, fos, 1024).throwIfErr();
        }).throwIfErr();
        return tmp;
    }

    /** Retrieves all currently-enabled ResourcePacks. */
    private static Collection<IResourcePack> getEnabledPacks() {
        return Minecraft.getInstance()
            .getResourcePackList()
            .getEnabledPacks()
            .stream()
            .map(ClientResourcePackInfo::getResourcePack)
            .collect(Collectors.toCollection(Lists::newLinkedList));
    }

    /** Attempts to write the image to the specified zip file. */
    private static Result<Void, IOException> writeImageToResources(Color[][] overlay, String path) {
        return Result.of(() -> {
            final File tmp = File.createTempFile("overlay", ".png");
            tmp.deleteOnExit();
            writeImageToFile(getImageFromColors(overlay), tmp.getPath()).throwIfErr();
            ZipTools.copyToResources(tmp, path).throwIfErr();
        });
    }

    /** Generates a matrix of colors from the input BufferedImage. */
    private static Color[][] getColorsFromImage(BufferedImage image) {
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
    private static BufferedImage getImageFromColors(Color[][] image) {
        final int w = image.length, h = image[0].length;
        final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                bi.setRGB(x, y, image[x][y].getRGB());
            }
        }
        return bi;
    }

    /** Attempts to write the image to the specified path. */
    private static Result<Void, IOException> writeImageToFile(BufferedImage image, String path) {
        return Result.of(() -> {
            final File png = new File(path);
            ImageIO.write(image, "png", png);
        });
    }

    /** For all functions directly related to producing an overlay. */
    private static class OverlayExtractor {
        /**
         * The average difference between two textures and their optimal
         * selection threshold are highly correlated (r = 0.9230). This
         * ratio is used to more accurately determine which pixels in a
         * texture belong to the actual ore and not its background.
         */
        private static final double AVG_DIFF_RATIO = 0.0055;
        /** The location of the the vignette mask. */
        private static final String MASK_LOCATION =  f("assets/{}/textures/mask.png", Main.MODID);
        /** The mask used for removing edge pixels from larger textures. */
        private static final BufferedImage VIGNETTE_MASK = loadImage(MASK_LOCATION).get();

        /**
         * Uses the average color of the background texture and the average
         * difference between each image to determine a difference threshold
         * used for retaining select pixels from the foreground. Produces an
         * overlay which ideally containing only the ore pixels from the
         * original foreground texture.
         */
        private static Color[][] extractOverlay(Color[][] background, Color[][] foreground) {
            final int w = foreground.length, h = foreground[0].length;
            final Color[][] overlay = new Color[w][h];
            // Don't remember why the average color is used.
            final Color bgAvg = ImageTools.getAverageColor(background);
            final double avgDiff = ImageTools.getAverageDifference(foreground, bgAvg);
            final double threshold = avgDiff * AVG_DIFF_RATIO;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    overlay[x][y] = ImageTools.getOrePixel(bgAvg, foreground[x][y], threshold);
                }
            }
            return overlay;
        }

        /**
         * Variant of extractOverlay() which applies shading to push and
         * pull the background texture, matching the original ore sprite.
         */
        private static Color[][] extractShadedOverlay(Color[][] background, Color[][] foreground) {
            final Color[][] overlay = extractOverlay(background, foreground);
            return shadeOverlay(overlay, background, foreground);
        }

        private static Color[][] shadeOverlay(Color[][] overlay, Color[][] background, Color[][] foreground) {
            final Color[][] mask = ensureSizeParity(getColorsFromImage(VIGNETTE_MASK), foreground);
            background = ensureSizeParity(background, foreground);
            // Again, I forget why only one color was used here.
            background = ImageTools.fillColors(background, ImageTools.getAverageColor(background));
            Color[][] texturePixels = ImageTools.convertToPushAndPull(background, foreground);
            texturePixels = ImageTools.removePixels(texturePixels, mask);
            return ImageTools.overlay(texturePixels, overlay);
        }
    }
}