package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.exception.FormattedException;
import personthecat.osv.config.BlockEntry;
import personthecat.osv.config.VariantDescriptor;

import java.util.Map;
import java.util.Set;

public class DuplicateBlockEntryException extends FormattedException {

    private final Map<VariantDescriptor, Set<BlockEntry>> duplicates;

    public DuplicateBlockEntryException(final Map<VariantDescriptor, Set<BlockEntry>> duplicates) {
        super("Discovered " + duplicates.size() + " duplicate block entries");
        this.duplicates = duplicates;
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent("TBD");
    }
}
