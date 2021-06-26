package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.config.ArrayTemplate;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.util.Group;
import lombok.extern.log4j.Log4j2;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class BlockGroups {

    public static Map<String, Group<ResourceLocation>> setupBlockGroups() {
        final Map<String, Group<ResourceLocation>> groups = new HashMap<>();
        Cfg.blockGroups.forEach((name, entries) -> {
            if (Cfg.modEnabled(name)) {
                groups.put(name, new Group<>(mapLocations(entries)));
            }
        });
        groups.put(Group.ALL, Group.createAllGroup(groups));
        groups.put(Group.DEFAULT, Group.createDefaultGroup(DefaultInfo.values(), groups));
        return groups;
    }

    private static Set<ResourceLocation> mapLocations(List<String> ids) {
       return ids.stream().map(BlockGroups::parseLocation).collect(Collectors.toSet());
    }

    private static ResourceLocation parseLocation(String id) {
        // Ignore variant data. We'll use all variants now.
        final String[] split = id.split("\\[");
        if (split.length > 1) {
            log.warn("Extra variant data in block ID. Ignoring \"{}\"...", split[1]);
        }
        return new ResourceLocation(split[0]);
    }

    /**
     * Attempts to locate a block group with the specified name. Else,
     * creates a new group containing only the respective blocks.
     */
    public static Group<ResourceLocation> findOrCreate(String name) {
        return LazyRegistries.BLOCK_GROUPS.getOptional(name)
            .orElseGet(() -> new Group<>(new ResourceLocation(name)));
    }

    /** Used for neatly displaying info about default BlockGroups. */
    public enum DefaultInfo implements ArrayTemplate<String> {
        /** Information containing all of the default PropertyGroups. */
        BYG("rocky_stone", "scoria_stone", "soapstone"),
        CREATE("dolomite", "gabbro", "limestone", "scoria", "weathered_limestone"),
        MINECRAFT("stone", "andesite", "diorite", "granite"),
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