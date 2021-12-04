package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.PathUtils;
import personthecat.catlib.util.SyntaxLinter;

import java.io.File;

public class PresetSyntaxException extends PresetLoadException {

    private final String text;

    public PresetSyntaxException(final File root, final File file, final String text, final Throwable cause) {
        super(PathUtils.getRelativePath(root, file), cause);
        this.text = text;
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.getLocalizedMessage());
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TextComponent(this.getCause().getLocalizedMessage());
    }

    @Override
    public @NotNull Component getDetailMessage() {
        return SyntaxLinter.DEFAULT_LINTER.lint(this.text);
    }
}
