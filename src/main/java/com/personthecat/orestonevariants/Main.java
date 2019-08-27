package com.personthecat.orestonevariants;

import com.google.common.collect.ImmutableSet;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.BlockInit;
import com.personthecat.orestonevariants.init.ItemInit;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.util.LazyImmutableList;
import com.personthecat.orestonevariants.util.LazyImmutableMap;
import com.personthecat.orestonevariants.util.LazyImmutableSet;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

@SuppressWarnings("unused")
@Mod(Main.MODID)
public class Main {
    /** A setting representing this mod's namespace. */
    public static final String MODID = "ore_stone_variants";
    /** The primary Log4j logger used by this mod. */
    public static final Logger LOGGER = logger(MODID);
    /** A standard list containing all of the items. */
    public static final LazyImmutableList<Item> ITEMS = LazyImmutableList.of(ItemInit::setupItems);
    /** A standard list containing all of the blocks */
    public static final LazyImmutableList<BaseOreVariant> BLOCKS = LazyImmutableList.of(BlockInit::setupBlocks);
    /** A standard list of all block groups for the config file. */
    public static final LazyImmutableList<BlockGroup> BLOCK_GROUPS = LazyImmutableList.of(BlockGroup::setupBlockGroups);
    /** A map of name -> properties. */
    public static final LazyImmutableMap<String, OreProperties> ORE_PROPERTIES = LazyImmutableMap.of(OreProperties::setupOreProperties);
    /** A standard list of all property groups for the config file. */
    public static final LazyImmutableList<PropertyGroup> PROPERTY_GROUPS = LazyImmutableList.of(PropertyGroup::setupPropertyGroups);
    /** A non-redundant list of block entries from the config file. */
    public static final LazyImmutableSet<BlockEntry> BLOCK_ENTRIES = LazyImmutableSet.of(BlockEntry::setupEntries);

    public Main() {
        setupListeners(FMLJavaModLoadingContext.get().getModEventBus(), MinecraftForge.EVENT_BUS);
        Cfg.register(ModLoadingContext.get().getActiveContainer());
    }

    private void setupListeners(IEventBus modBus, IEventBus eventBus) {
        modBus.addListener(this::init);
        modBus.addListener(this::initClient);
        eventBus.addListener(this::initServer);
        modBus.addListener(this::modConfig);
    }

    private void init(final FMLCommonSetupEvent event) {}

    private void initClient(final FMLClientSetupEvent event) {}

    private void initServer(final FMLServerStartingEvent event) {}

    private void modConfig(final ModConfig.ModConfigEvent event) {}
}