package personthecat.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import personthecat.mod.advancements.DynamicTrigger;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.config.JsonReader;
import personthecat.mod.config.ModConfigReader;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.properties.DefaultProperties;
import personthecat.mod.properties.DefaultProperties.DefaultOreProperties;
import personthecat.mod.proxy.CommonProxy;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.RegistryHandler;
import personthecat.mod.util.handlers.SpriteHandler;
import personthecat.mod.world.gen.DisableVanillaOreGen;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES)
public class Main {
	
	public static boolean isBaseMetalsLoaded, isBiomesOPlentyLoaded, isGlassHeartsLoaded, isIceAndFireLoaded, isQuarkLoaded, isSimpleOresLoaded;
	
	private static void testForMods()
	{
		isBaseMetalsLoaded = Loader.isModLoaded("basemetals");
		isBiomesOPlentyLoaded = Loader.isModLoaded("biomesoplenty");
		isGlassHeartsLoaded = Loader.isModLoaded("glasshearts");
		isIceAndFireLoaded = Loader.isModLoaded("iceandfire");
		isQuarkLoaded = Loader.isModLoaded("quark");
		isSimpleOresLoaded = Loader.isModLoaded("simpleores");
	}
	
	@Instance
	public static Main instance;
	
	@SidedProxy(clientSide = Reference.CLIENT, serverSide = Reference.COMMON)
	public static CommonProxy proxy;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{		
		testForMods();
		ConfigFile.preInit();
		if (isQuarkLoaded) ModConfigReader.readQuarkConfig();
		SpriteHandler.testForResourcePack();
		//RegistryHandler.onRegisterNewResourcesBadly();
		MinecraftForge.EVENT_BUS.register(ConfigInterpreter.class);
		MinecraftForge.EVENT_BUS.register(ModelEventHandler.class);
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		MinecraftForge.ORE_GEN_BUS.register(DisableVanillaOreGen.class);
		RegistryHandler.onDynamicTriggerRegister();
		MinecraftForge.EVENT_BUS.register(DynamicTrigger.class);
		RegistryHandler.otherRegistries();
		FurnaceRecipes.addRecipes();
	}
	
	@EventHandler
	public static void postInit(FMLPostInitializationEvent event) {}	
}