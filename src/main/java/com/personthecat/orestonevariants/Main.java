package com.personthecat.orestonevariants;

import com.personthecat.orestonevariants.io.ZipTools;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main {
    /** A setting representing this mod's namespace. */
    public static final String MODID = "osv";
    /** The primary Log4j logger used by this mod. */
    public static final Logger LOGGER = logger(MODID);
//    /** A registry containing all of the items. */
//    public static final Set<Item> ITEMS = SafeRegistry.of(ItemInit::setupItems);
//    /** A registry containing all of the blocks. */
//    public static final Set<BaseOreVariant> BLOCKS = SafeRegistry.of(BlockInit::setupBlocks);
//    /** A registry of all block groups for the config file. */
//    public static final Set<BlockGroup> BLOCK_GROUPS = SafeRegistry.of(BlockGroup::setupBlockGroups);
//    /** A registry of variant properties. */
//    public static final Set<OreProperties> ORE_PROPERTIES = SafeRegistry.of(OreProperties::setupOreProperties);
//    /** A registry of properties used for generating stone veins. */
//    public static final Set<StoneProperties> STONE_PROPERTIES = SafeRegistry.of(StoneProperties::setupStoneProperties);
//    /** A registry of all property groups for the config file. */
//    public static final Set<PropertyGroup> PROPERTY_GROUPS = SafeRegistry.of(PropertyGroup::setupPropertyGroups);
//    /** A registry of block entries from the config file. */
//    public static final Set<BlockEntry> BLOCK_ENTRIES = SafeRegistry.of(BlockEntry::setupEntries);
    /** A convenient reference to the current mod event bus. */
    private final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    /** A reference to Forge's main event bus. */
    private final IEventBus eventBus = MinecraftForge.EVENT_BUS;

    public Main() {
//        Cfg.register(ModLoadingContext.get().getActiveContainer());
        setupEventHandlers();
        ZipTools.copyResourcePack();
//        JarFiles.copyPresetFiles();
    }

    private void setupEventHandlers() {
        modBus.addListener(this::initCommon);
        modBus.addListener(this::initClient);
        eventBus.addListener(this::initServer);
    }

    private void initCommon(final FMLCommonSetupEvent event) {
        modBus.addListener(this::modConfig);
//        OreGen.setupOreFeatures();
    }

    private void initClient(final FMLClientSetupEvent event) {
//        modBus.addListener(ModelEventHandler::onTextureStitch);
//        modBus.addListener(ModelEventHandler::onModelBake);
    }

    private void initServer(final FMLServerStartingEvent event) {
//        FurnaceRecipes.handleRecipes(event.getServer().getRecipeManager());
    }

    private void modConfig(final ModConfig.ModConfigEvent event) {}
}
