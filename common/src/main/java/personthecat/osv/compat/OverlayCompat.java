package personthecat.osv.compat;

import personthecat.catlib.data.IntRef;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.io.ZipIO;

import java.io.File;

import static personthecat.catlib.util.PathUtils.extension;

public class OverlayCompat {

    public static int renameOverlays(final File path) {
        if (isImage(path)) {
            return renameSingle(path) ? 1 : 0;
        } else if (isZip(path)) {
            return renameZip(path);
        } else if (path.isDirectory()) {
            return renameFolder(path);
        }
        return 0;
    }

    private static boolean isImage(final File f) {
        return extension(f).equalsIgnoreCase("png");
    }

    private static boolean isZip(final File f) {
        return extension(f).equalsIgnoreCase("zip");
    }

    private static boolean renameSingle(final File f) {
        final String name = getCorrectedName(f.getName());
        if (name.equals(f.getName())) {
            return false;
        }
        return FileIO.rename(f, name).isOk();
    }

    private static int renameZip(final File f) {
        final IntRef count = new IntRef(0);
        ZipIO.transform(f, entry -> {
            if (extension(entry.getName()).equalsIgnoreCase("png")) {
                final String name = getCorrectedName(entry.getName());
                if (!name.equals(entry.getName())) {
                    count.increment();
                    return entry.rename(name);
                }
            }
            return entry;
        });
        return count.get();
    }

    private static int renameFolder(final File f) {
        int count = 0;
        for (final File img : FileIO.listFilesRecursive(f, OverlayCompat::isImage)) {
            if (renameSingle(img)) count++;
        }
        return count;
    }

    private static String getCorrectedName(final String name) {
        return name.replaceFirst("dense_(.*)_ore\\.png", "$1_ore_dense.png")
            .replaceAll("(.*)_ore_overlay_shaded\\.png", "$1_ore_shade.png")
            .replaceFirst("(.*)_ore_overlay\\.png", "$1_ore.png");
    }
}
