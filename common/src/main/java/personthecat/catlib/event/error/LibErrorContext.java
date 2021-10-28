package personthecat.catlib.event.error;

import lombok.extern.log4j.Log4j2;
import personthecat.catlib.exception.FormattedException;

@Log4j2
public class LibErrorContext {

    public static void registerSingle(final String modName, final FormattedException e) {
        registerSingle(Severity.ERROR, modName, e);
    }

    public static void registerSingle(final Severity level, final String modName, final FormattedException e) {
        log.error("Graphical errors not yet supported. Throwing...");
        throw new RuntimeException(e);
    }
}
