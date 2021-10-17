package personthecat.osv.client.texture;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.PathUtils;
import personthecat.catlib.util.Shorthand;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.io.FileSpec;
import personthecat.osv.io.PathSet;
import personthecat.osv.io.ResourceHelper;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.TextureSettings;
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

    // Todo: Somehow make these config-dependent (preset determines a list of modifiers)
    private static final OverlayGenerator NORMAL_GENERATOR = new SimpleOverlayGenerator();
    private static final OverlayGenerator THRESHOLD_GENERATOR = new ThresholdOverlayGenerator();
    private static final OverlayModifier DENSE_MODIFIER = new DenseOverlayModifier();
    private static final OverlayModifier SHADED_MODIFIER = new ShadedOverlayModifier();

    private static final Set<String> GENERATED_OVERLAYS = ConcurrentHashMap.newKeySet();

    public static boolean overlaysGenerated(final OrePreset preset) {
        return GENERATED_OVERLAYS.contains(preset.getName());
    }

    public static void generateOverlays(final OrePreset preset) {
        preset.getBackgroundPaths().biConsume(preset.getOverlayPaths(), (originals, overlays) ->
            Shorthand.biConsume(originals, overlays, (fg, out) ->
                ResourceHelper.writeResources(generateVariants(preset.getTexture(), fg, out))
            )
        );
        GENERATED_OVERLAYS.add(preset.getName());
    }

    private static Set<FileSpec> generateVariants(final TextureSettings cfg, final String fg, final String out) {
        final Color[][] fgColors = ImageLoader.loadColors(fg).orElse(null);
        final Color[][] bgColors = ImageLoader.loadColors(cfg.getBackground()).orElse(null);

        if (fgColors == null) {
            log.error("Missing fg sprite: {}", fg);
        }
        if (bgColors == null) {
            log.error("Missing bg sprite: {}", cfg.getBackground());
        }
        if (fgColors != null && bgColors != null) {
            final Set<FileSpec> files = new HashSet<>();
            final PathSet paths = new PathSet(out);
            generateOverlays(cfg, files, paths, bgColors, fgColors);
            generateMcMeta(files, paths, fg);
            return files;
        } else {
            return Collections.emptySet();
        }
    }

    private static void generateOverlays(final TextureSettings cfg, final Set<FileSpec> files, final PathSet paths, final Color[][] bg, final Color[][] fg) {
        final Color[][] bgScaled = ImageUtils.scaleWithFrames(bg, fg.length, fg[0].length);
        final Optional<Color[][]> loadNormal = ImageLoader.loadColors(paths.normal);
        final Optional<Color[][]> loadShaded = ImageLoader.loadColors(paths.shaded);
        final Optional<Color[][]> loadDense = ImageLoader.loadColors(paths.dense);

        final Color[][] normalColors = loadNormal.orElseGet(() -> generateOverlay(cfg, bgScaled, fg));
        final Color[][] shadedColors = loadShaded.orElseGet(() -> SHADED_MODIFIER.modify(bgScaled, fg, normalColors));
        final Color[][] denseColors = loadDense.orElseGet(() -> DENSE_MODIFIER.modify(bgScaled, fg, normalColors));

        if (!loadNormal.isPresent()) {
            files.add(new FileSpec(() -> ImageUtils.getStream(normalColors), paths.normal));
        }
        if (!loadShaded.isPresent()) {
            files.add(new FileSpec(() -> ImageUtils.getStream(shadedColors), paths.shaded));
        }
        if (!loadDense.isPresent()) {
            files.add(new FileSpec(() -> ImageUtils.getStream(denseColors), paths.dense));
        }
    }

    private static Color[][] generateOverlay(final TextureSettings cfg, final Color[][] bg, final Color[][] fg) {
        if (ImageUtils.isTranslucent(fg)) {
            return fg;
        } else if (cfg.getThreshold() != null) {
            return THRESHOLD_GENERATOR.generate(cfg, bg, fg);
        }
        return NORMAL_GENERATOR.generate(cfg, bg, fg);
    }

    private static void generateMcMeta(final Set<FileSpec> files, final PathSet paths, final String image) {
        final String metaPath = image + ".mcmeta";
        if (FileIO.resourceExists(metaPath)) {
            for (final String path : paths) {
                final Supplier<InputStream> getter = () -> ClientResourceHelper.locateResource(metaPath)
                    .orElseThrow(() -> resourceEx("Resource deleted: {}", metaPath));
                files.add(new FileSpec(getter::get, path + ".mcmeta"));
            }
        }
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
            ResourceHelper.writeResource(path, ImageUtils.getStream(colors)).expect("Writing {}", id);
        }
        return id;
    }

    private static ResourceLocation createId(final ResourceLocation bg, final ResourceLocation fg) {
        final String newPath = fg.getPath() + "_" + createPrefix(bg);
        return new ResourceLocation(Reference.MOD_ID, newPath);
    }

    private static String createPrefix(final ResourceLocation id) {
        final String end = id.getPath().replace("/", "_");
        if ("minecraft".equals(id.getNamespace())) {
            return end;
        }
        return id.getNamespace() + "_" + end;
    }
}
