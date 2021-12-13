package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import personthecat.catlib.exception.FormattedException;

public class ConfigFileNotLoadedException extends FormattedException {
    public ConfigFileNotLoadedException(final Throwable cause) {
        super(cause);
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TranslatableComponent("osv.errorText.noConfigFile");
    }
}
