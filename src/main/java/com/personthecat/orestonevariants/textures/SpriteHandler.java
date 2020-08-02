package com.personthecat.orestonevariants.textures;

import com.google.common.collect.Lists;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.FileSpec;
import com.personthecat.orestonevariants.io.ZipTools;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.TextureProperties;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import com.personthecat.orestonevariants.util.PathTools.PathSet;
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
import static com.personthecat.orestonevariants.textures.ImageTools.*;

public class SpriteHandler {

    /** The location of the the vignette mask. */
    private static final String MASK_LOCATION = f("/assets/{}/textures/mask", Main.MODID);
    /** The mask used for removing edge pixels from larger textures. */
    private static final Color[][] MASK = loadColors(MASK_LOCATION)
        .orElseThrow(() -> runEx("Build error: mask path is invalid."));

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
            // Todo: allow overlay variants to be included individually. Move builtin check.
            if (!tex.builtIn) {
                handleVariants(tex.background, tex.original, tex.overlayPath, tex.threshold);
            }
        }
    }

    /** Generates the main overlays, copying any .mcmeta files in the process. */
    private static void handleVariants(String background, String foreground, String output, Optional<Float> threshold) {
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
                final PathSet paths = new PathSet(output, ".png");
                // Test whether all textures already exist.
                // Cache the new files to be written.
                final Set<FileSpec> files = new HashSet<>();
                generateOverlays(files, bg, fg, paths, threshold);
                handleMcMeta(files, foreground, paths);
                // Write all of the files in the cache.
                ZipTools.copyToResources(files.toArray(new FileSpec[0]))
                    .expect("Error writing to resources.zip.");
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
            files.add(new FileSpec(getStream(normalColors), paths.normal));
        }
        if (!loadShaded.isPresent()) {
            files.add(new FileSpec(getStream(shadedColors), paths.shaded));
        }
        if (!loadDense.isPresent()) {
            files.add(new FileSpec(getStream(denseColors), paths.dense));
        }
    }

    private static Color[][] genOverlay(Color[][] bg, Color[][] fg, Optional<Float> threshold) {
        return threshold.isPresent() ? getOverlayManual(bg, fg, threshold.get()) : getOverlay(bg, fg);
    }

    /** Attempts to load an image file from the jar, then from the enabled resource packs. */
    private static Optional<BufferedImage> loadImage(String path) {
        Optional<InputStream> is = locateResource(path + ".png");
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
}