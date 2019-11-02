package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.fml.common.Loader;
import personthecat.mod.config.Cfg;

public class PropertyGroup
{
    private final OreProperties[] props;
    private final String name;
    private boolean inUse;

    private static final Map<String, PropertyGroup> PROPERTY_GROUP_MAP = new HashMap<>();

    private static PropertyGroup ALL = new PropertyGroup("all", new OreProperties[0]);
    private static PropertyGroup DEFAULT = new PropertyGroup("default", new OreProperties[0]);

    public PropertyGroup(String name, OreProperties[] props)
    {
        this.name = name;
        this.props = props;

        register();
    }

    public static Collection<PropertyGroup> getPropertyGroupRegistry()
    {
        return PROPERTY_GROUP_MAP.values();
    }

    public static PropertyGroup getPropertyGroup(String name)
    {
        if (name.equals("vanilla") || name.equals("base"))
        {
            return getPropertyGroup("minecraft");
        }
        return PROPERTY_GROUP_MAP.get(name);
    }

    public static PropertyGroup findOrCreateGroup(String name)
    {
        if (name.equals("default")) return getDefaultProperties();

        if (name.equals("all")) return getAllProperties();

        if (PROPERTY_GROUP_MAP.containsKey(name)) return PROPERTY_GROUP_MAP.get(name);

        return new PropertyGroup(name, new OreProperties[] { OreProperties.propertiesOf(name) });
    }

    public static PropertyGroup getGroupByProperties(OreProperties properties)
    {
        if (properties == null) return null;

        for (PropertyGroup group : getPropertyGroupRegistry())
        {
            for (OreProperties inGroup : group.getProperties())
            {
                if (inGroup.equals(properties)) return group;
            }
        }

        return null;
    }

    public String getName()
    {
        return name;
    }

    public int size()
    {
        return props.length;
    }

    public void setPropsInUse()
    {
        if (!this.inUse)
        {
            this.inUse = true;

            for (OreProperties props : props)
            {
                props.setInUse();
            }
        }
    }

    public boolean inUse()
    {
        return inUse;
    }

    public OreProperties[] getProperties()
    {
        return props;
    }

    public boolean getConditions()
    {
        if (!isDefaultGroup()) return true;

        else return Loader.isModLoaded(name) && Cfg.isSupportEnabled(name);
    }

    public static boolean isGroupRegistered(String name)
    {
        return PROPERTY_GROUP_MAP.containsKey(name);
    }

    public boolean isDefaultGroup()
    {
        return isDefaultGroup(name);
    }

    public static boolean isDefaultGroup(String name)
    {
        for (Builder b : Builder.DEFAULT_GROUP_INFO)
        {
            if (b.name.equals(name)) return true;
        }

        return false;
    }

    public static PropertyGroup getAllProperties()
    {
        if (ALL.size() == 0)
        {
            Set<OreProperties> allProps = new HashSet<>();

            for (PropertyGroup group : getPropertyGroupRegistry())
            {
                for (OreProperties props : group.getProperties())
                {
                    allProps.add(props);
                }
            }
            List<OreProperties> test = new ArrayList<>();

            ALL = new PropertyGroup("all", allProps.toArray(new OreProperties[0]));
        }

        return ALL;
    }

