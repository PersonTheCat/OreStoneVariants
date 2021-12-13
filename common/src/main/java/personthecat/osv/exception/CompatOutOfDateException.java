package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.exception.FormattedException;

public class CompatOutOfDateException extends FormattedException {

    private final String mod;

    public CompatOutOfDateException(final String mod, final Throwable cause) {
        super(cause);
        this.mod = mod;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.compat";
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TranslatableComponent("osv.errorText.outOfDate", this.mod);
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.mod);
    }
}
