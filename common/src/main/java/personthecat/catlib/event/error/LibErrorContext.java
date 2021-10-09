package personthecat.catlib.event.error;

import personthecat.catlib.exception.FormattedException;

public class LibErrorContext {

    public static void registerSingle(final String modName, final FormattedException e) {
        registerSingle(Severity.ERROR, modName, e);
    }

    public static void registerSingle(final Severity level, final String modName, final FormattedException e) {
        throw new UnsupportedOperationException();
    }
}