    public static PropertyGroup getDefaultProperties()
    {
        if (DEFAULT.size() == 0)
        {
            Set<OreProperties> defaultProps = new HashSet<>();

            for (PropertyGroup group : getPropertyGroupRegistry())
            {
                if (group.isDefaultGroup())
                {
                    for (OreProperties props : group.getProperties())
                    {
                        defaultProps.add(props);
                    }
                }
            }

            DEFAULT = new PropertyGroup("default", defaultProps.toArray(new OreProperties[0]));
        }

        return DEFAULT;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public PropertyGroup register()
    {
        PROPERTY_GROUP_MAP.put(name, this);

        return this;
    }

    public static class Builder
    {
        private final String[] properties;
        private final String name;

        public static final List<String> POSSIBLE_MISSING_INFO = new ArrayList<>();
        private static final List<Builder> PG_BUILDER_REGISTRY = new ArrayList<>();
        /**Need this list before OreProperties can be created*/
        public static Builder[] DEFAULT_GROUP_INFO = new Builder[]
                {
                        new Builder("minecraft", true, new String[]
                                {
                                        "coal_ore", "diamond_ore", "emerald_ore", "gold_ore",
                                        "iron_ore",    "lapis_ore", "redstone_ore"
                                }),
                        new Builder("iceandfire", true, new String[]
                                {
                                        "iceandfire_sapphire_ore", "iceandfire_silver_ore"
                                }),
                        new Builder("simpleores", true, new String[]
                                {
                                        "simpleores_adamantium_ore", "simpleores_copper_ore",
                                        "simpleores_mythril_ore", "simpleores_tin_ore"
                                }),
                        new Builder("basemetals", true, new String[]
                                {
                                        "basemetals_antimony_ore", "basemetals_bismuth_ore", "basemetals_copper_ore",
                                        "basemetals_lead_ore", "basemetals_mercury_ore", "basemetals_nickel_ore",
                                        "basemetals_pewter_ore", "basemetals_platinum_ore", "basemetals_silver_ore",
                                        "basemetals_tin_ore", "basemetals_zinc_ore"
                                }),
                        new Builder("biomesoplenty", true, new String[]
                                {
                                        "biomesoplenty_amber_ore", "biomesoplenty_malachite_ore", "biomesoplenty_peridot_ore",
                                        "biomesoplenty_ruby_ore", "biomesoplenty_sapphire_ore", "biomesoplenty_tanzanite_ore",
                                        "biomesoplenty_topaz_ore"
                                }),
                        new Builder("glasshearts", true, new String[]
                                {
                                        "glasshearts_agate_ore", "glasshearts_amethyst_ore", "glasshearts_onyx_ore",
                                        "glasshearts_opal_ore", "glasshearts_ruby_ore", "glasshearts_sapphire_ore",
                                        "glasshearts_topaz_ore"
                                }),
                        new Builder("thermalfoundation", true, new String[]
                                {
                                        "thermalfoundation_copper_ore", "thermalfoundation_lead_ore",
                                        "thermalfoundation_nickel_ore", "thermalfoundation_silver_ore",
                                        "thermalfoundation_tin_ore"
                                }),
                        new Builder("immersiveengineering", true, new String[]
                                {
                                        "immersiveengineering_aluminum_ore", "immersiveengineering_copper_ore",
                                        "immersiveengineering_lead_ore", "immersiveengineering_nickel_ore",
                                        "immersiveengineering_silver_ore", "immersiveengineering_uranium_ore"
                                }),
                        new Builder("thaumcraft", true, new String[]
                                {
                                        "thaumcraft_amber_ore", "thaumcraft_cinnabar_ore"
                                }),
                        new Builder("embers", true, new String[]
                                {
                                        "embers_aluminum_ore", "embers_copper_ore", "embers_lead_ore",
                                        "embers_nickel_ore", "embers_silver_ore", "embers_tin_ore"
                                }),
                        new Builder("mineralogy", true, new String[]
                                {
                                        "mineralogy_phosphorous_ore", "mineralogy_sulfur_ore"
                                }),
                        new Builder("modernmetals", true, new String[]
                                {
                                        "modernmetals_aluminum_ore", "modernmetals_beryllium_ore",
                                        "modernmetals_boron_ore", "modernmetals_cadmium_ore",
                                        "modernmetals_chromium_ore", "modernmetals_iridium_ore",
                                        "modernmetals_magnesium_ore", "modernmetals_manganese_ore",
                                        "modernmetals_osmium_ore", "modernmetals_plutonium_ore",
                                        "modernmetals_rutile_ore", "modernmetals_tantalum_ore",
                                        "modernmetals_thorium_ore", "modernmetals_tungsten_ore",
                                        "modernmetals_uranium_ore", "modernmetals_zirconium_ore"
                                })
                };

        public Builder(String name, String[] propertyNames)
        {
            this.name = name;
            this.properties = propertyNames;

            PG_BUILDER_REGISTRY.add(this);
        }

        public String getName()
        {
            return name;
        }

        public String[] getPropertyNames()
        {
            return properties;
        }

        private Builder(String name, boolean avoidConflicts, String[] propertyNames)
        {
            this.name = name;
            this.properties = propertyNames;
        }

        public static boolean isBuilderRegistered(String modid)
        {
            for (Builder b : PG_BUILDER_REGISTRY)
            {
                if (b.name.equals(modid)) return true;
            }

            return false;
        }

        public void convertRegistry()
        {
            PG_BUILDER_REGISTRY.add(this);

            for (int i = 0; i < DEFAULT_GROUP_INFO.length; i++)
            {
                Builder b = DEFAULT_GROUP_INFO[i];

                if (b.equals(this))
                {
                    POSSIBLE_MISSING_INFO.add(b.name);

                    return;
                }
            }
        }

        public static Builder getMatchingDefaultBuidler(String modid)
        {
            for (Builder b : DEFAULT_GROUP_INFO)
            {
                if (b.name.equals(modid)) return b;
            }

            return null;
        }

        public static void buildAll()
        {
            for (Builder b : PG_BUILDER_REGISTRY)
            {
                b.build();
            }

            PG_BUILDER_REGISTRY.clear();
        }

        public PropertyGroup build()
        {
            OreProperties[] newProps = new OreProperties[properties.length];

            for (int i = 0; i < properties.length; i++)
            {
                newProps[i] = OreProperties.propertiesOf(properties[i]);
            }

            return new PropertyGroup(name, newProps);
        }
    }
}
