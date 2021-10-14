package personthecat.osv.io;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import org.apache.commons.io.FileUtils;
import personthecat.fresult.Result;
import personthecat.fresult.Void;

import javax.annotation.CheckReturnValue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Log4j2
@UtilityClass
public class ResourceHelper {

    public static final PackResources RESOURCES = new FolderPackResources(ModFolders.RESOURCE_DIR);
    private static final boolean RESOURCES_CREATED = !ModFolders.RESOURCE_DIR.exists();

    /**
     * Writes a string of data at the relative location inside of the resources directory.
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
     * Writes a single file to the resources directory.
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
     * Returns a file in the resources directory.
     *
     * @param path The relative path in the resources folder.
     * @return An absolute file at this location.
     */
    public static File file(final String path) {
        return new File(ModFolders.RESOURCE_DIR, path);
    }

    /**
     * Returns whether the resource pack was generated when the game loaded,
     * and thus whether resources should always be dynamically generated.
     */
    public static boolean resourcesCreated() {
        return RESOURCES_CREATED;
    }
}
