package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.config.ArrayTemplate;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.BlockState;

import java.util.*;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.find;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.getBlockState;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockGroup {
    public final Lazy<Set<BlockState>> blocks;
    public final Optional<String> mod;
    public final String name;

    /** The group containing all blocks from all registered BlockGroups. */
    public static final Lazy<BlockGroup> ALL = new Lazy<>(
        BlockGroup::getAllBlocks
    );

    /** The group containing all block from default BlockGroups. */
    public static final Lazy<BlockGroup> DEFAULT = new Lazy<>(
        BlockGroup::getDefaultBlocks
    );

    /** Convenience Constructor for custom groups. */
    public BlockGroup(String name, Set<BlockState> blocks) {
        this(name, blocks, empty());
    }

    /** Convenience for handling lazy types. */
    public BlockGroup(String name, Set<BlockState> blocks, Optional<String> mod) {
        this(name, new Lazy<>(blocks), mod);
    }

    /** Primary constructor. */
    private BlockGroup(String name, Lazy<Set<BlockState>> blocks, Optional<String> mod) {
        this.name = name;
        this.blocks = blocks;
        this.mod = mod;
    }

    public static Set<BlockGroup> setupBlockGroups() {
        final Set<BlockGroup> groups = new HashSet<>();
        Cfg.blockGroups.forEach((name, entries) -> {
            if (shouldAdd(name)) {
                groups.add(new BlockGroup(name, new Lazy<>(() -> getStates(entries)), empty()));
            }
        });
        return groups;
    }

    private static boolean shouldAdd(String name) {
        return !Cfg.modFamiliar(name) || Cfg.modEnabled(name);
    }

    private static Set<BlockState> getStates(List<String> entries) {
        return entries.stream().map(entry -> getBlockState(entry)
            .orElseThrow(() -> runExF("There is no block named \"{}.\" Fix your block group.", entry)))
            .collect(Collectors.toSet());
    }

    /** Generates a group containing all registered blocks from all BlockGroups. */
    private static BlockGroup getAllBlocks() {
        final Set<BlockState> blocks = new HashSet<>();
        for (BlockGroup group : LazyRegistries.BLOCK_GROUPS) {
            blocks.addAll(group.blocks.get());
        }
        return new BlockGroup("all", blocks);
    }

    /** Generates a group containing all registered blocks from default BlockGroups. */
    private static BlockGroup getDefaultBlocks() {
        final Set<BlockState> blocks = new HashSet<>();
        // Find all groups with default values and reuse their blocks.
        for (DefaultInfo info : DefaultInfo.values()) {
            final Set<BlockState> updated = find(LazyRegistries.BLOCK_GROUPS, g -> g.name.equals(info.getName()))
                .map(group -> group.blocks.get())
                .orElseThrow(() -> runExF("BlockGroups were not registered in time."));
            blocks.addAll(updated);
        }
        return new BlockGroup("default", blocks);
    }

    /**
     * Attempts to locate a block group with the specified name. Else,
     * creates a new group containing only the respective OreProperties.
     */
    public static BlockGroup findOrCreate(String name) {
        return getHardCoded(name)
            .orElseGet(() -> find(LazyRegistries.BLOCK_GROUPS, g -> g.name.equals(name))
            .orElseGet(() -> new BlockGroup(name, Collections.singleton(getBlockState(name)
                .orElseThrow(() -> runExF("No block named \"{}.\" Fix your block group.", name))))));
    }

    private static Optional<BlockGroup> getHardCoded(String name) {
        switch (name) {
            case "all" : return full(ALL.get());
            case "default" : return full(DEFAULT.get());
            default : return empty();
        }
    }

    /** Returns the number of entries in this group. */
    public int size() {
        return blocks.get().size();
    }

    /** Used for neatly displaying info about default BlockGroups. */
    public enum DefaultInfo implements ArrayTemplate<String> {
        /** Information containing all of the default PropertyGroups. */
        MINECRAFT("stone", "andesite", "diorite", "granite");

        private final List<String> values;
        private final String name = toString().toLowerCase();
        DefaultInfo(String... entries) {
            this.values = getNames(entries);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<String> getValues() {
            return values;
        }

        /** Returns the formatted values in this group. */
        private List<String> getNames(String[] entries) {
            final List<String> names = new ArrayList<>();
            for (String entry : entries) {
                final StringBuilder sb = new StringBuilder();
                if (!name.equals("minecraft")) {
                    sb.append(name);
                    sb.append(':');
                }
                sb.append(entry);
                names.add(sb.toString());
            }
            return names;
        }
    }
}