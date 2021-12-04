package personthecat.osv.exception;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
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
    public @NotNull String getCategory() {
        return "osv.errorMenu.blockList";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TranslatableComponent("osv.errorText.duplicateEntries");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        final TextComponent newLine = new TextComponent("\n");
        final MutableComponent component = new TextComponent("");

        component.append(new TranslatableComponent("osv.errorText.variantsDuplicated")
            .setStyle(Style.EMPTY.applyFormats(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)));
        component.append(newLine);
        component.append(newLine);

        for (final Map.Entry<VariantDescriptor, Set<BlockEntry>> kv : this.duplicates.entrySet()) {
            component.append(new TextComponent(" * ").withStyle(Style.EMPTY.withBold(true)));
            component.append(new TextComponent(kv.getKey().getId().toString())
                .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));
            component.append(newLine);

            for (final BlockEntry entry : kv.getValue()) {
                component.append(new TextComponent("   - ").withStyle(Style.EMPTY.withBold(true)));
                component.append(new TextComponent(entry.getRaw()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                component.append(newLine);
            }
            component.append(newLine);
        }
        return component;
    }
}
