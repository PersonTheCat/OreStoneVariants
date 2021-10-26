package personthecat.osv.client.texture;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.PathUtils;
import personthecat.catlib.util.Shorthand;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.io.FileSpec;
import personthecat.osv.io.ResourceHelper;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.util.Reference;

import java.awt.*;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static personthecat.catlib.exception.Exceptions.resourceEx;

@Log4j2
public class TextureHandler {

    private static final Set<String> GENERATED_OVERLAYS = ConcurrentHashMap.newKeySet();

    public static boolean overlaysGenerated(final OrePreset preset) {
        return GENERATED_OVERLAYS.contains(preset.getName());
    }

    public static void generateOverlays(final OrePreset preset) {
        preset.getBackgroundPaths().biConsume(preset.getOverlayPaths(), (originals, overlays) ->
            Shorthand.biConsume(originals, overlays, (fg, out) ->
                ResourceHelper.writeResources(generateVariants(preset, fg, out)).unwrap()
            )
        );
        GENERATED_OVERLAYS.add(preset.getName());
    }

    private static Set<FileSpec> generateVariants(final OrePreset cfg, final String fg, final String out) {
        final Color[][] fgColors = ImageLoader.loadColors(fg).orElse(null);
        final Color[][] bgColors = ImageLoader.loadColors(cfg.getBackgroundTexture()).orElse(null);

        if (fgColors == null) {
            log.error("Missing fg sprite: {}", fg);
        }
        if (bgColors == null) {
            log.error("Missing bg sprite: {}", cfg.getTexture().getBackground());
        }
        if (fgColors != null && bgColors != null) {
            final Set<FileSpec> files = generateOverlays(cfg, out, bgColors, fgColors);
            generateMcMeta(files, fg);
            return files;
        } else {
            return Collections.emptySet();
        }
    }

    private static Set<FileSpec> generateOverlays(final OrePreset cfg, final String out, final Color[][] bg, final Color[][] fg) {
        final Set<FileSpec> files = new HashSet<>();
        final Color[][] bgScaled = ImageUtils.scaleWithFrames(bg, fg.length, fg[0].length);
        final Optional<Color[][]> loadNormal = ImageLoader.loadColors(out);
        final Color[][] normalColors = loadNormal.orElseGet(() -> generateOverlay(cfg, bgScaled, fg));

        if (!loadNormal.isPresent()) {
            files.add(new FileSpec(() -> ImageUtils.stream(normalColors), out));
        }
        cfg.getOverlayModifiers().forEach((state, modifiers) -> {
            final String affix = Modifier.format(modifiers);
            final String modified = PathUtils.appendFilename(out, "_" + affix);

            if (!ClientResourceHelper.hasResource(modified)) {
                Color[][] image = normalColors;
                for (final Modifier modifier : modifiers) {
                    image = modifier.get().modify(bgScaled, fg, image);
                }
                final Color[][] finalImage = image;
                files.add(new FileSpec(() -> ImageUtils.stream(finalImage), modified));
            }
        });
        return files;
    }

    private static Color[][] generateOverlay(final OrePreset cfg, final Color[][] bg, final Color[][] fg) {
        if (ImageUtils.isTranslucent(fg)) {
            return fg;
        } else {
            return cfg.getOverlayGenerator().generate(cfg.getTexture(), bg, fg);
        }
    }

    private static void generateMcMeta(final Set<FileSpec> files, final String image) {
        final String metaPath = image + ".mcmeta";
        if (FileIO.resourceExists(metaPath)) {
            for (final String path : getPaths(files)) {
                final Supplier<InputStream> getter = () -> ClientResourceHelper.locateResource(metaPath)
                    .orElseThrow(() -> resourceEx("Resource deleted: {}", metaPath));
                files.add(new FileSpec(getter::get, path + ".mcmeta"));
            }
        }
    }

    private static Set<String> getPaths(final Set<FileSpec> files) {
        final Set<String> paths = new HashSet<>();
        for (final FileSpec file : files) {
            paths.add(file.path);
        }
        return paths;
    }

    @Nullable
    public static ResourceLocation generateSingleLayer(final ResourceLocation bg, final ResourceLocation fg) {
        final ResourceLocation id = createId(bg, fg);
        final String path = PathUtils.asTexturePath(id);

        if (!ClientResourceHelper.hasResource(path)) {
            final Optional<Color[][]> loadBg = ImageLoader.loadColors(bg);
            final Optional<Color[][]> loadFg = ImageLoader.loadColors(fg);

            if (!(loadBg.isPresent() && loadFg.isPresent())) {
                return null;
            }
            final Color[][] colors = ImageUtils.overlay(loadBg.get(), loadFg.get());
            ResourceHelper.writeResource(path, ImageUtils.stream(colors)).expect("Writing {}", id);
        }
        return id;
    }

    private static ResourceLocation createId(final ResourceLocation bg, final ResourceLocation fg) {
        final String newPath = fg.getPath() + "_" + createPrefix(bg);
        return new ResourceLocation(Reference.MOD_ID, newPath);
    }

    private static String createPrefix(final ResourceLocation id) {
        final String end = id.getPath().replaceAll("^block/", "").replace("/", "_");
        if ("minecraft".equals(id.getNamespace())) {
            return end;
        }
        return id.getNamespace() + "_" + end;
    }
}
