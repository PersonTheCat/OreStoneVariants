package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.ArrayTemplate;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Lazy;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PropertyGroup {
    public final Set<OreProperties> properties;
    public final Optional<String> mod;
    public final String name;

    /** The group containing all registered PropertyGroups */
    public static final Lazy<PropertyGroup> ALL = new Lazy<>(
        PropertyGroup::getAllProperties
    );

    /** The group containing all of the default PropertyGroups. */
    public static final Lazy<PropertyGroup> DEFAULT = new Lazy<>(
        PropertyGroup::getDefaultProperties
    );

    /** Convenience constructor for custom groups. */
    public PropertyGroup(String name, Set<OreProperties> properties) {
        this(name, properties, empty());
    }

    /** Primary constructor. */
    public PropertyGroup(String name, Set<OreProperties> properties, Optional<String> mod) {
        this.name = name;
        this.properties = properties;
        this.mod = mod;
    }

    public static Set<PropertyGroup> setupPropertyGroups() {
        final Set<PropertyGroup> groups = new HashSet<>();
        Cfg.propertyGroups.forEach((name, entries) -> {
            if (shouldAdd(name)) {
                groups.add(new PropertyGroup(name, OreProperties.of(entries)));
            }
        });
        return groups;
    }

    private static boolean shouldAdd(String name) {
        return !Cfg.modFamiliar(name) || Cfg.modEnabled(name);
    }

    /**
     * Attempts to locate a property group with the specified name. Else,
     * creates a new group containing only the respective OreProperties.
     */
    public static PropertyGroup findOrCreate(String name) {
        return getHardCoded(name)
            .orElseGet(() -> find(Main.PROPERTY_GROUPS, g -> g.name.equals(name))
            .orElseGet(() -> new PropertyGroup(name, Collections.singleton(OreProperties.of(name)
                .orElseThrow(() -> runExF("No properties named \"{}.\" Fix your property group.", name))))));
    }

    private static Optional<PropertyGroup> getHardCoded(String name) {
        switch (name) {
            case "all" : return full(ALL.get());
            case "default" : return full(DEFAULT.get());
            default : return empty();
        }
    }

    /** Generates a group containing all registered OreProperties. */
    private static PropertyGroup getAllProperties() {
        return new PropertyGroup("all", new HashSet<>(Main.ORE_PROPERTIES));
    }

    /** Generates a group containing all default OreProperties. */
    private static PropertyGroup getDefaultProperties() {
        final Set<OreProperties> list = new HashSet<>();
        // Find all groups with default values and reuse their blocks.
        for (DefaultInfo info : DefaultInfo.values()) {
            final Set<OreProperties> updated = find(Main.PROPERTY_GROUPS, g -> g.name.equals(info.name))
                .map(group -> group.properties)
                .orElseThrow(() -> runExF("PropertyGroups were not registered in time."));
            list.addAll(updated);
        }
        return new PropertyGroup("default", list);
    }

    /** Used for neatly displaying info about default PropertyGroups. */
    public enum DefaultInfo implements ArrayTemplate<String> {
        /** Information containing all of the default PropertyGroups. */
        MINECRAFT("coal", "diamond", "emerald", "gold", "iron", "lapis", "redstone");

        private static final String[] ADDITIONAL_NAMES = {"TUTORIAL"};

        private final List<String> values;
        private final String name = toString().toLowerCase();
        DefaultInfo(String... entries) {
            this.values = getNames(entries);
        }

        /** All of the default property names. These should exist in the jar. */
        public static List<String> getAllNames() {
            final List<String> names = new ArrayList<>();
            for (String name : ADDITIONAL_NAMES) {
                names.add(name);
            }
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