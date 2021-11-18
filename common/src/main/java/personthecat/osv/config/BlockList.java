package personthecat.osv.config;

import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.error.Severity;
import personthecat.catlib.exception.GenericFormattedException;
import personthecat.osv.exception.DuplicateBlockEntryException;
import personthecat.osv.exception.InvalidBlockEntryException;
import personthecat.osv.util.Reference;

import java.util.*;

public class BlockList {

    public static Map<BlockEntry, List<VariantDescriptor>> loadEntries() {
        final Map<BlockEntry, List<VariantDescriptor>> entries = parseEntries(Cfg.blockEntries());
        if (Cfg.checkForDuplicates()) {
            final Map<VariantDescriptor, Set<BlockEntry>> duplicates = getDuplicates(entries);
            if (!duplicates.isEmpty()) {
                LibErrorContext.registerSingle(Severity.WARN, Reference.MOD_NAME,
                    new DuplicateBlockEntryException(duplicates));
            }
        }
        return entries;
    }

    public static Map<BlockEntry, List<VariantDescriptor>> parseEntries(final List<String> raw) {
        final Map<BlockEntry, List<VariantDescriptor>> entries = new HashMap<>();
        for (final String e : raw) {
            final BlockEntry entry = parseEntry(e);
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

    public static Map<VariantDescriptor, Set<BlockEntry>> getDuplicates(final Map<BlockEntry, List<VariantDescriptor>> map) {
        final Map<VariantDescriptor, Set<BlockEntry>> duplicates = new HashMap<>();
        for (final Map.Entry<BlockEntry, List<VariantDescriptor>> e1 : map.entrySet()) {
            for (final Map.Entry<BlockEntry, List<VariantDescriptor>> e2 : map.entrySet()) {
                if (e1.getKey() == e2.getKey()) continue;

                for (final VariantDescriptor d1 : e1.getValue()) {
                    for (final VariantDescriptor d2 : e2.getValue()) {
                        if (d1 != d2 && d1.equals(d2)) {
                            final Set<BlockEntry> entries = duplicates.computeIfAbsent(d1, d -> new HashSet<>());
                            entries.add(e1.getKey());
                            entries.add(e2.getKey());
                        }
                    }
                }
            }
        }
        return duplicates;
    }
}
