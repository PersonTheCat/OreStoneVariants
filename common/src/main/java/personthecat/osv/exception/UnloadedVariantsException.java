package personthecat.osv.exception;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import personthecat.catlib.exception.FormattedException;
import personthecat.osv.config.BlockEntry;
import personthecat.osv.config.VariantDescriptor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UnloadedVariantsException extends FormattedException {

    private final Collection<VariantDescriptor> descriptors;

    public UnloadedVariantsException(final Collection<VariantDescriptor> descriptors) {
        super("Unable to load " + descriptors.size() + " variants.");
        this.descriptors = descriptors;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.blockList";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TranslatableComponent("osv.errorText.unloadedVariants", this.descriptors.size());
    }

    @Override
    public @NotNull Component getDetailMessage() {
        final TextComponent newLine = new TextComponent("\n");

        final MutableComponent component = new TextComponent("");
        component.append(new TranslatableComponent("osv.errorText.entriesAffected")
            .withStyle(Style.EMPTY.applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.BOLD)));
        component.append(newLine);
        component.append(newLine);

        for (final BlockEntry entry : this.resolveEntries()) {
            component.append(new TextComponent(" * ").withStyle(Style.EMPTY.withBold(true)));
            component.append(new TextComponent(entry.getRaw()).withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));
            component.append(newLine);
        }
        component.append(newLine);

        component.append(new TranslatableComponent("osv.errorText.variantsWillNotLoad")
            .withStyle(Style.EMPTY.applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.BOLD)));
        component.append(newLine);
        component.append(newLine);

        for (final VariantDescriptor descriptor : this.descriptors) {
            final HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new TranslatableComponent("osv.errorText.variantAddedBy", descriptor.getEntry().getRaw()));

            component.append(new TextComponent(" * ").withStyle(Style.EMPTY.withBold(true)));
            component.append(new TextComponent(descriptor.getId().toString())
                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withHoverEvent(hover)));
            component.append(newLine);
        }
        return component;
    }

    private Set<BlockEntry> resolveEntries() {
        final Set<BlockEntry> entries = new HashSet<>();
        this.descriptors.forEach(d -> entries.add(d.getEntry()));
        return entries;
    }

    @Override
    public void onErrorReceived(final Logger log) {
        log.error(this.getMessage());
        this.descriptors.forEach(log::warn);
    }
}
