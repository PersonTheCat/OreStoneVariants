package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PresetGenerationException extends PresetSyntaxException {

    public PresetGenerationException(final File root, final File file, final String text, final Throwable cause) {
        super(root, file, text, cause);
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return new TranslatableComponent("osv.errorText.presetNotGenerated", this.name);
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TranslatableComponent("osv.errorText.xPresetNotGenerated");
    }
}
