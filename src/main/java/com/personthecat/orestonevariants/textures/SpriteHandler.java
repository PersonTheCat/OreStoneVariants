package com.personthecat.orestonevariants.textures;

import com.google.common.collect.Lists;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.FileSpec;
import com.personthecat.orestonevariants.io.ZipTools;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.TextureProperties;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathSet;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import personthecat.fresult.Result;

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
import static com.personthecat.orestonevariants.textures.ImageTools.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SpriteHandler {

    /** A list of all currently-enabled ResourcePacks. */
    private static final Lazy<Collection<IResourcePack>> ENABLED_PACKS = new Lazy<>(
        SpriteHandler::getEnabledPacks
    );

    /** The location of the the vignette mask. */
    private static final String MASK_LOCATION = f("/assets/{}/textures/mask.png", Main.MODID);

    /** The mask used for removing edge pixels from larger textures. */
    private static final Color[][] MASK = loadColors(MASK_LOCATION)
        .orElseThrow(() -> runEx("Build error: mask path is invalid."));

    /** Generates overlay sprites for all ore properties. */
    public static void generateOverlays() {
        final Set<FileSpec> files = new HashSet<>();
        for (OreProperties p : Main.ORE_PROPERTIES) {
            info("Generating textures for {}.", p.name);
            final TextureProperties tex = p.texture;
            handleVariants(files, tex.background, tex.original, tex.overlayPath.get(), tex.threshold);
        }
        // Write all of the files in the cache.
        ZipTools.copyToResources(files.toArray(new FileSpec[0]))
            .expect("Error writing to resources.zip.");
    }

    /** Generates the main overlays, copying any .mcmeta files in the process. */
    private static void handleVariants(Set<FileSpec> files, String background, String foreground, String output, Optional<Float> threshold) {
        final Optional<Color[][]> fgColors = loadColors(foreground);
        final Optional<Color[][]> bgColors = loadColors(background);
        if (!fgColors.isPresent()) {
            info("Missing fg sprite: {}", foreground);
        }
        if (!bgColors.isPresent()) {
            info("Missing bg sprite: {}", background);
        }
        fgColors.ifPresent(fg ->
            bgColors.ifPresent(bg -> {
                // Generate paths.
                final PathSet paths = new PathSet(output, "");
                // Test whether all textures already exist.
                // Cache the new files to be written.
                generateOverlays(files, bg, fg, paths, threshold);
                handleMcMeta(files, foreground, paths);
            })
        );
    }

    /** Generates all of the new overlays and places their information in an array. */
    private static void generateOverlays(Set<FileSpec> files, Color[][] bg, Color[][] fg, PathSet paths, Optional<Float> threshold) {
        // Make sure the background is scaled correctly.
        final Color[][] bgScaled = ensureSizeParity(bg, fg);

        // Attempt to load existing overlays.
        Optional<Color[][]> loadNormal = loadColors(paths.normal);
        Optional<Color[][]> loadShaded = loadColors(paths.shaded);
        Optional<Color[][]> loadDense = loadColors(paths.dense);

        // Generate overlays, if absent.
        final Color[][] normalColors = loadNormal.orElseGet(() -> genOverlay(bgScaled, fg, threshold));
        final Color[][] normalClone = cloneColors(normalColors);
        final Color[][] shadedColors = loadShaded.orElseGet(() -> shadeOverlay(normalClone, bgScaled, fg, MASK));
        final Color[][] denseColors = loadDense.orElseGet(() -> shiftImage(normalColors));

        // Queue missing overlays to be copied.
        if (!loadNormal.isPresent()) {
            files.add(new FileSpec(() -> getStream(normalColors), paths.normal));
        }
        if (!loadShaded.isPresent()) {
            files.add(new FileSpec(() -> getStream(shadedColors), paths.shaded));
        }
        if (!loadDense.isPresent()) {
            files.add(new FileSpec(() -> getStream(denseColors), paths.dense));
        }
    }

    private static Color[][] genOverlay(Color[][] bg, Color[][] fg, Optional<Float> threshold) {
        return threshold.map(t -> getOverlayManual(bg, fg, t)).orElse(getOverlay(bg, fg));
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
        if (Cfg.overlaysFromRp.get()) {
            final ResourceLocation asRL = PathTools.getResourceLocation(path);
            for (IResourcePack rp : ENABLED_PACKS.get()) {
                if (rp.resourceExists(ResourcePackType.CLIENT_RESOURCES, asRL)) {
                    try {
                        return full(rp.getResourceStream(ResourcePackType.CLIENT_RESOURCES, asRL));
                    } catch (IOException ignored) {}
                }
            }
        }
        return getResource(path);
    }

    /**
     * Determines whether a resource exists in any location. Use this to avoid
     * generating too many open InputStreams at onec
     */
    private static boolean resourceExists(String path) {
        return locateResource(path).map(is -> {
            try {
                is.close();
            } catch (IOException e) {
                throw runEx("Unable to close temporary resource.", e);
            }
            return true;
        }).isPresent();
    }


    private static Optional<Color[][]> loadColors(String path) {
        return loadImage(path).map(ImageTools::getColors);
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

    /** Reuses any original .mcmeta files for all overlay variants. */
    private static void handleMcMeta(Set<FileSpec> files, String forImage, PathSet paths) {
        final String metaPath = forImage + ".mcmeta";
        if (resourceExists(metaPath)) {
            for (String path : paths) {
                files.add(new FileSpec(() -> getResource(metaPath).get(), path + ".mcmeta"));
            }
        }
    }

    /** Retrieves all currently-enabled ResourcePacks. */
    private static Collection<IResourcePack> getEnabledPacks() {
        return Minecraft.getInstance()
            .getResourcePackList()
            .getEnabledPacks()
            .stream()
            .map(ResourcePackInfo::getResourcePack)
            .collect(Collectors.toCollection(Lists::newLinkedList));
    }

}