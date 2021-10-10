package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.PathUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CorruptPresetException extends PresetLoadException {

    public CorruptPresetException(final File root, final File file, final Throwable cause) {
        super(PathUtils.getRelativePath(root, file), cause);
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.getLocalizedMessage());
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TextComponent("Preset is corrupt or unreadable");
    }

    @Override
    public @Nullable Component getDetailsPage() {
        return new TextComponent(this.readStacktrace());
    }
}
