package personthecat.mod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import personthecat.mod.advancements.AdvancementMap;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.JsonReader;
import personthecat.mod.config.ModConfigReader;
import personthecat.mod.proxy.CommonProxy;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.RegistryHandler;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES)
public class Main
{
	@Instance
	public static Main instance;
	
	@SidedProxy(clientSide = Reference.CLIENT, serverSide = Reference.COMMON)
	public static CommonProxy proxy;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		ConfigFile.init();
		RegistryHandler.registerDefaultProperties();
		ModConfigReader.readQuarkConfig();		
		JsonReader.loadNewProperties();
		proxy.createAndRegisterResourcePack();
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		RegistryHandler.registerAPIComms();		
		RegistryHandler.registerGenerators();
		FurnaceRecipes.addRecipes();
		ModConfigReader.disableModGeneration();
	}
	
	@EventHandler
	public static void postInit(FMLPostInitializationEvent event)
	{
		AdvancementMap.loadAdvancementList();
	}
}