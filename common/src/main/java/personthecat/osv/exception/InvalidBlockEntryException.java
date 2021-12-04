package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(raw);
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TextComponent("Invalid format");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return new TextComponent("Alright, now let me tell you how to use this thing."); // Todo: display instructions on using the block list
    }
}
