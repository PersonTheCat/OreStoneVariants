package com.personthecat.orestonevariants;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.commands.CommandOSV;
import com.personthecat.orestonevariants.commands.HjsonArgument;
import com.personthecat.orestonevariants.commands.PathArgument;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.BlockInit;
import com.personthecat.orestonevariants.init.ItemInit;
import com.personthecat.orestonevariants.io.JarFiles;
import com.personthecat.orestonevariants.io.ZipTools;
import com.personthecat.orestonevariants.item.VariantItem;
import com.personthecat.orestonevariants.models.ModelEventHandler;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.SafeRegistry;
import com.personthecat.orestonevariants.world.OreGen;
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

import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

@Mod(Main.MODID)
public class Main {
    /** A setting representing this mod's namespace. */
    public static final String MODID = "osv";
    /** The primary Log4j logger used by this mod. */
    public static final Logger LOGGER = logger(MODID);
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
    /** A convenient reference to the current mod event bus. */
    private final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    /** A reference to Forge's main event bus. */
    private final IEventBus eventBus = MinecraftForge.EVENT_BUS;

    // Todo: proxy

    public Main() {
        Cfg.register(ModLoadingContext.get().getActiveContainer());
        setupEventHandlers();
        ZipTools.copyResourcePack();
        JarFiles.copyPresetFiles();
    }

    private void setupEventHandlers() {
        modBus.addListener(this::initCommon);
        modBus.addListener(this::initClient);
        eventBus.addListener(this::initServer);
    }

    private void initCommon(final FMLCommonSetupEvent event) {
        modBus.addListener(this::modConfig);
        OreGen.setupOreFeatures();
        PathArgument.register();
        HjsonArgument.register();
    }

    private void initClient(final FMLClientSetupEvent event) {
        modBus.addListener(ModelEventHandler::onTextureStitch);
        modBus.addListener(ModelEventHandler::onModelBake);
    }

    private void initServer(final FMLServerStartingEvent event) {
        RecipeHelper.handleRecipes(event.getServer().getRecipeManager());
        CommandOSV.register(event.getServer().getCommandManager());
    }

    private void modConfig(final ModConfig.ModConfigEvent event) {}
}
