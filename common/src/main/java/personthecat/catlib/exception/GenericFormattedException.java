package personthecat.catlib.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

public class GenericFormattedException extends FormattedException {
    public GenericFormattedException(final Throwable cause) {
        super(cause);
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.getCause().getLocalizedMessage());
    }

    @Override
    public @Nullable Component getTooltip() {
        return null;
    }

    @Override
    public @Nullable Component getDetailsPage() {
        return new TextComponent(this.readStacktrace());
    }
}
