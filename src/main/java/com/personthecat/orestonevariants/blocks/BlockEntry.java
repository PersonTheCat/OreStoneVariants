package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroups;
import com.personthecat.orestonevariants.util.Group;
import lombok.extern.log4j.Log4j2;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.isModLoaded;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

@Log4j2
public class BlockEntry {
    public final Group<ResourceLocation> blocks;
    public final Group<OreProperties> properties;
    private final String[] raw;

    private BlockEntry(String properties, String blocks) {
        this.properties = PropertyGroups.findOrCreate(properties);
        this.blocks = BlockGroups.findOrCreate(blocks);
        this.raw = new String[] { properties, blocks };
    }

    private static Stream<BlockEntry> create(String entry) {
        final String[] split = split(entry);
        if (loadTest(split[0]) && loadTest(split[1])) {
            log.info("{}, {} is valid. Loading...", split[0], split[1]);
            return Stream.of(new BlockEntry(split[0], split[1]));
        }
        return Stream.empty();
    }

    /** Generates entries from the block list. */
    public static Set<BlockEntry> setupEntries() {
        final Set<BlockEntry> entries = createEntries(Cfg.blockEntries.get());
        if (Cfg.testForDuplicates.get()) {
            testForDuplicates(entries);
        }
        return entries;
    }

    public static Set<BlockEntry> createEntries(List<String> raw) {
        return raw.stream().flatMap(BlockEntry::create).collect(Collectors.toSet());
    }

    public static void testForDuplicates(Set<BlockEntry> entries) {
        forAllEntries(entries, (index1, block1, props1) ->
            forAllEntries(entries, (index2, block2, props2) -> {
                if (!index1.equals(index2) && block1.equals(block2) && props1.equals(props2)) {
                    throw runExF("Invalid block list: multiple entries contain {} in {}.", props1.name, block1);
                }
            })
        );
    }

    /** Runs the input function for each combination of BlockState : OreProperties. */
    private static void forAllEntries(Set<BlockEntry> entries, TriConsumer<Integer, ResourceLocation, OreProperties> fun) {
        int i = 0;
        for (BlockEntry entry : entries) {
            for (ResourceLocation block : entry.blocks.items) {
                for (OreProperties props : entry.properties.items) {
                    fun.accept(i, block, props);
                }
            }
            i++;
        }
    }

    private static boolean loadTest(String mod) {
        mod = mod.toLowerCase();
        return Group.isSpecial(mod) || Cfg.modEnabled(mod);
    }

    /**
     * Splits entries around either `,` or ` `, removing empty values
     * and trimming the results.
     */
    public static String[] split(String entry) {
        String[] split = entry.split("[, ]");
        ArrayUtils.removeAllOccurences(split, "");
        assert(split.length == 2);
        split[0] = split[0].trim();
        split[1] = split[1].trim();
        return split;
    }

    @Override
    public String toString() {
        return f("BlockGroup{{} ores -> {} blocks}", raw[0], raw[1]);
    }
}