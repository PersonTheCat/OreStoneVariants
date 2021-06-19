package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.ArrayTemplate;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.util.Group;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.f;
public class PropertyGroups {

    public static Map<String, Group<OreProperties>> setupPropertyGroups() {
        final Map<String, Group<OreProperties>> groups = new HashMap<>();
        Cfg.propertyGroups.forEach((name, entries) -> {
            if (Cfg.groupListed(name) && Cfg.modEnabled(name)) {
                groups.put(name, new Group<>(LazyRegistries.ORE_PROPERTIES.getAll(entries)));
            }
        });
        groups.put(Group.ALL, Group.createAllGroup(groups));
        groups.put(Group.DEFAULT, Group.createDefaultGroup(DefaultInfo.values(), groups));
        return groups;
    }

    /**
     * Attempts to locate a property group with the specified name. Else,
     * creates a new group containing only the respective OreProperties.
     */
    public static Group<OreProperties> findOrCreate(String name) {
        return LazyRegistries.PROPERTY_GROUPS.getOptional(name)
            .orElseGet(() -> new Group<>(LazyRegistries.ORE_PROPERTIES.getAsserted(name)));
    }

    /** Used for neatly displaying info about default PropertyGroups. */
    public enum DefaultInfo implements ArrayTemplate<String> {
        /** Information containing all of the default PropertyGroups. */
        MINECRAFT("coal", "diamond", "emerald", "gold", "iron", "lapis", "redstone"),
        CREATE("copper", "zinc"),
        SIMPLEORES("tin", "mythril", "copper", "adamantium"),
        ICEANDFIRE("amethyst", "copper", "sapphire", "silver");

        private static final String[] ADDITIONAL_NAMES = {
            "TUTORIAL", "gilded_blackstone_ore", "nether_gold_ore", "quartz_ore",
            "infested_stone", "quark_biotite_ore", "simpleores_onyx_ore"
        };

        private final List<String> values;
        private final String name = toString().toLowerCase();
        DefaultInfo(String... entries) {
            this.values = getNames(entries);
        }

        /** All of the default property names. These should exist in the jar. */
        public static List<String> getAllNames() {
            final List<String> names = new ArrayList<>();
            Collections.addAll(names, ADDITIONAL_NAMES);
            for (DefaultInfo info : values()) {
                names.addAll(info.values);
            }
            return names;
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
            for (String name : entries) {
                names.add(this.name.equals("minecraft")
                    ? f("{}_ore", name)
                    : f("{}_{}_ore", this.name, name));
            }
            return names;
        }
    }
}