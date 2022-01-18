package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.exception.FormattedException;

public class CompatibilityModeException extends FormattedException {

    public CompatibilityModeException() {
        super("Running in compatibility mode. Some things may not work right.");
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TranslatableComponent("osv.errorText.compatibilityMode");
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TranslatableComponent("osv.errorText.interceptorUnavailable");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return new TranslatableComponent("osv.errorText.interceptorUnavailable");
    }
}
