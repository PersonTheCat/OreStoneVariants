package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.config.ArrayTemplate;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.util.Lazy;
import lombok.extern.log4j.Log4j2;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.find;
import static com.personthecat.orestonevariants.util.CommonMethods.full;

@Log4j2
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockGroup {
    public final Set<ResourceLocation> blocks;
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
    public BlockGroup(String name, Set<ResourceLocation> blocks) {
        this(name, blocks, empty());
    }

    /** Primary constructor. */
    public BlockGroup(String name, Set<ResourceLocation> blocks, Optional<String> mod) {
        this.name = name;
        this.blocks = blocks;
        this.mod = mod;
    }

    public static Set<BlockGroup> setupBlockGroups() {
        final Set<BlockGroup> groups = new HashSet<>();
        Cfg.blockGroups.forEach((name, entries) -> {
            if (shouldAdd(name)) {
                groups.add(new BlockGroup(name, mapLocations(entries), empty()));
            }
        });
        return groups;
    }

    private static boolean shouldAdd(String name) {
        return !Cfg.modFamiliar(name) || Cfg.modEnabled(name);
    }

    private static Set<ResourceLocation> mapLocations(List<String> ids) {
       return ids.stream().map(BlockGroup::parseLocation).collect(Collectors.toSet());
    }

    private static ResourceLocation parseLocation(String id) {
        // Ignore variant data. We'll use all variants now.
        final String[] split = id.split("\\[");
        if (split.length > 1) {
            log.warn("Extra variant data in block ID. Ignoring \"{}\"...", split[1]);
        }
        return new ResourceLocation(split[0]);
    }

    /** Generates a group containing all registered blocks from all BlockGroups. */
    private static BlockGroup getAllBlocks() {
        final Set<ResourceLocation> blocks = new HashSet<>();
        for (BlockGroup group : LazyRegistries.BLOCK_GROUPS) {
            blocks.addAll(group.blocks);
        }
        return new BlockGroup("all", blocks);
    }

    /** Generates a group containing all registered blocks from default BlockGroups. */
    private static BlockGroup getDefaultBlocks() {
        final Set<ResourceLocation> blocks = new HashSet<>();
        // Find all groups with default values and reuse their blocks.
        for (DefaultInfo info : DefaultInfo.values()) {
            final Set<ResourceLocation> updated = find(LazyRegistries.BLOCK_GROUPS, g -> g.name.equals(info.getName()))
                .map(group -> group.blocks)
                .orElseGet(Collections::emptySet);
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
            .orElseGet(() -> new BlockGroup(name, Collections.singleton(new ResourceLocation(name)))));
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
        return blocks.size();
    }

    /** Used for neatly displaying info about default BlockGroups. */
    public enum DefaultInfo implements ArrayTemplate<String> {
        /** Information containing all of the default PropertyGroups. */
        MINECRAFT("stone", "andesite", "diorite", "granite"),
        CREATE("dolomite", "gabbro", "limestone", "scoria", "weathered_limestone"),
        QUARK("jasper", "limestone", "marble", "slate");

        private final List<String> values;
        private final List<String> formatted;
        private final String name = toString().toLowerCase();
        DefaultInfo(String... entries) {
            this.values = getNames(entries);
            this.formatted = format(this.values);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<String> getValues() {
            return values;
        }

        public List<String> getFormatted() {
            return formatted;
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

        /** Reformats the list of values to return filenames. */
        private List<String> format(List<String> values) {
            return values.stream()
                .map(s -> s.replace(":", "_"))
                .collect(Collectors.toList());
        }
    }
}