package personthecat.osv.init;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.SafeRegistry;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.io.FileIO;
import personthecat.osv.exception.PresetLoadException;
import personthecat.osv.io.JarFiles;
import personthecat.osv.io.ModFolders;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.util.Reference;

import java.io.File;
import java.util.*;

import static java.util.Optional.empty;
import static personthecat.catlib.util.Shorthand.full;
import static personthecat.catlib.util.PathUtils.extension;
import static personthecat.catlib.util.PathUtils.noExtension;

// Todo: on ore loaded: generate textures, etc
//   figure out if this ore loaded late && generated textures
//     if so, schedule refresh?

// Todo: on getOres (rename to initOreRegistry)
//   return a copy of the output ores
//     do not drain the context
//   add a way to "deep" or fully reload the registry
//     add some kind of flag
//     start from scratch

@Log4j2
public class PresetLoadingContext {

    private static final SafeRegistry<String, File> ORES =
        SafeRegistry.of(() -> collectPresets(ModFolders.ORE_DIR)).canBeReset(true);

    private static final SafeRegistry<String, File> STONE =
        SafeRegistry.of(() -> collectPresets(ModFolders.STONE_DIR)).canBeReset(true);

    private static final Context CTX = new Context();

    private PresetLoadingContext() {}

    private static Map<String, File> collectPresets(final File dir) {
        final Map<String, File> files = new HashMap<>();
        for (final File file : FileIO.listFilesRecursive(dir)) {
            if (isPreset(file)) {
                files.put(noExtension(file), file);
            }
        }
        return files;
    }

    private static boolean isPreset(final File file) {
        return !JarFiles.TUTORIAL.equals(file.getName()) && Reference.VALID_EXTENSIONS.contains(extension(file));
    }

    // Todo: support loading presets by ore ID.
    public static Optional<OrePreset> loadOre(final String path) {
        final OrePreset cached = CTX.outputOres.get(path);
        return cached != null ? full(cached) : createOre(path);
    }

    private static Optional<OrePreset> createOre(final String path) {
        final File file = ORES.get(path);
        final Optional<OrePreset> ore;
        if (file != null) {
            ore = createFromFile(path, file);
        } else {
            ore = Optional.of(OrePreset.createDynamic(new ResourceLocation(path)));
        }
        ore.ifPresent(o -> onOreCreated(file, path, o));
        return ore;
    }

    private static Optional<OrePreset> createFromFile(final String path, final File file) {
        try {
            final Optional<OrePreset> ore = OrePreset.fromFile(file);
            if (ore.isPresent()) {
                synchronized (CTX) {
                    CTX.outputOres.put(path, ore.get());
                }
            }
            return ore;
        } catch (final PresetLoadException e) {
            LibErrorContext.registerSingle(Reference.MOD_NAME, e);
        }
        return empty();
    }

    private static void onOreCreated(@Nullable final File file, final String name, final OrePreset ore) {
        final ResourceLocation id = ore.getOreId();
        if (file == null) {
            log.info("Ore preset {} is loading dynamically.", name);
        } else if (id != null) {
            log.info("Ore preset {} with background {} is enabled.", name, id);
        } else {
            log.info("Ore preset {} is enabled.", name);
        }
        TextureHandler.generateOverlays(ore);
    }

    public static Optional<StonePreset> loadStone(final String path) {
        throw new UnsupportedOperationException();
    }

    public static Map<String, OrePreset> getOres() {
        return ImmutableMap.copyOf(CTX.outputOres);
    }

    public static Map<String, StonePreset> getStones() {
        return ImmutableMap.copyOf(CTX.outputStones);
    }

    public static void reloadOres() {
        synchronized (CTX) {
            CTX.outputOres.clear();
            collectPresets(ModFolders.ORE_DIR).forEach((name, file) -> {
                try {
                    OrePreset.fromFile(file).ifPresent(ore -> CTX.outputOres.put(name, ore));
                } catch (final PresetLoadException ignored) {}
            });
        }
    }

    public static void reloadStones() {
        synchronized (CTX) {
            CTX.outputStones.clear();
            collectPresets(ModFolders.STONE_DIR).forEach((name, file) -> {
                try {
                    StonePreset.fromFile(file).ifPresent(stone -> CTX.outputStones.put(name, stone));
                } catch (final PresetLoadException ignored) {}
            });
        }
    }

    private static class Context {
        final Map<String, OrePreset> outputOres = new HashMap<>();
        final Map<String, StonePreset> outputStones = new HashMap<>();
    }
}
