package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.exception.FormattedException;

public class InvalidBlockEntryException extends FormattedException {

    private final String raw;

    public InvalidBlockEntryException(final String raw) {
        super("Could not parse block entry: " + raw);
        this.raw = raw;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.blockList";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.raw);
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return new TranslatableComponent("osv.errorText.couldNotParse", this.raw);
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TranslatableComponent("osv.errorText.invalidFormat");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return new TranslatableComponent("osv.errorText.blockListTutorial");
    }
}
