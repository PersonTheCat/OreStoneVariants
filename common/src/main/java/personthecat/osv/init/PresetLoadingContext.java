package personthecat.osv.init;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.collections.LazyRegistry;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.serialization.json.XjsUtils;
import personthecat.osv.compat.PresetCompat;
import personthecat.osv.exception.PresetLoadException;
import personthecat.osv.io.JarFiles;
import personthecat.osv.io.ModFolders;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.util.Reference;
import personthecat.osv.util.VariantNamingService;

import java.io.File;
import java.util.*;

import static java.util.Optional.empty;
import static personthecat.catlib.util.PathUtils.extension;
import static personthecat.catlib.util.PathUtils.noExtension;

// Todo: on ore loaded: generate textures, etc
//   figure out if this ore loaded late && generated textures
//     if so, schedule refresh?

@Log4j2
public class PresetLoadingContext {

    private static final LazyRegistry<String, File> ORES =
        LazyRegistry.of(() -> collectPresets(ModFolders.ORE_DIR)).canBeReset(true);

    private static final LazyRegistry<String, File> STONE =
        LazyRegistry.of(() -> collectPresets(ModFolders.STONE_DIR)).canBeReset(true);

    private static final Context CTX = new Context();

    private PresetLoadingContext() {}

    public static void runTransformations() {
        ORES.forEach(file -> XjsUtils.readSuppressing(file).ifPresent(json ->
            PresetCompat.transformOrePreset(file, json)));
        STONE.forEach(file -> XjsUtils.readSuppressing(file).ifPresent(json ->
            PresetCompat.transformStonePreset(file, json)));
    }

    private static Map<String, File> collectPresets(final File dir) {
        final Map<String, File> files = new HashMap<>();
        for (final File file : FileIO.listFilesRecursive(dir)) {
            if (isPreset(file)) {
                files.put(noExtension(file), file);
            }
        }
        return files;
    }

    public static boolean isPreset(final File file) {
        return !JarFiles.isSpecialFile(file.getName())
            && Reference.VALID_EXTENSIONS.contains(extension(file));
    }

    public static Optional<OrePreset> loadOre(final String path) {
        if (CTX.availableOres.contains(path)) {
            return Optional.ofNullable(CTX.outputOres.get(path));
        }
        synchronized (CTX) {
            final Optional<OrePreset> ore = createOre(path);
            CTX.availableOres.add(path);
            ore.ifPresent(o -> CTX.outputOres.put(path, o));
            return ore;
        }
    }

    private static Optional<OrePreset> createOre(final String path) {
        final ResourceLocation asId = new ResourceLocation(path);
        final String normalized = VariantNamingService.formatFg(asId);
        final File file = ORES.get(normalized);

        final Optional<OrePreset> ore;
        if (file != null) {
            ore = createFromFile(file);
        } else {
            ore = Optional.of(OrePreset.createDynamic(asId, normalized));
        }
        ore.ifPresent(o -> onOreCreated(file, path, o));
        return ore;
    }

    private static Optional<OrePreset> createFromFile(final File file) {
        try {
            return OrePreset.fromFile(file);
        } catch (final PresetLoadException e) {
            LibErrorContext.error(Reference.MOD, e);
        }
        return empty();
    }

    private static void onOreCreated(@Nullable final File file, final String name, final OrePreset ore) {
        final ResourceLocation id = ore.getOreId();
        if (file == null) {
            log.info("Ore preset {} is loading dynamically.", name);
        } else if (id != null) {
            log.info("Ore preset {} is enabled with background {}.", name, id);
        } else {
            log.info("Ore preset {} is enabled.", name);
        }
    }

    public static Map<String, OrePreset> getOres() {
        return ImmutableMap.copyOf(CTX.outputOres);
    }

    public static Map<String, StonePreset> getStones() {
        if (CTX.outputStones.isEmpty()) loadStones();
        return ImmutableMap.copyOf(CTX.outputStones);
    }

    public static void loadStones() {
        synchronized (CTX) {
            STONE.reload().forEach((name, file) -> {
                try {
                    StonePreset.fromFile(file).ifPresent(stone -> CTX.outputStones.put(name, stone));
                } catch (final PresetLoadException e) {
                    LibErrorContext.error(Reference.MOD, e);
                }
            });
        }
    }

    public static void reloadOres() {
        synchronized (CTX) {
            CTX.outputOres.clear();
            ORES.reload().forEach((name, file) -> {
                try {
                    OrePreset.fromFile(file).ifPresent(ore -> CTX.outputOres.put(name, ore));
                } catch (final PresetLoadException ignored) {}
            });
        }
    }

    public static void reloadStones() {
        synchronized (CTX) {
            CTX.outputStones.clear();
            STONE.reload().forEach((name, file) -> {
                try {
                    StonePreset.fromFile(file).ifPresent(stone -> CTX.outputStones.put(name, stone));
                } catch (final PresetLoadException ignored) {}
            });
        }
    }

    private static class Context {
        final Set<String> availableOres = new HashSet<>();
        final Map<String, OrePreset> outputOres = new HashMap<>();
        final Map<String, StonePreset> outputStones = new HashMap<>();
    }
}
