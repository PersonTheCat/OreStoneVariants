package personthecat.osv.util;

import net.minecraft.resources.ResourceLocation;
import personthecat.osv.util.Reference;

public class VariantNamingService {

    private static final String MINECRAFT = "minecraft";
    private static final String STONE = "stone";

    public static ResourceLocation create(final String foreground, final ResourceLocation background) {
        final String bgFormat = formatId(background);
        if (bgFormat.isEmpty()) {
            return new ResourceLocation(Reference.MOD_ID, foreground);
        }
        return new ResourceLocation(Reference.MOD_ID, foreground + "_" + bgFormat);
    }

    public static String formatId(final ResourceLocation id) {
        if (MINECRAFT.equals(id.getNamespace())) {
            return STONE.equals(id.getPath()) ? "" : id.getPath();
        }
        if (STONE.equals(id.getPath())) {
            return id.getNamespace();
        }
        return id.getNamespace() + "_" + id.getPath();
    }
}
