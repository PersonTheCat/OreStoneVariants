package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.linting.SyntaxLinter;
import personthecat.catlib.util.PathUtils;

import java.io.File;

public class PresetSyntaxException extends PresetLoadException {

    private final String text;
    private final String name;

    public PresetSyntaxException(final File root, final File file, final String text, final Throwable cause) {
        super(PathUtils.getRelativePath(root, file), cause);
        this.text = text;
        this.name = file.getName();
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.getMessage());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return new TranslatableComponent("osv.errorText.xPresetInvalid", this.name);
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TranslatableComponent("osv.errorText.presetInvalid");
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return SyntaxLinter.DEFAULT_LINTER.lint(this.text.replace("\t", "  ").replace("\r", ""));
    }
}
