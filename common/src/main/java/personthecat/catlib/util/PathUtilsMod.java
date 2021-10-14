package personthecat.catlib.util;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static personthecat.catlib.util.Shorthand.f;

public class PathUtilsMod {

    public static String asTexturePath(final ResourceLocation id) {
        return asTexturePath(id.getNamespace(), id.getPath());
    }

    public static String asTexturePath(final String mod, final String path) {
        return f("/assets/{}/textures/{}.png", mod, path);
    }

    public static String asModelPath(final ResourceLocation id) {
        return asModelPath(id.getNamespace(), id.getPath());
    }

    public static String asModelPath(final String mod, final String path) {
        return f("/assets/{}/models/{}.json", mod, path);
    }

    public static String asBlockStatePath(final ResourceLocation id) {
        return asBlockStatePath(id.getNamespace(), id.getPath());
    }

    public static String asBlockStatePath(final String mod, final String path) {
        return f("/assets/{}/blockstates/{}.json", mod, path);
    }

    public static String namespaceToSub(final String path) {
        return PathUtils.namespaceToSub(fromRawPath(path));
    }

    public static ResourceLocation fromRawPath(final String path) {
        final int content = contentRoot(path);
        if (content < 0) return new ResourceLocation(path);
        final String fromMod = path.substring(content + 1);

        final int mod = fromMod.indexOf("/");
        if (mod < 0) return new ResourceLocation(path);
        final String modName = fromMod.substring(0, mod);
        final String fullPath = fromMod.substring(mod + 1);

        final int key = fullPath.indexOf("/");
        if (key < 0) return new ResourceLocation(modName, fullPath + 1);

        return new ResourceLocation(modName, PathUtils.noExtension(fullPath.substring(key + 1)));
    }

    @Nullable
    public static String contentType(final String path) {
        final int content = contentRoot(path);
        if (content < 0) {
            // Assume this path starts just after the mod.
            final int afterMod = path.indexOf("/");
            if (afterMod < 0) return null; // Only one path element.
            return path.substring(0, afterMod + 1);
        }
        final String fromMod = path.substring(content + 1);

        final int mod = fromMod.indexOf("/");
        if (mod < 0) return null; // Would be "assets/mod"
        final String fullPath = fromMod.substring(mod + 1);

        final int fromType = fullPath.indexOf("/");
        if (fromType < 0) return null; // Would be "assets/mod/file"

        return fullPath.substring(0, fromType);
    }

    private static int contentRoot(final String path) {
        final int assets = path.indexOf("assets/");
        if (assets > 0) {
            return assets + "assets".length();
        }
        final int data = path.indexOf("data/");
        if (data > 0) {
            return data + "data".length();
        }
        return -1;
    }
}
