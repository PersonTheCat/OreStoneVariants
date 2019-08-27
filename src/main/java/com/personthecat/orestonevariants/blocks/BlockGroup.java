package com.personthecat.orestonevariants.blocks;

import com.google.common.collect.ImmutableList;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.util.Lazy;
import javafx.util.Pair;
import net.minecraft.block.BlockState;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockGroup {
    public final List<BlockState> blocks;
    public final Optional<String> mod;
    public final String name;

    /** Information containing all of the default PropertyGroups. */
    private static final GroupInfo[] DEFAULT_INFO = {
        new GroupInfo("minecraft", "stone", "andesite",
            "diorite", "granite")
    };

    /** The group containing all blocks from all registered BlockGroups. */
    public static final Lazy<BlockGroup> ALL = new Lazy<>(
        BlockGroup::getAllBlocks
    );

    /** The group containing all block from default BlockGroups. */
    public static final Lazy<BlockGroup> DEFAULT = new Lazy<>(
        BlockGroup::getDefaultBlocks
    );

    /** Convenience Constructor for custom groups. */
    public BlockGroup(String name, List<BlockState> blocks) {
        this(name, blocks, empty());
    }

    /** Primary constructor. */
    public BlockGroup(String name, List<BlockState> blocks, Optional<String> mod) {
        this.name = name;
        this.blocks = blocks;
        this.mod = mod;
        Main.BLOCK_GROUPS.add(this);
    }

    public static ImmutableList<BlockGroup> setupBlockGroups() {
        return ImmutableList.of();
    }

    /** Generates a group containing all registered blocks from all BlockGroups. */
    private static BlockGroup getAllBlocks() {
        final List<BlockState> blocks = new ArrayList<>();
        for (BlockGroup group : Main.BLOCK_GROUPS) {
            blocks.addAll(group.blocks);
        }
        return new BlockGroup("all", blocks);
    }

    /** Generates a group containing all registered blocks from default BlockGroups. */
    private static BlockGroup getDefaultBlocks() {
        final List<BlockState> blocks = new ArrayList<>();
        for (GroupInfo info : DEFAULT_INFO) {
            for (String name : info.getNames()) {
                blocks.add(getBlockState(name)
                    .orElseThrow(() -> runExF("{} was not validated and does not exist.", name)));
            }
        }
        return new BlockGroup("default", blocks);
    }

    /**
     * Attempts to locate a block group with the specified name. Else,
     * creates a new group containing only the respective OreProperties.
     */
    public static BlockGroup findOrCreate(String name) {
        return find(Main.BLOCK_GROUPS, g -> g.name.equals(name))
            .orElse(new BlockGroup(name, Collections.singletonList(getBlockState(name)
            .orElseThrow(() -> runExF("No BlockGroup or properties named \"{}\"", name)))));
    }

    /** Returns the number of entries in this group. */
    public int size() {
        return blocks.size();
    }

    /** Used for neatly displaying info about default BlockGroups. */
    private static class GroupInfo extends Pair<String, String[]> {
        private GroupInfo(String name, String... entries) {
            super(name, entries);
        }

        /** Returns the formatted names in this group. */
        private String[] getNames() {
            final String[] names = new String[getValue().length];
            int index = 0;
            for (String name : getValue()) {
                StringBuilder sb = new StringBuilder();
                // To-do: ideally avoid the leading underscore.
                if (!getKey().equals("minecraft")) {
                    sb.append("_");
                    sb.append(getKey());
                }
                if(!name.equals("stone")) {
                    sb.append("_");
                    sb.append(name);
                }
                names[index++] = sb.toString();
            }
            return names;
        }
    }
}