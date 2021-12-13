package personthecat.osv.compat;

import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.util.McUtils;
import personthecat.osv.exception.CompatOutOfDateException;
import personthecat.osv.util.Reference;

class CompatLoader {
    static void runChecked(final String mod, final Runnable f) {
        if (McUtils.isModLoaded(mod)) {
            try {
                f.run();
            } catch (final LinkageError e) {
                LibErrorContext.error(Reference.MOD, new CompatOutOfDateException(mod, e));
            }
        }
    }
}
