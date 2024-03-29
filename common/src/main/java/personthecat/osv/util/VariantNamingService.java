package personthecat.osv.util;

import net.minecraft.resources.ResourceLocation;

public class VariantNamingService {

    private static final String MINECRAFT = "minecraft";
    private static final String STONE = "stone";

    public static ResourceLocation create(final String foreground, final ResourceLocation background) {
        final String fgFormat = formatFg(foreground);
        final String bgFormat = formatBg(background);
        if (bgFormat.isEmpty()) {
            return new ResourceLocation(Reference.MOD_ID, fgFormat);
        }
        return new ResourceLocation(Reference.MOD_ID, fgFormat + "_" + bgFormat);
    }

    public static String formatFg(final String foreground) {
        return formatFg(new ResourceLocation(foreground));
    }

    public static String formatFg(final ResourceLocation foreground) {
        if (MINECRAFT.equals(foreground.getNamespace())) {
            return foreground.getPath();
        }
        return foreground.getNamespace() + "_" + foreground.getPath();
    }

    public static String formatBg(final ResourceLocation id) {
        if (MINECRAFT.equals(id.getNamespace())) {
            return STONE.equals(id.getPath()) ? "" : id.getPath();
        }
        if (STONE.equals(id.getPath())) {
            return id.getNamespace();
        }
        return id.getNamespace() + "_" + id.getPath();
    }
}
