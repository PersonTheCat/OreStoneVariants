package personthecat.osv.config;

import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.data.collections.MultiValueHashMap;
import personthecat.catlib.data.collections.MultiValueMap;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.exception.GenericFormattedException;
import personthecat.osv.ModRegistries;
import personthecat.osv.exception.DuplicateBlockEntryException;
import personthecat.osv.exception.InvalidBlockEntryException;
import personthecat.osv.util.Group;
import personthecat.osv.util.Reference;

import java.util.*;

public class BlockList {

    public static Set<BlockEntry> get() {
        return parseEntries(Cfg.blockEntries());
    }

    public static Set<BlockEntry> parseEntries(final List<String> raw) {
        final Set<BlockEntry> entries = new HashSet<>();
        for (final String entry : raw) {
            entries.add(parseEntry(entry));
        }
        return entries;
    }

    public static Map<BlockEntry, List<VariantDescriptor>> loadEntries() {
        final Map<BlockEntry, List<VariantDescriptor>> entries = mapEntries(Cfg.blockEntries());
        if (Cfg.checkForDuplicates()) {
            final Map<VariantDescriptor, Set<BlockEntry>> duplicates = getDuplicates(entries);
            if (!duplicates.isEmpty()) {
                LibErrorContext.error(Reference.MOD, new DuplicateBlockEntryException(duplicates));
            }
        }
        return entries;
    }

    public static Map<BlockEntry, List<VariantDescriptor>> mapEntries(final List<String> raw) {
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
            LibErrorContext.error(Reference.MOD, e);
            return null;
        }
    }

    private static void tryPutEntry(final Map<BlockEntry, List<VariantDescriptor>> map, final BlockEntry entry) {
        try {
            map.put(entry, entry.resolve());
        } catch (final RuntimeException e) {
            LibErrorContext.error(Reference.MOD, new GenericFormattedException(e));
        }
    }

    public static Set<BlockEntry> create(final Collection<Group> ores, final Collection<Group> bgs) {
        final Set<BlockEntry> entries = new HashSet<>();
        for (final Group ore : ores) {
            for (final Group bg : bgs) {
                if (bg.isSynthetic()) {
                    entries.add(new BlockEntry(ore.getName(), bg.asId().toString()));
                } else {
                    entries.add(new BlockEntry(ore.getName(), bg.getName()));
                }
            }
        }
        return entries;
    }

    public static Map<BlockEntry, List<VariantDescriptor>> resolve(final Collection<BlockEntry> entries) {
        final Map<BlockEntry, List<VariantDescriptor>> map = new HashMap<>();
        for (final BlockEntry entry : entries) {
            tryPutEntry(map, entry);
        }
        return map;
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

    public static List<String> intoRaw(final Map<BlockEntry, List<VariantDescriptor>> entries) {
        return intoRaw(entries.keySet());
    }

    public static List<String> intoRaw(final Collection<BlockEntry> entries) {
        final List<String> raw = new ArrayList<>();
        for (final BlockEntry entry : entries) {
            raw.add(entry.getRaw());
        }
        return raw;
    }

    public static Set<BlockEntry> optimize(final Map<BlockEntry, List<VariantDescriptor>> entries) {
        return optimize(entries.keySet());
    }

    public static Set<BlockEntry> optimize(final Set<BlockEntry> entries) {
        final Set<BlockEntry> deconstructed = deconstruct(entries);
        final MultiValueMap<String, String> presetMap = mapPresets(deconstructed);
        presetMap.forEach((preset, blocks) -> simplifyBlocks(blocks));
        final MultiValueMap<String, String> blockMap = mapBlocks(presetMap);
        blockMap.forEach((block, presets) -> simplifyPresets(presets));
        return reconstruct(blockMap);
    }

    public static Set<BlockEntry> deconstruct(final Set<BlockEntry> entries) {
        final Set<BlockEntry> all = new HashSet<>();
        for (final BlockEntry entry : entries) {
            all.addAll(entry.deconstruct());
        }
        return all;
    }

    private static MultiValueMap<String, String> mapPresets(final Set<BlockEntry> deconstructed) {
        final MultiValueMap<String, String> presets = new MultiValueHashMap<>();
        for (final BlockEntry entry : deconstructed) {
            presets.add(entry.getForeground(), entry.getBackground());
        }
        return presets;
    }

    private static void simplifyBlocks(final List<String> blocks) {
        for (final Group group : ModRegistries.BLOCK_GROUPS) {
            if (!group.isMetaGroup()) {
                substitute(blocks, group.map(id -> new ResourceLocation(id).toString()), group.getName());
            }
        }
        if (!substitute(blocks, getAllGroups(ModRegistries.BLOCK_GROUPS), Group.ALL)) {
            substitute(blocks, getDefaultGroups(DefaultStones.LISTED), Group.DEFAULT);
        }
    }

    private static MultiValueMap<String, String> mapBlocks(final MultiValueMap<String, String> presetMap) {
        final MultiValueMap<String, String> blockMap = new MultiValueHashMap<>();
        presetMap.forEachInner((preset, block) -> blockMap.add(block, preset));
        return blockMap;
    }

    private static void simplifyPresets(final List<String> presets) {
        for (final Group group : ModRegistries.PROPERTY_GROUPS) {
            if (!group.isMetaGroup()) {
                substitute(presets, group.getEntries(), group.getName());
            }
        }
        if (!substitute(presets, getAllGroups(ModRegistries.PROPERTY_GROUPS), Group.ALL)) {
            substitute(presets, getDefaultGroups(DefaultOres.LISTED), Group.DEFAULT);
        }
    }

    private static boolean substitute(final Collection<String> entries, final Collection<String> from, final String to) {
        if (entries.containsAll(from)) {
            entries.removeAll(from);
            entries.add(to);
            return true;
        }
        return false;
    }

    private static Set<String> getAllGroups(final Map<String, Group> groups) {
        final Set<String> allGroups = new HashSet<>(groups.keySet());
        allGroups.remove(Group.ALL);
        allGroups.remove(Group.DEFAULT);
        return allGroups;
    }

    private static Set<String> getDefaultGroups(final Group[] groups) {
        final Set<String> defaultGroups = new HashSet<>();
        for (final Group group : groups) {
            defaultGroups.add(group.getName());
        }
        return defaultGroups;
    }

    private static Set<BlockEntry> reconstruct(final MultiValueMap<String, String> blockMap) {
        final Set<BlockEntry> entries = new HashSet<>();
        blockMap.forEachInner((block, preset) -> entries.add(new BlockEntry(preset, block)));
        return entries;
    }
}
