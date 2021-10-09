package personthecat.catlib.exception;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class FormattedException extends Exception {

    public FormattedException(final String msg) {
        super(msg);
    }

    public FormattedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public FormattedException(final Throwable cause) {
        super(cause);
    }

    @NotNull
    public abstract Component getDisplayMessage();

    @Nullable
    public abstract Component getTooltip();

    @Nullable
    public abstract Component getDetailsPage();

    protected String readStacktrace() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        this.getCause().printStackTrace(pw);
        return sw.toString();
    }
}
