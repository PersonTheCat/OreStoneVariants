package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import net.minecraft.block.BlockState;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BlockEntry {
    public final BlockGroup blocks;
    public final PropertyGroup properties;

    private BlockEntry(String properties, String blocks) {
        this.properties = PropertyGroup.findOrCreate(properties);
        this.blocks = BlockGroup.findOrCreate(blocks);
    }

    private static Stream<BlockEntry> create(String entry) {
        final String[] split = split(entry);
        if (loadTest(split[0]) && loadTest(split[1])) {
            info("{}, {} is valid. Loading...", split[0], split[1]);
            return Stream.of(new BlockEntry(split[0], split[1]));
        }
        return Stream.empty();
    }

    /** Generates entries from the block list. */
    public static Set<BlockEntry> setupEntries() {
        final Set<BlockEntry> entries = Cfg.blockEntries.get().stream()
            .flatMap(BlockEntry::create)
            .filter(BlockEntry::modsSupported)
            .collect(Collectors.toSet());
        if (Cfg.testForDuplicates.get()) {
            testForDuplicates(entries);
        }
        return entries;
    }

    private static void testForDuplicates(Set<BlockEntry> entries) {
        forAllEntries(entries, (index1, block1, props1) ->
            forAllEntries(entries, (index2, block2, props2) -> {
                if (!index1.equals(index2) && block1.equals(block2) && props1.equals(props2)) {
                    throw runExF("Registry error: multiple entries generated with {} in {}. Check your block list.",
                        props1.name, block1);
                }
            })
        );
    }

    /** Runs the input function for each combination of BlockState : OreProperties. */
    private static void forAllEntries(Set<BlockEntry> entries, TriConsumer<Integer, BlockState, OreProperties> fun) {
        int i = 0;
        for (BlockEntry entry : entries) {
            for (BlockState block : entry.blocks.blocks.get()) {
                for (OreProperties props : entry.properties.properties) {
                    fun.accept(i, block, props);
                }
            }
            i++;
        }
    }

    private static boolean loadTest(String mod) {
        mod = mod.toLowerCase();
        return mod.equals("default") || mod.equals("all") || !Cfg.modFamiliar(mod) || modEnabled(mod);
    }

    private static boolean modEnabled(String mod) {
        return Cfg.modEnabled(mod) && isModLoaded(mod);
    }


    private boolean modsSupported() {
        return blocks.mod.map(Cfg::modEnabled).orElse(true)
            && properties.mod.map(Cfg::modEnabled).orElse(true);
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
        return f("BlockGroup{{} ores -> {} blocks}", properties.name, blocks.name);
    }
}