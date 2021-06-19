package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.BlockGroups;
import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.item.VariantItem;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroups;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.util.Group;
import com.personthecat.orestonevariants.util.SafeRegistry;
import net.minecraft.util.ResourceLocation;

import static com.personthecat.orestonevariants.util.CommonMethods.f;

/**
 * All of this mod's lazily-initialized registries. This class essentially defines a set of
 * registry events which are intended to run as late as possible. This is ideal for this
 * mod which depends on virtually every other mod to load before it.
 */
public class LazyRegistries {

    /** A registry containing all of the items. */
    public static final SafeRegistry<ResourceLocation, VariantItem> ITEMS =
        SafeRegistry.of(ItemInit::setupItems);

    /** A registry containing all of the blocks. */
    public static final SafeRegistry<ResourceLocation, OreVariant> BLOCKS =
        SafeRegistry.of(BlockInit::setupBlocks);

    /** A registry of all block groups for the config file. */
    public static final SafeRegistry<String, Group<ResourceLocation>> BLOCK_GROUPS =
        SafeRegistry.of(BlockGroups::setupBlockGroups);

    /** A registry of variant properties. */
    public static final SafeRegistry<String, OreProperties> ORE_PROPERTIES =
        SafeRegistry.of(OreProperties::setupOreProperties,
            name -> f("There are no properties named \"{}.\" Fix your property group.", name));

    /** A registry of properties used for generating stone veins. */
    public static final SafeRegistry<ResourceLocation, StoneProperties> STONE_PROPERTIES =
        SafeRegistry.of(StoneProperties::setupStoneProperties);

    /** A registry of all property groups for the config file. */
    public static final SafeRegistry<String, Group<OreProperties>> PROPERTY_GROUPS =
        SafeRegistry.of(PropertyGroups::setupPropertyGroups);

    /** A registry of block entries from the config file. */
    public static final SafeRegistry<Integer, BlockEntry> BLOCK_ENTRIES =
        SafeRegistry.enumerated(BlockEntry::setupEntries);

}
