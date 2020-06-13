package com.personthecat.orestonevariants.textures;

import com.google.common.collect.Lists;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.BufferOutputStream;
import com.personthecat.orestonevariants.io.FileSpec;
import com.personthecat.orestonevariants.io.ZipTools;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.TextureProperties;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import com.personthecat.orestonevariants.util.unsafe.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.io.SafeFileIO.getResource;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class SpriteHandler {

    /** Exposes the collection of built-in resource packs. */
    public static final Lazy<Collection<IResourcePack>> defaultPacks = new Lazy<>(
        SpriteHandler::getDefaultPacks
    );

    /** A list of all currently-enabled ResourcePacks. */
    private static final Lazy<Collection<IResourcePack>> enabledPacks = new Lazy<>(
        SpriteHandler::getEnabledPacks
    );

    /** Generates overlay sprites for all ore properties. */
    public static void generateOverlays() {
        for (OreProperties p : Main.ORE_PROPERTIES) {
            final TextureProperties tex = p.texture;
            handleVariants(tex.background, tex.original, tex.overlayPath);
        }
    }

    /** Generates the main overlays, copying any .mcmeta files in the process. */
    private static void handleVariants(String background, String foreground, String output) {
        loadImage(foreground).ifPresent(fg ->
            loadImage(background).ifPresent(bg -> {
                // Generate paths.
                final String normal = PathTools.ensureNormal(output) + ".png";
                final String shaded = PathTools.ensureShaded(output) + ".png";
                final String dense = PathTools.ensureDense(output) + ".png";
                // Test whether all textures already exist.
                if (!allPathsInResources(normal, shaded, dense)) {
                    // Cache the new files to be written.
                    final Set<FileSpec> files = new HashSet<>();
                    generateOverlays(files, bg, fg, normal, shaded, dense);
                    handleMcMeta(files, foreground, normal, shaded, dense);
                    // Write all of the files in the cache.
                    ZipTools.copyToResources(files.toArray(new FileSpec[0]))
                        .expect("Error writing to resources.zip.");
                }
            })
        );
    }

    /** Attempts to load an image file from the jar, then from the enabled resource packs. */
    private static Optional<BufferedImage> loadImage(String path) {
        Optional<InputStream> is = locateResource(path + ".png");
        if (is.isPresent()) {
            return Result.of(() -> ImageIO.read(is.get())).get(Result::IGNORE);
        }
        return empty();
    }

    /** Generates all of the new overlays and places their information in an array. */
    private static void generateOverlays(Set<FileSpec> files, BufferedImage bg, BufferedImage fg, String normal, String shaded, String dense) {
        // Load original colors.
        final Color[][] fgColors = getColors(fg);
        final Color[][] bgColors = ensureSizeParity(getColors(bg), fgColors);
        // Generate overlays.
        final Color[][] normalColors = Extractor.primary(bgColors, fgColors);
        final Color[][] shadedColors = Extractor.shade(cloneColors(normalColors), bgColors, fgColors);
        final Color[][] denseColors = ImageTools.shiftImage(normalColors);
        // Add all of the overlay specs.
        files.add(new FileSpec(getStream(normalColors), normal));
        files.add(new FileSpec(getStream(shadedColors), shaded));
        files.add(new FileSpec(getStream(denseColors), dense));
    }

    /** Scans all loaded jars and enabled resource packs for a file. */
    private static Optional<InputStream> locateResource(String path) {
        final Optional<InputStream> resource = getResource(path);
        if (resource.isPresent()) {
            return resource;
        }
        if (Cfg.BlocksCat.overlaysFromRP) {
            final ResourceLocation asRL = PathTools.getResourceLocation(path);
            for (IResourcePack rp : enabledPacks.get()) {
                if (rp.resourceExists(asRL)) {
                    try {
                        return full(rp.getInputStream(asRL));
                    } catch (IOException ignored) {}
                }
            }
        }
        return empty();
    }

    /** Scales the background to the width of the foreground, repeating it for additional frames. */
    private static Color[][] ensureSizeParity(Color[][] background, Color[][] foreground) {
        final int w = foreground.length, h = foreground[0].length;
        background = getColors(ImageTools.scale(getImage(background), w, h));
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
    private static void handleMcMeta(Set<FileSpec> files, String forImage, String... paths) {
        locateResource(forImage + ".mcmeta").ifPresent(mcmeta -> {
            for (String path : paths) {
                files.add(new FileSpec(mcmeta, path + ".mcmeta"));
            }
        });
    }

    /** Retrieves the registry of default resource packs. */
    private static Collection<IResourcePack> getDefaultPacks() {
        return ReflectionTools.getValue(Minecraft.class, "defaultResourcePacks", "field_110449_ao", Minecraft.getMinecraft());
    }

    /** Retrieves all currently-enabled ResourcePacks. */
    private static Collection<IResourcePack> getEnabledPacks() {
        return Minecraft.getMinecraft()
            .getResourcePackRepository()
            .getRepositoryEntries()
            .stream()
            .map(ResourcePackRepository.Entry::getResourcePack)
            .collect(Collectors.toCollection(Lists::newLinkedList));
    }

    /** Generates a matrix of colors from the input BufferedImage. */
    private static Color[][] getColors(BufferedImage image) {
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
    private static BufferedImage getImage(Color[][] image) {
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
    private static InputStream getStream(Color[][] image) {
        BufferOutputStream os = new BufferOutputStream();
        Result.of(() -> ImageIO.write(getImage(image), "png", os))
            .expect("Unable to generate faux InputStream from color matrix.");
        return os.toInputStream();
    }

    /** Returns a clone of the input color matrix. */
    private static Color[][] cloneColors(Color[][] colors) {
        final int w = colors.length, h = colors[0].length;
        final Color[][] newColors = new Color[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                newColors[x][y] = colors[x][y];
            }
        }
        return newColors;
    }

    /** For all functions directly related to producing an overlay. */
    private static class Extractor {
        /**
         * The average difference between two textures and their optimal
         * selection threshold are highly correlated (r = 0.9230). This
         * ratio is used to more accurately determine which pixels in a
         * texture belong to the actual ore and not its background.
         */
        private static final double AVG_DIFF_RATIO = 2.6; // Number is poorly tested --10/8/19.
        /** The location of the the vignette mask. */
        private static final String MASK_LOCATION =  f("/assets/{}/textures/mask", Main.MODID);
        /** The mask used for removing edge pixels from larger textures. */
        private static final BufferedImage VIGNETTE_MASK = loadImage(MASK_LOCATION)
            .orElseThrow(() -> runEx("Build error: mask path is invalid."));

        /**
         * Uses the average color of the background texture and the average
         * difference between each image to determine a difference threshold
         * used for retaining select pixels from the foreground. Produces an
         * overlay which ideally containing only the ore pixels from the
         * original foreground texture.
         */
        private static Color[][] primary(Color[][] background, Color[][] foreground) {
            final int w = foreground.length, h = foreground[0].length;
            final Color[][] overlay = new Color[w][h];
            final double avgDiff = ImageTools.getAverageDifference(foreground, background);
            final double threshold = avgDiff * AVG_DIFF_RATIO;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    overlay[x][y] = ImageTools.getOrePixel(background[x][y], foreground[x][y], threshold);
                }
            }
            return overlay;
        }

        /**
         * Variant of primary() which applies shading to push and
         * pull the background texture, matching the original ore sprite.
         */
        private static Color[][] shade(Color[][] overlay, Color[][] background, Color[][] foreground) {
            final Color[][] mask = ensureSizeParity(getColors(VIGNETTE_MASK), foreground);
            background = ensureSizeParity(background, foreground);
            // Again, I forget why only one color was used here.
            background = ImageTools.fillColors(background, ImageTools.getAverageColor(background));
            Color[][] texturePixels = ImageTools.convertToPushAndPull(background, foreground);
            texturePixels = ImageTools.removePixels(texturePixels, mask);
            return ImageTools.overlay(texturePixels, overlay);
        }
    }
}