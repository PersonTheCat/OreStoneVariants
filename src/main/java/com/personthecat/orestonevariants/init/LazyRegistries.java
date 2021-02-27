package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.item.VariantItem;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.util.SafeRegistry;

import java.util.Set;

/**
 * All of this mod's lazily-initialized registries. This class essentially defines a set of
 * registry events which are intended to run as late as possible. This is ideal for this
 * mod which depends on virtually every other mod to load before it.
 */
public class LazyRegistries {

    /** A registry containing all of the items. */
    public static final Set<VariantItem> ITEMS = SafeRegistry.of(ItemInit::setupItems);

    /** A registry containing all of the blocks. */
    public static final Set<BaseOreVariant> BLOCKS = SafeRegistry.of(BlockInit::setupBlocks);

    /** A registry of all block groups for the config file. */
    public static final Set<BlockGroup> BLOCK_GROUPS = SafeRegistry.of(BlockGroup::setupBlockGroups);

    /** A registry of variant properties. */
    public static final Set<OreProperties> ORE_PROPERTIES = SafeRegistry.of(OreProperties::setupOreProperties);

    /** A registry of properties used for generating stone veins. */
    public static final Set<StoneProperties> STONE_PROPERTIES = SafeRegistry.of(StoneProperties::setupStoneProperties);

    /** A registry of all property groups for the config file. */
    public static final Set<PropertyGroup> PROPERTY_GROUPS = SafeRegistry.of(PropertyGroup::setupPropertyGroups);

    /** A registry of block entries from the config file. */
    public static final Set<BlockEntry> BLOCK_ENTRIES = SafeRegistry.of(BlockEntry::setupEntries);

}
