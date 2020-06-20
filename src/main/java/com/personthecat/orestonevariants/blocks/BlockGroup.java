package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.ArrayTemplate;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.state.IBlockState;
import org.hjson.JsonObject;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockGroup {
    public final Lazy<Set<IBlockState>> blocks;
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
    public BlockGroup(String name, Set<IBlockState> blocks) {
        this(name, blocks, empty());
    }

    /** Convenience for handling lazy types. */
    public BlockGroup(String name, Set<IBlockState> blocks, Optional<String> mod) {
        this(name, new Lazy<>(blocks), mod);
    }

    /** Primary constructor. */
    private BlockGroup(String name, Lazy<Set<IBlockState>> blocks, Optional<String> mod) {
        this.name = name;
        this.blocks = blocks;
        this.mod = mod;
    }

    public static Set<BlockGroup> setupBlockGroups() {
        final Set<BlockGroup> groups = new HashSet<>();
        Cfg.BlockRegistryCat.blockGroups.forEach((name, entries) -> {
            if (shouldAdd(name)) {
                groups.add(new BlockGroup(name, new Lazy<>(() -> getStates(entries)), empty()));
            }
        });
        return groups;
    }

    private static boolean shouldAdd(String name) {
        return !Cfg.modFamiliar(name) || Cfg.modEnabled(name);
    }

    private static Set<IBlockState> getStates(String... entries) {
        return Stream.of(entries).map(entry -> getBlockState(entry)
            .orElseThrow(() -> runExF("There is no block named \"{}.\" Fix your block group.", entry)))
            .collect(Collectors.toSet());
    }

    /** Generates a group containing all registered blocks from all BlockGroups. */
    private static BlockGroup getAllBlocks() {
        final Set<IBlockState> blocks = new HashSet<>();
        for (BlockGroup group : Main.BLOCK_GROUPS) {
            blocks.addAll(group.blocks.get());
        }
        return new BlockGroup("all", blocks);
    }

    /** Generates a group containing all registered blocks from default BlockGroups. */
    private static BlockGroup getDefaultBlocks() {
        final Set<IBlockState> blocks = new HashSet<>();
        // Find all groups with default values and reuse their blocks.
        for (DefaultInfo info : DefaultInfo.values()) {
            final Set<IBlockState> updated = find(Main.BLOCK_GROUPS, g -> g.name.equals(info.getName()))
                .map(group -> group.blocks.get())
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
            .orElseGet(() -> find(Main.BLOCK_GROUPS, g -> g.name.equals(name))
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
        /** Information containing all of the default BlockGroups. */
        MINECRAFT("stone", "andesite", "diorite", "granite"),
        EARTHWORKS("block_chalk", "block_slate", "block_slate_green", "block_slate_purple"),
        MINERALOGY("amphibolite", "andesite", "basalt", "chert", "conglomerate", "diorite",
            "dolomite", "granite", "gypsum", "limestone", "marble", "pegmatite", "phyllite", "pumice",
            "rhyolite", "schist"),
        QUARK("limestone", "marble", "slate", "jasper"),
        RUSTIC("slate"),
        UNDERGROUNDBIOMES("igneous_stone:0", "igneous_stone:1", "igneous_stone:2",
            "igneous_stone:3", "igneous_stone:4", "igneous_stone:5", "igneous_stone:6",
            "igneous_stone:7", "metamorphic_stone:0", "metamorphic_stone:1", "metamorphic_stone:2",
            "metamorphic_stone:3", "metamorphic_stone:4", "metamorphic_stone:5", "metamorphic_stone:6",
            "metamorphic_stone:7", "sedimentary_stone:0", "sedimentary_stone:1", "sedimentary_stone:2",
            "sedimentary_stone:3", "sedimentary_stone:4", "sedimentary_stone:5", "sedimentary_stone:6",
            "sedimentary_stone:7");

        private final String[] values;
        private final String[] names;
        private final String name = toString().toLowerCase();
        DefaultInfo(String... entries) {
            this.names = entries;
            this.values = getValues(entries);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String[] getValues() {
            return values;
        }

        public String[] getNames() {
            return names;
        }

        /** Returns the formatted values in this group. */
        private String[] getValues(String[] entries) {
            final List<String> names = new ArrayList<>();
            for (String entry : entries) {
                final StringBuilder sb = new StringBuilder();
                if (name.equals("minecraft")) {
                    // May be unnecessary, but it allows nicer file names
                    // than e.g. "stone_1.hjson." It's a bit of a hack for
                    // 1.12, but oh well.
                    final String location = getLocation(entry);
                    sb.append(location);
                } else {
                    sb.append(name);
                    sb.append(':');
                    sb.append(entry);
                }
                names.add(sb.toString());
            }
            return names.toArray(new String[0]);
        }

        /** Retrieves the actual block location from the json. */
        private static String getLocation(String entry) {
            if (entry.equals("stone")) {
                return entry;
            }
            final String path = f("assets{}{}.hjson", StoneProperties.FOLDER, entry);
            final InputStream is = getRequiredResource(path);
            final JsonObject json = readJson(is)
                .orElseThrow(() -> runExF("Expected {}.json to exist in jar.", entry));
            return getObject(json, "block")
                .flatMap(o -> getString(o, "location"))
                .orElseThrow(() -> runEx("Missing field bundled preset."));
        }
    }
}