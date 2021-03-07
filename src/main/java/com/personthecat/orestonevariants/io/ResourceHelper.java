package com.personthecat.orestonevariants.io;

import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FolderPack;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleReloadableResourceManager;
import org.apache.commons.io.FileUtils;
import personthecat.fresult.Result;
import personthecat.fresult.Void;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.lang.reflect.Field;

import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;

/** Utilities and data related to the resources directory. */
@Log4j2
public class ResourceHelper {

    /** The directory containing all of the generated resources for this mod. */
    public static final File DIR = new File(getOSVDir(), "resources");

    /** The actual resource pack model to be registered and handled by the game. */
    private static final IResourcePack RESOURCES = new FolderPack(DIR);

    /**
     * Registers all of the generated resources for this mod as a resource pack. It is
     * safe to call this before resources are generated and thus it should be called as
     * soon as possible.
     */
    public static void enableResourcePack() {
        log.info("Enabling resource pack.");
        synchronized (Minecraft.getInstance().getResourceManager()) {
            ((SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                .addResourcePack(RESOURCES);
        }
    }

    /**
     * Determines whether this mod's resource pack has been enabled.
     *
     * @return true, if the pack is enabled.
     */
    public static boolean resourcePackEnabled() {
        synchronized (Minecraft.getInstance().getResourceManager()) {
            return Minecraft.getInstance()
                .getResourceManager()
                .getResourcePackStream()
                .anyMatch(RESOURCES::equals);
        }
    }

    /**
     * This is a standard call to Minecraft#reloadResources which quietly suppresses the
     * deprecation warnings. The resources will not be refreshed a second time if they are
     * already being refreshed.
     *
     * The documentation points to an FMLClientHandler#refreshResources, but this method
     * seems to not exist in the current Forge version.
     */
    @SuppressWarnings("deprecation")
    public static void triggerIndiscriminateRefresh() {
        final Minecraft mc = Minecraft.getInstance();
        final Field f = ReflectionTools.getField(Minecraft.class, "field_213276_aV");
        final Object refresh = ReflectionTools.getOptionalValue(f, mc).orElse(null);
        if (refresh != null) {
            log.info("Indiscriminate refresh triggered to apply generated assets.");
            Result.of(() -> mc.reloadResources().get()).ifErr(e -> log.error("Error on refresh", e));
        } else {
            log.info("Refresh in progress. Skipping...");
        }
    }

    /**
     * Writes a string of data at the relative location inside of the resources directory.
     *
     * @param path The relative path where the data will be kept.
     * @param data The string contents of this file.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(String path, String data) {
        return writeResource(new FileSpec(() -> new ByteArrayInputStream(data.getBytes()), path));
    }

    /**
     * Writes a single file to the resources directory.
     *
     * @param spec A model containing the input stream and relative path.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(FileSpec spec) {
        final File file = new File(DIR, spec.path);
        final File parent = file.getParentFile();
        if (!(parent.exists() || parent.mkdirs())) {
            return Result.err(new IOException("Could not make directory: " + file.getParentFile()));
        }
        return Result.of(() -> FileUtils.copyInputStreamToFile(spec.is.get(), file));
    }

    /**
     * Writes an series of input streams to the disk using their respective paths.
     *
     * @param files Models containing data streams and resource locations.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResources(FileSpec... files) {
        return Result.of(() -> {
            for (FileSpec spec : files) {
                writeResource(spec).throwIfErr();
            }
        });
    }

    /**
     * Returns a file in the resources directory.
     *
     * @param path The relative path in the resources folder.
     * @return An absolute file at this location.
     */
    public static File file(String path) {
        return new File(DIR, path);
    }
}
