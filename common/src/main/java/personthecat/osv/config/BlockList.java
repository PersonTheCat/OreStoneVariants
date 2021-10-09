package personthecat.osv.config;

import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.exception.GenericFormattedException;
import personthecat.osv.exception.InvalidBlockEntryException;
import personthecat.osv.util.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockList {

    public static Map<BlockEntry, List<VariantDescriptor>> loadEntries() {
        final Map<BlockEntry, List<VariantDescriptor>> entries = new HashMap<>();
        for (final String raw : Cfg.blockEntries()) {
            final BlockEntry entry = parseEntry(raw);
            if (entry != null) {
                tryPutEntry(entries, entry);
            }
        }
        return entries;
    }

    private static BlockEntry parseEntry(final String raw) {
        try {
            return BlockEntry.create(raw);
        } catch (final InvalidBlockEntryException e) {
            LibErrorContext.registerSingle(Reference.MOD_NAME, e);
            return null;
        }
    }

    private static void tryPutEntry(final Map<BlockEntry, List<VariantDescriptor>> map, final BlockEntry entry) {
        try {
            map.put(entry, entry.resolve());
        } catch (final RuntimeException e) {
            LibErrorContext.registerSingle(Reference.MOD_NAME, new GenericFormattedException(e));
        }
    }
}
