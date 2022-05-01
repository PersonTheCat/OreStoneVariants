package personthecat.osv.exception;

import personthecat.osv.world.providers.OffsetHeightProvider;

public class OffsetOutOfBoundsException extends InvalidPresetArgumentException {

    public OffsetOutOfBoundsException(
            final OffsetHeightProvider provider, final int min, final int max, final int lower, final int upper) {
        super("Out of bounds: " + provider + " (" + min + "," + max + ") actual: (" + lower + "," + upper + ")", null);
    }
}
