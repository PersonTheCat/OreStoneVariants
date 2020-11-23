package com.personthecat.orestonevariants;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.blocks.BlockGroup;
import com.personthecat.orestonevariants.commands.CommandOSV;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.BlockInit;
import com.personthecat.orestonevariants.init.ItemInit;
import com.personthecat.orestonevariants.io.JarFiles;
import com.personthecat.orestonevariants.io.ZipTools;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGroup;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.proxy.CommonProxy;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.Reference;
import com.personthecat.orestonevariants.util.SafeRegistry;
import com.personthecat.orestonevariants.world.DisableVanillaGen;
import com.personthecat.orestonevariants.world.OreGen;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

@Mod(
    modid = Main.MODID,
    name = "Ore Stone Variants",
    version = "5.8",
    dependencies = "after:*"
)
@SuppressWarnings("unused")
public class Main {
    /** The main instance of this mod, as required by Forge. */
    @Instance public static Main instance;
    /** A setting representing this mod's namespace. */
    public static final String MODID = "osv";
    /** The primary Log4j logger used by this mod. */
    public static final Logger LOGGER = logger(MODID);
    /** A registry containing all of the items. */
    public static final Set<Item> ITEMS = SafeRegistry.of(ItemInit::setupItems);
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

    /** Used to register placeholder models on the client side. */
    @SidedProxy(clientSide = Reference.CLIENT, serverSide = Reference.COMMON)
    public static CommonProxy proxy;

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        ZipTools.copyResourcePack();
        JarFiles.copyPresetFiles();
        proxy.generateOverlays();
        proxy.enableResourcePack();
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
        RecipeHelper.handleRecipes();
        if (Cfg.WorldCat.enabled) {
            enableWorldGen();
        }
    }

    @EventHandler
    public static void serverInit(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandOSV());
    }

    private static void enableWorldGen() {
        GameRegistry.registerWorldGenerator(new OreGen(), Integer.MAX_VALUE);
        MinecraftForge.ORE_GEN_BUS.register(DisableVanillaGen.class);
    }
}