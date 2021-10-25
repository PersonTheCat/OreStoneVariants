package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.PathUtils;

import java.io.File;

public class InvalidPresetArgumentException extends PresetLoadException {

    private final String msg;

    public InvalidPresetArgumentException(final File root, final File file, final String msg) {
        super(PathUtils.getRelativePath(root, file));
        this.msg = msg;
    }

    public InvalidPresetArgumentException(final File root, final File file, final Throwable cause) {
        super(PathUtils.getRelativePath(root, file), cause);
        this.msg = cause.getLocalizedMessage();
    }

    public InvalidPresetArgumentException(final String msg, final Throwable cause) {
        super(msg, cause);
        this.msg = msg;
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.getLocalizedMessage());
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TextComponent(msg);
    }

    @Override
    public @Nullable Component getDetailsPage() {
        return new TextComponent(this.readStacktrace());
    }
}
