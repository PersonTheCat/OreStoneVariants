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
        Cfg.BlockRegistryCat.propertyGroups.forEach((name, entries) -> {
            if (shouldAdd(name)) {
                groups.add(new PropertyGroup(name, OreProperties.of(entries)));
            }
        });
        return groups;
    }

    private static boolean shouldAdd(String name) {
        return !Cfg.modFamiliar(name) || Cfg.modEnabled(name);
    }

    public boolean modLoaded() {
        if (!mod.isPresent()) {
            return true;
        }
        final String domain = mod.get();
        return !Cfg.modFamiliar(domain) || Cfg.modEnabled(domain);
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
            final Optional<Set<OreProperties>> updated = find(Main.PROPERTY_GROUPS, g -> g.name.equals(info.name))
                .filter(PropertyGroup::modLoaded)
                .map(group -> group.properties);
            updated.ifPresent(list::addAll);
        }
        return new PropertyGroup("default", list);
    }

    /** Used for neatly displaying info about default PropertyGroups. */
    public enum DefaultInfo implements ArrayTemplate<String> {
        /** Information containing all of the default PropertyGroups. */
        MINECRAFT("coal", "diamond", "emerald", "gold", "iron", "lapis", "redstone"),
        ICEANDFIRE("sapphire", "silver"),
        SIMPLEORES("adamantium", "copper", "mythril", "tin", "onyx"),
        BASEMETALS("antimony", "bismuth", "copper", "lead", "mercury", "nickel", "platinum",
            "silver", "tin", "zinc"),
        BIOMESOPLENTY("amber", "malachite", "peridot", "ruby", "sapphire", "tanzanite",
            "topaz", "amethyst"),
        GLASSHEARTS("agate", "amethyst", "opal", "ruby", "sapphire", "topaz"),
        THERMALFOUNDATION("copper", "lead", "nickel", "silver", "tin"),
        IMMERSIVEENGINEERING("aluminum", "copper", "lead", "nickel", "silver", "uranium"),
        EMBERS("aluminum", "copper", "lead", "nickel", "silver"),
        THAUMCRAFT("amber", "cinnabar"),
        MINERALOGY("phosphorous", "sulfur"),
        MODERNMETALS("aluminum", "aluminumbrass", "beryllium", "boron", "cadmium", "chromium",
            "galvanizedsteel", "iridium", "magnesium", "nichrome", "osmium", "plutonium", "rutile",
            "stainlesssteel", "tantalum", "thorium", "titanium", "tungsten", "uranium", "zirconium");

        /** A list of ores that shouldn't spawn by default. */
        private static final List<String> ADDITIONAL_NAMES = list(
            "TUTORIAL", "quartz_ore", "quark_biotite_ore", "simpleores_onyx_ore",
            "basemetals_pewter_ore", "basemetals_adamantine_ore", "basemetals_coldiron_ore",
            "basemetals_cupronickel_ore", "basemetals_starsteel_ore", "thermalfoundation_aluminum_ore",
            "thermalfoundation_iridium_ore", "thermalfoundation_mithril_ore", "thermalfoundation_platinum_ore"
        );

        private final String[] values;
        private final String name = toString().toLowerCase();
        DefaultInfo(String... entries) {
            this.values = getNames(entries);
        }

        /** All of the default property names. These should exist in the jar. */
        public static List<String> getAllNames() {
            final List<String> names = new ArrayList<>(ADDITIONAL_NAMES);
            for (DefaultInfo info : values()) {
                names.addAll(list(info.values));
            }
            return names;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String[] getValues() {
            return values;
        }

        /** Returns the formatted values in this group. */
        private String[] getNames(String[] entries) {
            final List<String> names = new ArrayList<>();
            for (String name : entries) {
                names.add(this.name.equals("minecraft")
                    ? f("{}_ore", name)
                    : f("{}_{}_ore", this.name, name));
            }
            return names.toArray(new String[0]);
        }
    }
}