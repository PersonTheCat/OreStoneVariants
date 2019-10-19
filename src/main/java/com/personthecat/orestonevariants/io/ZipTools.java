package com.personthecat.orestonevariants.io;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.util.unsafe.Result;
import com.personthecat.orestonevariants.util.unsafe.Void;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static com.personthecat.orestonevariants.io.SafeFileIO.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class ZipTools {
    /** The directory containing this mod's zip files. */
    private static final String DIR = f("{}/{}/", FMLPaths.CONFIGDIR.get(), Main.MODID);
    /** The name of this mod's resource pack. */
    private static final String NAME = "resources.zip";
    /** The resource pack containing this mod's textures. */
    public static final File RESOURCE_PACK = new File(DIR, NAME);
    /** The internal path to the resource pack. */
    private static final String RP_JAR_PATH = "/assets/ore_stone_variants/resources.zip";

    /** Tests for the base resource pack and copies it from the jar, if absent. */
    public static void copyResourcePack() {
        if (!RESOURCE_PACK.exists()) {
            mkdirs(RESOURCE_PACK.getParentFile())
                .expect("Unable to create ./config/ore_stone_variants/.");
            copyStream(getRequiredResource(RP_JAR_PATH), RESOURCE_PACK.getPath())
                .expect("Unable to copy resource pack from the jar.");
        }
    }

    /** Generates an empty zip file at the location of `zip`. */
    public static Result<Void, IOException> createEmptyZip(File zip) {
        if (!zip.exists()) {
            return Result.of(() -> {
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
                zos.close();
            });
        }
        return Result.ok();
    }

    /** Determines whether the input path is present in this zip. */
    public static boolean fileInZip(File zip, String path) {
        return Result.with(() -> new ZipFile(zip),
            zipFile -> zipFile.getEntry(path) != null)
            .orElse(false);
    }

    /** Retrieves a BufferedImage from the input zip file. */
    public static Optional<BufferedImage> getImage(File zip, String path) {
        if (fileInZip(zip, path)) {
            return Result.of(() -> {
                ZipFile zipFile = new ZipFile(zip);
                ZipEntry entry = zipFile.getEntry(path);
                BufferedImage image = ImageIO.read(zipFile.getInputStream(entry));
                zipFile.close();
                return image;
            }).get(Result::IGNORE);
        }
        return empty();
    }

    /** Copies a file into the mod's resource pack. */
    public static Result<Void, IOException> copyToResources(File file, String path) {
        return copyToZip(RESOURCE_PACK, file, path);
    }

    /** Convenience variant of copyToZip(). */
    public static Result<Void, IOException> copyToZip(File zip, File file, String path) {
        return copyToZip(zip, file, path, false);
    }

    /** Adds a file to the input zip without removing its original contents. */
    public static Result<Void, IOException> copyToZip(File zip, File file, String path, boolean allowReplace) {
        if (!allowReplace && fileInZip(zip, path)) {
            // The file already exists and should not be replaced -> stop.
            return Result.ok();
        }
        return Result.of(() -> {
            // Move the original file to a temporary location.
            File tmp = File.createTempFile("osv_", ".zip");
            Files.move(zip.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Create a new zip in the original location.
            ZipFile tmpZip = new ZipFile(tmp);
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));

            // Copy the original contents.
            Collections.list(tmpZip.entries()).forEach(entry -> {
                if (!(allowReplace && path.equals(entry.getName()))) {
                    moveEntry(tmpZip, zos, entry) // Don't allow a memory leak if this fails.
                        .expect("Unrecoverable error when copying file to zip.");
                }
            });

            // Copy the new file.
            moveToZip(new FileInputStream(file), zos, new ZipEntry(path));

            // Clean up.
            zos.close();
            tmpZip.close();
            tmp.delete();
        });
    }

    /** Copies a zip entry between two zip files. */
    private static Result<Void, IOException> moveEntry(ZipFile from, ZipOutputStream to, ZipEntry entry) {
        return Result.of(() -> moveToZip(from.getInputStream(entry), to, entry));
    }

    /** Handles the one-time operation of copying a file into a ZipOutputStream. */
    private static void moveToZip(InputStream is, ZipOutputStream zos, ZipEntry entry) throws IOException {
        zos.putNextEntry(entry);
        copyStream(is, zos, 1024).throwIfErr();
        is.close();
    }
}