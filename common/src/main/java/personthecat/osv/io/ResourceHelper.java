package personthecat.osv.io;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import org.apache.commons.io.FileUtils;
import personthecat.catlib.io.FileIO;
import personthecat.fresult.Result;
import personthecat.fresult.Void;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;

@Log4j2
@UtilityClass
public class ResourceHelper {

    public static final PackResources RESOURCES = new FolderPackResources(ModFolders.RESOURCE_DIR);
    private static final boolean RESOURCES_CREATED = !ModFolders.RESOURCE_DIR.exists();

    /**
     * Writes a string of data at the relative location in the /resources directory.
     *
     * @param path The relative path where the data will be kept.
     * @param data The string contents of this file.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(final String path, final String data) {
        return writeResource(new FileSpec(() -> new ByteArrayInputStream(data.getBytes()), path));
    }

    /**
     * Writes <b>any</b> stream of data at the relative location in the /resources directory.
     *
     * @param path The relative path where the data will be kept.
     * @param data The raw contents of this file.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(final String path, final InputStream data) {
        return writeResource(new FileSpec(() -> data, path));
    }

    /**
     * Writes a single file to the /resources directory.
     *
     * @param spec A model containing the input stream and relative path.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResource(final FileSpec spec) {
        final File file = new File(ModFolders.RESOURCE_DIR, spec.path);
        if (!ModFolders.RESOURCE_DIR.exists() || ModFolders.RESOURCE_DIR.mkdirs()) {
            return Result.err(new IOException("Could not make directory: " + ModFolders.RESOURCE_DIR));
        }
        return Result.of(() -> FileUtils.copyInputStreamToFile(spec.is.get(), file)).ifErr(Result::WARN);
    }

    /**
     * Writes a series of input streams to the disk using their respective paths.
     *
     * <p>Note that if any file fails to serialize, the following files will be ignored.
     *
     * @param files Models containing data streams and resource locations.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResources(final Collection<FileSpec> files) {
        return Result.of(() -> {
            for (final FileSpec spec : files) {
                writeResource(spec).throwIfErr();
            }
        }).ifErr(Result::WARN);
    }

    /**
     * Variant of {@link #writeResources(Collection)} which operates from an array.
     *
     * @param files Models containing data streams and resource locations.
     * @return The result of this operation, wrapping a potential error.
     */
    @CheckReturnValue
    public static Result<Void, IOException> writeResources(final FileSpec... files) {
        return Result.of(() -> {
            for (final FileSpec spec : files) {
                writeResource(spec).throwIfErr();
            }
        }).ifErr(Result::WARN);
    }

    /**
     * Returns whether the /resources directory contains the given path <b>or</b>
     * the resource is loaded as an asset in memory.
     *
     * @param path The <b>relative</b> path to the resource.
     * @return Whether the resource exists.
     */
    @CheckReturnValue
    public static boolean hasResource(final String path) {
        return file(path).exists() || FileIO.resourceExists(path);
    }

    /**
     * Returns a resource at the given path. If the resource does not exist in the
     * /resources directory, it will be returned from the jar file.
     *
     * @param path The relative path to the expected resource.
     * @return The resource, or else {@link Optional#empty}.
     */
    @CheckReturnValue
    public static Optional<InputStream> getResource(final String path) {
        final File file = file(path);
        if (file.exists()) {
            return Result.<InputStream>suppress(() -> new FileInputStream(file)).get();
        }
        return FileIO.getResource(path);
    }

    /**
     * Returns a file in the /resources directory.
     *
     * @param path The relative path in the /resources folder.
     * @return An absolute file at this location.
     */
    @CheckReturnValue
    public static File file(final String path) {
        return new File(ModFolders.RESOURCE_DIR, path);
    }

    /**
     * Returns whether the resource pack was generated when the game loaded,
     * and thus whether resources should always be dynamically generated.
     */
    @CheckReturnValue
    public static boolean resourcesCreated() {
        return RESOURCES_CREATED;
    }
}
