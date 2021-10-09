package personthecat.osv.io;

import net.minecraft.resources.ResourceLocation;

import static personthecat.catlib.util.PathUtils.extension;
import static personthecat.catlib.util.PathUtils.filename;
import static personthecat.catlib.util.PathUtils.noExtension;

public class OsvPaths {

    private static final String SHADED_AFFIX = "_shaded";
    private static final String DENSE_PREFIX = "dense_";

    public static String normalize(final String path) {
        String name = filename(path);
        if (noExtension(name).endsWith(SHADED_AFFIX)) {
            name = name.substring(0, SHADED_AFFIX.length()) + extension(path);
        }
        if (name.startsWith(DENSE_PREFIX)) {
            name = name.substring(DENSE_PREFIX.length());
        }
        return name;
    }

    public static ResourceLocation getDense(final ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), getDense(location.getPath()));
    }

    public static String getDense(final String path) {
        return normalToDense(normalize(path));
    }

    public static String normalToDense(final String normal) {
        final String name = filename(normal);
        return normal.substring(0, normal.lastIndexOf(name)) + "/" + DENSE_PREFIX + name;
    }

    public static String getShaded(final String path) {
        return normalToShaded(normalize(path));
    }

    public static String normalToShaded(final String normal) {
        final String name = filename(normal);
        return normal.substring(0, normal.lastIndexOf(name)) + "/" + noExtension(name) + SHADED_AFFIX + extension(normal);
    }
}
