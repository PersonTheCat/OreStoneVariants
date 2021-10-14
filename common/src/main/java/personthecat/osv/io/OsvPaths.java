package personthecat.osv.io;

import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.util.PathUtils;
import personthecat.catlib.util.PathUtilsMod;
import personthecat.osv.util.Reference;

import static personthecat.catlib.util.PathUtils.extension;
import static personthecat.catlib.util.PathUtils.filename;
import static personthecat.catlib.util.PathUtils.noExtension;

public class OsvPaths {

    private static final String SHADED_AFFIX = "_shaded";
    private static final String DENSE_PREFIX = "dense_";

    public static String normalize(final String path) {
        String name = filename(path);
        final int pathIndex = path.indexOf(name);
        if (noExtension(name).endsWith(SHADED_AFFIX)) {
            name = name.substring(0, SHADED_AFFIX.length()) + "." + extension(path);
        }
        if (name.startsWith(DENSE_PREFIX)) {
            name = name.substring(DENSE_PREFIX.length());
        }
        return pathIndex < 0 ? name : path.substring(0, pathIndex) + name;
    }

    public static ResourceLocation getDense(final ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), getDense(location.getPath()));
    }

    public static String getDense(final String path) {
        return normalToDense(normalize(path));
    }

    public static String normalToDense(final String normal) {
        final String name = filename(normal);
        final String path = normal.substring(0, normal.lastIndexOf(name));
        return path + DENSE_PREFIX + name;
    }

    public static String getShaded(final String path) {
        return normalToShaded(normalize(path));
    }

    public static String normalToShaded(final String normal) {
        final String name = filename(normal);
        final String path = normal.substring(0, normal.lastIndexOf(name));
        return path + noExtension(name) + SHADED_AFFIX + "." + extension(normal);
    }

    public static String toOsvTexturePath(final String path) {
        final String subbed = PathUtilsMod.namespaceToSub(path);
        return PathUtilsMod.asTexturePath(Reference.MOD_ID, subbed);
    }

    public static ResourceLocation toOsvTextureId(final ResourceLocation id) {
        final String subbed = PathUtils.namespaceToSub(id);
        return new ResourceLocation(Reference.MOD_ID, subbed);
    }
}
