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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.advancements.DynamicTrigger;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.config.JsonReader;
import personthecat.mod.config.ModConfigReader;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.proxy.CommonProxy;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.RegistryHandler;
import personthecat.mod.util.handlers.SpriteHandler;
import personthecat.mod.world.gen.DisableVanillaOreGen;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES)
public class Main
{
	public static boolean isBaseMetalsLoaded() {return Loader.isModLoaded("basemetals");}
	public static boolean isBiomesOPlentyLoaded() {return Loader.isModLoaded("bioemsoplenty");}
	public static boolean isGlassHeartsLoaded() {return Loader.isModLoaded("glasshearts");}
	public static boolean isIceAndFireLoaded() {return Loader.isModLoaded("iceandfire");}
	public static boolean isQuarkLoaded() {return Loader.isModLoaded("quark");}
	public static boolean isSimpleOresLoaded() {return Loader.isModLoaded("simpleores");}
	public static boolean isThermalFoundationLoaded() {return Loader.isModLoaded("thermalfoundation");}
	
	@Instance
	public static Main instance;
	
	@SidedProxy(clientSide = Reference.CLIENT, serverSide = Reference.COMMON)
	public static CommonProxy proxy;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		ConfigFile.preInit();
		if (isQuarkLoaded()) ModConfigReader.readQuarkConfig();
		RegistryHandler.registerDefaultProperties();
		JsonReader.loadNewProperties();
	}
	
	@SideOnly(value = Side.CLIENT)
	@EventHandler
	public static void preInitClient(FMLPreInitializationEvent event)
	{
		SpriteHandler.testForResourcePack();
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		MinecraftForge.ORE_GEN_BUS.register(DisableVanillaOreGen.class);
		RegistryHandler.otherRegistries();
		FurnaceRecipes.addRecipes();
	}
	
	@EventHandler
	public static void postInit(FMLPostInitializationEvent event)
	{
		DynamicTrigger.loadAdvancementList();
	}	
}