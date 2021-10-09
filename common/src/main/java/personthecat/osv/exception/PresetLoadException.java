package personthecat.osv.exception;

import personthecat.catlib.exception.FormattedException;

public abstract class PresetLoadException extends FormattedException {
    public PresetLoadException(final String msg) {
        super(msg);
    }

    public PresetLoadException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
