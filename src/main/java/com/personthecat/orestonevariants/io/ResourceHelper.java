package com.personthecat.orestonevariants.io;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.*;
import org.apache.commons.io.FileUtils;
import personthecat.fresult.Result;
import personthecat.fresult.Void;

import javax.annotation.CheckReturnValue;
import java.io.*;

import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;

/** Utilities and data related to the resources directory. */
@Log4j2
public class ResourceHelper {

    /** The directory containing all of the generated resources for this mod. */
    public static final File DIR = new File(getOSVDir(), "resources");

    /** The actual resource pack model to be registered and handled by the game. */
    public static final IResourcePack RESOURCES = new FolderPack(DIR);

    /** Whether the resources were freshly generated when the game loaded. */
    private static final boolean RESOURCES_CREATED = !DIR.exists();

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

    /**
     * Returns whether the resource pack was generated when the game loaded,
     * and thus whether resources should always be dynamically generated.
     */
    public static boolean resourcesCreated() {
        return RESOURCES_CREATED;
    }
}
