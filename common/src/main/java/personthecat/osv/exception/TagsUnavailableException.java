package personthecat.osv.exception;

import net.minecraft.resources.ResourceLocation;

public class TagsUnavailableException extends RuntimeException {
    public TagsUnavailableException(final ResourceLocation missingRegistry) {
        super("Unable to inject tags for registry: " + missingRegistry);
    }
}
