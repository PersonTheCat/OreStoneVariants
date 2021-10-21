package personthecat.catlib.util;

public class PathUtilsMod {
    public static String appendFilename(final String path, final String affix) {
        final String extension = PathUtils.extension(path);
        if (extension.isEmpty()) {
            return path + affix;
        }
        final int index = path.lastIndexOf(extension);
        return path.substring(0, index - 1) + affix + "." + extension;
    }
}
