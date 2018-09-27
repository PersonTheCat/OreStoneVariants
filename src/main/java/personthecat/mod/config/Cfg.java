package personthecat.mod.config;

import static personthecat.mod.Main.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import personthecat.mod.objects.blocks.BlockEntry;
import personthecat.mod.objects.blocks.BlockGroup;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.CommonMethods;
import personthecat.mod.util.Reference;

/**
 * What can I say? Using an annotation config took more lines. ¯\_(">)_/¯
 * 
 * @author PersonTheCat
 */

@EventBusSubscriber
@Config(modid = Reference.MODID, category = "")
public class Cfg
{
	/**
	 * @Ignore had not been implemented yet.
	 * Creates permanent registers for temporary fields.
	 */
	private static class ConfigIgnore
	{
		private static String[] requestedCustomOres = null;
		
		private static final Map<String, Boolean> 
		
			MOD_SUPPORT_MAP = new HashMap<>(),
			MOD_GENERATION_MAP = new HashMap<>();
			
		private static final String
		
			ADD_BLOCKS = "dynamic blocks.variant adder",
			ADD_BLOCK_GROUPS = "dynamic blocks.block groups",
			ADD_PROPERTY_GROUPS = "dynamic blocks.property groups",
			ENABLE_MODS = "mod support.enable mods",
			MOD_GENERATION = "mod support.mod generation hax";
		
		private static Configuration config;
		
		private static boolean configChanged;
		
		static { loadConfigVar(); }
		
		private static void loadConfigVar()
		{
			File configFile = new File(Loader.instance().getConfigDir(), Reference.MODID + ".cfg");
			ConfigIgnore.config = new Configuration(configFile);
			ConfigIgnore.config.load();
		}
	}
	
	@SubscribeEvent
	public static void onConfigChangedEvent(OnConfigChangedEvent event)
	{
		if (event.getModID().equals(Reference.MODID))
		{
			sync();
		}
	}
	
	public static void sync()
	{
		ConfigManager.sync(Reference.MODID, Type.INSTANCE);
	}
	
	public static void conditionalSync()
	{
		if (ConfigIgnore.configChanged) sync();
	}
	
	public static void configChanged()
	{
		ConfigIgnore.configChanged = true;
	}
	
	@Name("Blocks")
	@LangKey("cfg.blocks")
	public static BlocksCat blocksCat;
	
	@Name("Dense Ore Variants")
	@LangKey("cfg.dense")
	public static DenseCat denseCat;
	
	@Name("Dynamic Blocks")
	@LangKey("cfg.dynamicBlocks")
	public static BlockRegistryCat blockRegistryCat;
	
	@Name("Mod Support")
	@LangKey("cfg.modSupport")
	public static ModSupportCat modSupportCat;
	
	@Name("World Generation Settings")
	@LangKey("cfg.world")
	public static WorldCat worldCat;
	
	public static class BlocksCat
	{
		@Name("Miscellaneous Block Settings")
		@LangKey("cfg.blocks.misc")
		public static MiscCat miscCat;
		
		@Name("Disable Ore Variants")
		@LangKey("cfg.blocks.disable")
		public static DisableOresCat disableOresCat;
		
		@Name("Ore Variant Drop Settings")
		@LangKey("cfg.blocks.drop")
		public static OreDropCat oreDropCat;
		
		public static class MiscCat
		{
			@Comment(
			{"These settings can be changed per-client.",
			 "",
			 "Set this to true if you're using a resource pack or overlay textures with transparency for a better appearance."})
			@Name("Overlays are shaded / are not highlighted")
			@LangKey("cfg.blocks.misc.overlaysShaded")
			public static boolean shade = false;
			
			@Comment(
			{"####################################################################################################### #",
			 "Shade Overrides",
			 "------------------------------------------------------------------------------------------------------- #",	
			 "Add the names of any blocks you would like to be shaded or not shaded, opposite of the global setting.",
		     "For all blocks, the name follows this model (\"minecraft\" and \"0\" are never written):",
		     "",
		     "         oreType_bgBlockNamespace_bgBlockName or oreType_bgBlockNamespace_bgBlockName_metaValue",
		     "                Example 1:  coal_ore_stone or diamond_ore_sand_1",
		     "                Example 2:  basemetals_copper_ore_quark_limestone",
		     "                Example 3:  coal_ore",
		     "",
		     "You do have to put the name of the mod for each ore type and for each stone type (unless vanilla). See example 2.",
		     "You can simply put the ore type and all ores of that type will be overriden. See example 3.",
		     "####################################################################################################### #"})
			@Name("Shade Overrides")
			@LangKey("cfg.blocks.misc.shadeOverrides")
			public static String[] shadeOverrides = new String[0];
			
			@Comment(
			{"To enable built-in textures with shaded backgrounds.",
			 "Supports a number of blocks, including those from Biomes O' Plenty and Glass Hearts, for sylistic consistency.",
			 "This may effect resource packs. Check inside of /config/ore_stone_variants_mods/resources.zip for a way around it."})
			@Name("Use Built-in Blended Textures")
			@LangKey("cfg.blocks.misc.blendedTextures")
			public static boolean blendedTextures = true;
			
			@Comment(
			 "Experimental. Setting this to true will disable the overlay transparency for better compatibility with shaders.")
			@Name("Disable Transparency")
			@LangKey("cfg.blocks.misc.transparency")
			public static boolean noTranslucent = false;
			
			@Name("Enable Advancements")
			@LangKey("cfg.blocks.misc.enableAdvancements")
			@RequiresWorldRestart
			public static boolean enableAdvancements = true;
			
			@Comment(
			{"Variants will imitate the properties of their background blocks, such as the ability to fall like sand,",
			 "sustain leaves, etc."})
			@Name("Background Block Imitation")
			@LangKey("cfg.blocks.misc.backgroundBlockImitation")
			@RequiresMcRestart
			public static boolean bgBlockImitation = true;
			
			@Comment(
			 "These often do not generate well, as of now.")
			@Name("Generate Overlays from Resource Pack")
			@LangKey("cfg.blocks.misc.resourcePackOverlays")
			public static boolean overlaysFromRP = false;
			
			@Name("Enable Furnace Recipes")
			@LangKey("cfg.blocks.misc.enableFurnaceRecipes")
			@RequiresMcRestart
			public static boolean enableFurnaceRecipes = true;
			
			@Name("Enable Ore Dictionary")
			@LangKey("cfg.blocks.misc.enableOreDictionary")
			@RequiresMcRestart
			public static boolean enableOreDictionary = true;
		}
		
		public static class DisableOresCat
		{			
			@Comment(
			{"This will automatically disable vanilla support (stone, andesite, diorite, and granite)",
			 "in the presence of any mod listed here."})
			@Name("Automatically Disable Vanilla Variants")
			@LangKey("cfg.blocks.disable.autoVanilla")
			@RequiresMcRestart
			public static String[] autoDisableVanillaVariants = new String[]
			{
			 	"mineralogy",
			 	"undergroundbiomes"
			};
		}
		
		public static class OreDropCat
		{
			@Name("Variants Drop")
			@LangKey("cfg.blocks.drop.variantsDrop")
			public static boolean variantsDrop = false;
			
			@Name("Variants Still Drop with Silk Touch")
			@LangKey("cfg.blocks.drop.variantsDropSilkTouch")
			public static boolean variantsDropWithSilkTouch = true;
		}
	}
	
	public static class DenseCat
	{
		@Name("General Dense Ore Settings")
		@LangKey("cfg.dense.general")
		public static GeneralDenseCat generalDenseCat;
		
		public static class GeneralDenseCat
		{
			@Comment(
			"Adds a second dense variant of every ore. Drops 1-3 ores instead of just 1.")
			@Name("Enable Dense Variants")
			@LangKey("cfg.dense.general.enable")
			@RequiresMcRestart
			public static boolean denseVariants = false;
			
			@Comment(
			"The 0 - 1 chance that dense ores will spawn.")
			@Name("Dense Ore Frequency")
			@LangKey("cfg.dense.general.frequency")
			@RangeDouble(min = 0.0, max = 1.0)
			@RequiresWorldRestart
			public static double denseVariantFrequency = 0.09;
		}
	}
	
	public static class BlockRegistryCat
	{
		@Comment(
		{"You can add as many new ore types as you like using any background block at all, blocks from other mods",
		 "included. A block model will be dynamically generated for each block and they will automatically be added",
		 "to the world generation, where they will generate in the correct blocks (within height restrictions per",
		 "ore type). The ores retain all properties of their original counterparts. These blocks currently obey",
		 "global shade settings, but can still be overridden per-block. The easiest way to find out which name to",
		 "enter is to press f3 + h in-game to see the block's full name.",
		 "",
		 "This is the basic syntax: \"<properties>, <blocks>\" OR \"<properties> <blocks>\"",
		 "",
		 "<properties> can refer to one of several things:",
		 "",
		 "    * The name of the properties, listed under \"Compatible Ores:\"",
		 "    * The name of the property group, defined below.",
		 "    * \"all,\" which uses any valid properties from the property groups below.",
		 "    * \"default,\" which uses any properties listed in the default groups below.",
		 "    * The registry name of the ore representing the properties, i.e. \"minecraft:diamond_ore.\"",
		 "        * if no corresponding properties are found, the properties will be handled dynamically.",
		 "          In this case, the ore will generate under the same conditions as those of gold_ore,",
		 "          because world generation properties cannot be handled dynamically. Additionally,",
		 "          no texture will be generated. You will need to create a property mod or manually",
		 "          supply one to work around this.",
		 "",
		 "<blocks> can refer to one of several things:",
		 "",
		 "    * The registry name of the block.",
		 "        * e.g. \"minecraft:sandstone\" or just \"sandstone\"",
		 "        * Use an asterisk (*) in place of the state number to retrieve all applicable properties",
		 "          for that block.",
		 "            * e.g. \"stained_hardened_clay:*\" instead of \"stained_hardened_clay:0,\"",
		 "              \"stained_hardened_clay:1,\" \"stained_hardened_clay:2,\" etc.",
		 "    * The name of the block group, defined below.",
		 "    * \"all,\" which will place the properties inside of each of the registered block groups.",
		 "    * \"default,\" which will place the properties inside of each of the default block groups.",
		 "",
		 "      Comma delimited:",
		 "",
		 "          Example 1:  coal_ore, minecraft:sandstone:0",
		 "          Example 2:  iron_ore, red_sandstone",
		 "          Example 3:  minecraft, stained_hardened_clay:*",
		 "          Example 4:  custom_props, all",
		 "          Example 5:  all, stained_hardened_clay:*",
		 "",
		 "      Or, space delimited:",
		 "",
		 "          Example 6:  minecraft   minecraft",
		 "          Example 7:  custom_ore  all",
		 "          Example 8:  gold_ore    netherrack",
		 "          Example 9:  custom_ore  default",
		 "",
		 "It is possible to replace all default entries with \"default, default\" or \"all, all\" to",
		 "automatically handle spawning default variants, if you prefer having fewer entries in the list.",
		 "",
		 "          Example 10:  all, all",
		 "          Example 11:  default default",
		 "",
		 "------------------------------------------------------------------------------------------------------ #",
		 "                                        Compatible Ores:",
		 "------------------------------------------------------------------------------------------------------ #",
		 "basemetals:",
		 "",
		 "	basemetals_antimony_ore, basemetals_bismuth_ore, basemetals_copper_ore, basemetals_lead_ore,",
		 "	basemetals_mercury_ore, basemetals_nickel_ore, basemetals_pewter_ore, basemetals_platinum_ore,",
		 "	basemetals_silver_ore, basemetals_tin_ore, basemetals_zinc_ore, basemetals_adamantine_ore,",
		 "	basemetals_coldiron_ore, basemetals_cupronickel_ore, basemetals_starsteel_ore",
		 "",
		 "biomesoplenty:",
		 "",
		 "	biomesoplenty_amber_ore, biomesoplenty_malachite_ore, biomesoplenty_peridot_ore, biomesoplenty_ruby_ore,",
		 "	biomesoplenty_sapphire_ore, biomesoplenty_tanzanite_ore, biomesoplenty_topaz_ore,",
		 "	biomesoplenty_amethyst_ore",
		 "",
		 "embers:",
		 "",
		 "	embers_aluminum_ore, embers_copper_ore, embers_lead_ore, embers_nickel_ore, embers_silver_ore,",
		 "	embers_tin_ore",
		 "",
		 "glasshearts:",
		 "",
		 "	glasshearts_agate_ore, glasshearts_amethyst_ore, glasshearts_onyx_ore, glasshearts_opal_ore,",
		 "	glasshearts_ruby_ore, glasshearts_sapphire_ore, glasshearts_topaz_ore",
		 "",
		 "iceandfire:",
		 "",
		 "	iceandfire_sapphire_ore, iceandfire_silver_ore",
		 "",
		 "immersiveengineering:",
		 "",
		 "	immersiveengineering_aluminum_ore, immersiveengineering_copper_ore, immersiveengineering_lead_ore,",
		 "	immersiveengineering_nickel_ore, immersiveengineering_silver_ore, immersiveengineering_uranium_ore",
		 "",
		 "minecraft:",
		 "",
		 "	coal_ore, diamond_ore, emerald_ore, gold_ore, iron_ore, lapis_ore, redstone_ore, quartz_ore",
		 "",
		 "mineralogy:",
		 "",
		 "	mineralogy_phosphorous_ore, mineralogy_sulfur_ore",
		 "",
		 "modernmetals:",
		 "",
		 "	modernmetals_aluminum_ore, modernmetals_beryllium_ore, modernmetals_boron_ore, modernmetals_cadmium_ore,",
		 "	modernmetals_chromium_ore, modernmetals_iridium_ore, modernmetals_magnesium_ore,",
		 "	modernmetals_manganese_ore, modernmetals_osmium_ore, modernmetals_plutonium_ore, modernmetals_rutile_ore,",
		 "	modernmetals_tantalum_ore, modernmetals_thorium_ore, modernmetals_tungsten_ore, modernmetals_uranium_ore,",
		 "	modernmetals_zirconium_ore",
		 "",
		 "quark:",
		 "",
		 "	quark_biotite_ore",
		 "",
		 "simpleores:",
		 "",
		 "	simpleores_adamantium_ore, simpleores_copper_ore, simpleores_mythril_ore, simpleores_tin_ore,",
		 "	simpleores_onyx_ore",
		 "",
		 "thaumcraft:",
		 "",
		 "	thaumcraft_amber_ore, thaumcraft_cinnabar_ore",
		 "",
		 "thermalfoundation:",
		 "",
		 "	thermalfoundation_copper_ore, thermalfoundation_lead_ore, thermalfoundation_nickel_ore,",
		 "	thermalfoundation_silver_ore, thermalfoundation_tin_ore, thermalfoundation_aluminum_ore,",
		 "  thermalfoundation_iridium_ore, thermalfoundation_mithril_ore, thermalfoundation_platinum_ore"})
		@Name("Variant Adder")
		@LangKey("cfg.dynamicBlocks.adder")
		public static Registry registry;
		
		@Comment(
		{"This category is dynamic. You can actually add fields using the format below",
		 "to establish additional block groups. Groups that are listed by default can be",
		 "edited, but may not be removed. You can disable them by disabling support for",
		 "their respective mods in the mod support category."})
		@Name("Block Groups")
		@LangKey("cfg.dynamicBlocks.blockGroups")
		@RequiresMcRestart
		public static Map<String, String[]> blockGroupsCat = new HashMap<>();
		
		static
		{
			ConfigCategory cat = ConfigIgnore.config.getCategory(ConfigIgnore.ADD_BLOCK_GROUPS);
			
			for (Map.Entry<String, Property> entry : cat.entrySet())
			{
				blockGroupsCat.put(entry.getKey(), entry.getValue().getStringList());
			}
		}
		
		@Comment(
		{"This category is also dynamic and can be used to define additional property groups.",
		 "Likewise, each default group corresponds to a specific mod and therefore will be",
		 "automatically listed here if it is removed. To stop this from happening, disable",
		 "the group's respective mod in the mod support category."})
		@Name("Property Groups")
		@LangKey("cfg.dynamicBlocks.propertyGroups")
		@RequiresMcRestart
		public static Map<String, String[]> propertyGroupsCat = new HashMap<>();
		
		static
		{
			ConfigCategory cat = ConfigIgnore.config.getCategory(ConfigIgnore.ADD_PROPERTY_GROUPS);

			for (Map.Entry<String, Property> entry : cat.entrySet())
			{
				propertyGroupsCat.put(entry.getKey(), entry.getValue().getStringList());
			}
		}
		
		@Name("Ore Property Mods")
		@LangKey("cfg.dynamicBlocks.customOres")
		public static PropertyModsCat propertyModsCat;
		
		public static class Registry
		{
			@Name("Add Blocks Here")
			@LangKey("cfg.dynamicBlocks.adder.add")
			@RequiresMcRestart
			public static String[] values = new String[0];
			
			@Name("Log Skipped Entries")
			@LangKey("cfg.dynamicBlocks.adder.logSkip")
			@RequiresMcRestart
			public static boolean logSkippedEntries = true;
			
			@Name("Test For Duplicates")
			@LangKey("cfg.dynamicBlocks.adder.safety")
			@RequiresMcRestart
			public static boolean safety = true;
			
			@Comment(
			{"Determines whether to separate entries regstered using an asterisk (*) into multiple blocks.",
			 "E.g. \"coal_ore, stained_hardened_clay:*\" will create 16 separate blocks instead of one.",
			 "For backward compatibility with pre-4.0 worlds."})
			@Name("Separate Asterisk Entries.")
			@LangKey("cfg.dynamicBlocks.adder.separateAsteriskEntries")
			@RequiresMcRestart
			public static boolean separateAsteriskEntries = false;
		}
		
		public static class PropertyModsCat
		{
			@Comment(
			{"This will generate a new zip file containing most of the necessary properties for new ore types to be added",
			 "to the mod. It is not recommended to generate these from the server side.",
			 "",
	 		 "Enter the registry names of any ores you would like starter zips generated for. Expect that some values",
			 "be inaccurate. Expect these ores to not generate in the world until a WorldGenProperties json has been",
			 "manually created. Zip files will be placed in /config/ore_stone_variants_mods/.",
			 "",
			 "Instructions for use:",
			 "",
			 "\t" + "1. Enter any registry names here. They will be removed once the game has loaded.",
			 "\t" + "2. Load the game.",
			 "\t" + "3. Load any world--this is necessary for some values to be retrieved.",
			 "\t" + "4. Open or extract the zip file. Check the contents of each json. Some values are",
			 "\t" + "   estimated and thus not exact.",
			 "\t" + "5. (Optional) Manually create a WorldGenProperties json so that the ore can be",
			 "\t" + "   generated in the world by the mod. See template.zip for more info"})
			@Name("Generate Starter Mods")
			@LangKey("cfg.dynamicBlocks.customOres.generator")
			@RequiresMcRestart
			@Deprecated
			public static String[] _requestedCustomOres = new String[0];
			
			public static String[] getRequestedCustomOres()
			{
				return ConfigIgnore.requestedCustomOres;
			}
		}
	}
	
	public static class ModSupportCat
	{
		@Name("Enable Mods")
		@LangKey("cfg.modSupport.enableMods")
		@RequiresMcRestart
		@Deprecated
		public static Map<String, Boolean> _enableModsCat = new HashMap<>();
		
		public static Map<String, Boolean> getEnabledModsCat()
		{
			return ConfigIgnore.MOD_SUPPORT_MAP;
		}
		
		@Name("Mod Generation Hax")
		@LangKey("cfg.modSupport.modGeneration")
		@RequiresMcRestart
		@Deprecated
		public static Map<String, Boolean> _badHaxCat = new HashMap<>();
		
		public static Map<String, Boolean> getBadHaxCat()
		{
			return ConfigIgnore.MOD_GENERATION_MAP;
		}
	}
	
	public static class WorldCat
	{
		@Name("Dimensions")
		@LangKey("cfg.world.dimensions")
		public static GenDimensionsCat genDimensionsCat;
		
		@Name("Generator Overrides")
		@LangKey("cfg.world.replace")
		public static VanillaOverridesCat vanillaOverridesCat;
		
		@Name("New Stone Gen Variables")
		@LangKey("cfg.world.stone")
		public static StoneGenVarsCat stoneGenVarsCat;
		
		@Name("Ore Generation Settings")
		@LangKey("cfg.world.ore")
		public static OreGenCat oreGenCat;
		
		public static class GenDimensionsCat
		{
			@Comment(
			{"Mainly for performance purposes. You may try removing -1 and 1 if you don't have any blocks spawning",
			 "in the End or Nether. Or, you may need to add to this array if you want ores spawning in modded dimensions.",
			 "If this array is empty, ores will be able to spawn anywhere."})
			@Name("Dimension Whitelist")
			@LangKey("cfg.world.dimensions.whitelist")
			@RequiresWorldRestart
			public static int[] dimensionWhitelist = new int[]
			{
			 	-1, 0, 1
			};
		}
		
		public static class VanillaOverridesCat
		{
			@Comment(
			{"For better compatibility with some terrain gen mods. Set this to false if another terrain mod also",
			 "spawns patches of gravel, andesite, etc.",
			 "For best compatibility, please manually disable ore spawning from all other supported mods, or",
			 "Use the mod generation settings below to attempt to handle this automatically."})
			@Name("Replace Vanilla Ore and Stone Generation")
			@LangKey("cfg.world.replace.vanilla")
			@RequiresMcRestart
			public static boolean replaceVanillaStoneGeneration = true;
		}
		
		public static class StoneGenVarsCat
		{
			@Comment(
			{"-2 = off; -1 = half size; 0 = vanilla size; 1 = 1.33 x vanilla; 2 = 1.58 x vanilla",
			 "2 may cause a small amount of cascading gen lag.",
			 "It is recommended to use another mod, such as Cave Generator, to handle spawning",
			 "of such large stone veins."})
			@Name("Dirt Size")
			@LangKey("cfg.world.stone.dirtSize")
			@RangeInt(min = -2, max = 2)
			@RequiresWorldRestart
			public static int dirtSize = 1;
			
			@Name("Gravel Size")
			@LangKey("cfg.world.stone.gravelSize")
			@RangeInt(min = -2, max = 2)
			@RequiresWorldRestart
			public static int gravelSize = 1;
			
			@Name("Andesite Size")
			@LangKey("cfg.world.stone.andesiteSize")
			@RangeInt(min = -2, max = 2)
			@RequiresWorldRestart
			public static int andesiteSize = 1;
			
			@Name("Diorite Size")
			@LangKey("cfg.world.stone.dioriteSize")
			@RangeInt(min = -2, max = 2)
			@RequiresWorldRestart
			public static int dioriteSize = 1;
			
			@Name("Granite Size")
			@LangKey("cfg.world.stone.graniteSize")
			@RangeInt(min = -2, max = 2)
			@RequiresWorldRestart
			public static int graniteSize = 1;
			
			@Comment(
			 "-1 = half count; 0 = vanilla count; 1 = 2 x vanilla; 2 = 4 x vanilla.")
			@Name("Stone Variant Count")
			@LangKey("cfg.world.stone.stoneCount")
			@RangeInt(min = -1, max = 2)
			@RequiresWorldRestart
			public static int stoneCount = 0;
			
			@Comment(
			 "Generates stone variants in layers. 1 = y(0 - 20); 2 = y(25 - 45); 3 = y(40 - 80)")
			@Name("Generate Stone Variants in Layers")
			@LangKey("cfg.world.stone.layerToggle")
			@RequiresWorldRestart
			public static boolean stoneInLayers = false;
			
			@Name("Andesite Layer")
			@LangKey("cfg.world.stone.andesiteLayer")
			@RangeInt(min = 1, max = 3)
			@RequiresWorldRestart
			public static int andesiteLayer = 2;
			
			@Name("Diorite Layer")
			@LangKey("cfg.world.stone.dioriteLayer")
			@RangeInt(min = 1, max = 3)
			@RequiresWorldRestart
			public static int dioriteLayer = 3;
			
			@Name("graniteLayer")
			@LangKey("cfg.world.stone.graniteLayer")
			@RangeInt(min = 1, max = 3)
			@RequiresWorldRestart
			public static int graniteLayer = 1;
		}
		
		public static class OreGenCat
		{
			@Name("Biome Specific Generation")
			@LangKey("cfg.world.ore.biomeSpecific")
			@RequiresWorldRestart
			public static boolean biomeSpecificOres = true;
			
			@Comment(
			{"Ores most often generate near others of the same type.",
			 "Large ore veins become larger; small ore veins become more frequent.",
			 "World generation may be notably slower, by not severely."})
			@Name("Large Ore Clusters")
			@LangKey("cfg.world.ore.largeOreClusters")
			@RequiresWorldRestart
			public static boolean largeOreClusters = false;
			
			@Comment(
			{"The probability that any ore will spawn outside of its selected regions. Allows more types to be easily found.",
			 "Bear in mind that ore veins will still be relatively large and thus easy to find. Limited to a max of 5.0%."})
			@Name("Default Probability For Unselected Chunks")
			@LangKey("cfg.world.ore.largeClusterDefaultProbability")
			@RangeDouble(min = 0.0, max = 5.0)
			@RequiresWorldRestart
			public static double largeClusterDefaultProbability = 0.0;
		}
	}
	
	static
	{		
		testForModSupport();
		testForModGenerationDisabled();
		
		ConfigIgnore.config.save();
	}

	public static void postOrePropertyInit()
	{
		logger.info("Finalizing block registry info.");
		
		setupBlockGroups();
		setupPropertyGroups();
		ensureDefaultBlockGroupsLoaded();
		ensureDefaultPropertyGroupsLoaded();
		ensureMissingGroupsAreRegistered();

		sync();
	}
	
	private static void setupBlockGroups()
	{
		logger.info("Setting up block groups. Num to create: " + blockRegistryCat.blockGroupsCat.size());
		
		for (Map.Entry<String, String[]> entry : blockRegistryCat.blockGroupsCat.entrySet())
		{
			String groupName = entry.getKey();
			String[] memberNames = entry.getValue();
			
			logger.info("Creating block group for " + groupName + "...");			
			
			/*
			 * Ignore groups that are supposed to be disabled. 
			 * Too tedious to actually remove them.
			 */
			if (BlockGroup.isDefaultGroup(groupName))
			{
				String mod = getModFromGroup(groupName);
				
				if (!Loader.isModLoaded(mod) || !isSupportEnabled(mod))
				{
					continue;
				}
			}
			new BlockGroup.Builder(groupName, memberNames);
		}
	}
	
	private static void setupPropertyGroups()
	{
		logger.info("Setting up property groups. Num to create: " + blockRegistryCat.propertyGroupsCat.size());
		
		for (Map.Entry<String, String[]> entry : blockRegistryCat.propertyGroupsCat.entrySet())
		{
			String groupName = entry.getKey();
			String[] memberNames = entry.getValue();

			logger.info("Creating property group for " + groupName + "...");
			
			if (PropertyGroup.isDefaultGroup(groupName))
			{
				if (!Loader.isModLoaded(groupName) || !isSupportEnabled(groupName))
				{
					continue;
				}
			}
			new PropertyGroup.Builder(groupName, memberNames);
		}
	}
	
	private static void ensureDefaultBlockGroupsLoaded()
	{
		for (String modid : Reference.getSupportedModIDs())
		{
			if (isSupportEnabled(modid) && Loader.isModLoaded(modid))
			{
				if (!BlockGroup.Builder.isBuilderRegistered(modid))
				{
					for (BlockGroup.Builder builder : BlockGroup.Builder.getMatchingDefaultBuilders(modid))
					{
						builder.convertRegistry();
						
						blockRegistryCat.blockGroupsCat.put(builder.getName(), builder.getPropertyNames());
					}
				}
			}
		}
	}
	
	private static void ensureDefaultPropertyGroupsLoaded()
	{
		for (String modid : Reference.getSupportedModIDs())
		{
			if (isSupportEnabled(modid) && Loader.isModLoaded(modid))
			{
				if (!PropertyGroup.Builder.isBuilderRegistered(modid))
				{
					PropertyGroup.Builder builder = PropertyGroup.Builder.getMatchingDefaultBuidler(modid);
					
					if (builder != null)
					{
						builder.convertRegistry();
						
						blockRegistryCat.propertyGroupsCat.put(builder.getName(), builder.getPropertyNames());
					}
					else logger.info("Mod name \"" + modid + "\" does not have a default PropertyGroup. Ignoring.");	
				}
			}
		}
	}
	
	private static void ensureMissingGroupsAreRegistered()
	{		
		if (!allAllUsed())
		{
			for (String propertyGroup : PropertyGroup.Builder.POSSIBLE_MISSING_INFO)
			{
				addBlockEntry(propertyGroup, "default");
			}
		}

		PropertyGroup.Builder.POSSIBLE_MISSING_INFO.clear();
	}
	
	public static boolean allAllUsed()
	{
		for (String entry : blockRegistryCat.registry.values)
		{
			String[] split = BlockEntry.splitEntry(entry);

			String props = split[0];
			String blocks = split[1];
			
			if ((props.equals("all") || props.equals("default")) &&
				(blocks.equals("all") || blocks.equals("default")))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static void addBlockEntry(String props, String blocks)
	{
		if (!registryContainsEntry(props, blocks))
		{
			blockRegistryCat.registry.values = addToBeginning(blockRegistryCat.registry.values, props + ", " + blocks);
		}
	}
	
	public static boolean registryContainsEntry(String props, String blocks)
	{
		for (String entry : blockRegistryCat.registry.values)
		{
			String[] split = BlockEntry.splitEntry(entry);
			
			if (split[0].equals(props) && split[1].equals(blocks))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static String[] addToBeginning(String[] original, String entry)
	{
		String[] newArray = new String[original.length + 1];
		newArray[0] = entry;
		
		for (int i = 1; i < newArray.length; i++)
		{
			newArray[i] = original [i - 1];
		}
		
		return newArray;
	}
	
	private static String getModFromGroup(String group)
	{
		if (StringUtils.isNumeric(group.substring(group.length() - 1)))
		{
			group = group.substring(0, group.length() - 1);
		}
		
		return group;
	}

	public static boolean isSupportEnabled(String forMod)
	{
		if (ConfigIgnore.MOD_SUPPORT_MAP.get(forMod) == null) return false;
		
		return ConfigIgnore.MOD_SUPPORT_MAP.get(forMod);
	}
	
	private static void testForModSupport()
	{		
		List<String> propOrderModSupport = new ArrayList<>();
		
		String[] allSupportedMods = Reference.getSupportedModIDs();
		
		int index = 0;
		
		for (Map.Entry<String, String> entry : Reference.SUPPORTED_MOD_NAMES.entrySet())
		{
			String modName = entry.getKey(), formatted = entry.getValue();
			index++;
			
			if (!modName.equals("basemetals"))
			{
				Property propModSupport = ConfigIgnore.config.get(ConfigIgnore.ENABLE_MODS, formatted, true);
				
				if (index == 0)	propModSupport.setComment(
					"Set any of these to false to disable creation and spawning of new ore variants, relative to each mod.\n");
				
				propOrderModSupport.add(propModSupport.getName());

				ConfigIgnore.MOD_SUPPORT_MAP.put(modName, propModSupport.getBoolean());
			}
		}
			
		//basemetals should be listed last--comment formatting
		
		Property propBaseMetalsSupport = ConfigIgnore.config.get(ConfigIgnore.ENABLE_MODS, "Base Metals", true);
		
		propBaseMetalsSupport.setComment(
			"For easiest compatibility with Base Metals, set both using_orespawn and fallback_orespawn to false\n"
          + "in BaseMetals.cfg, and subsequently disable OreSpawn itself.\n"
          + "This is because both mods when combined will otherwise spawn twice as many ores as necessary.\n"
          + "Only if you prefer to avoid modifying the jsons under /config/orespawn3.\n");
		
		propOrderModSupport.add(propBaseMetalsSupport.getName());
		
		ConfigIgnore.MOD_SUPPORT_MAP.put("basemetals", propBaseMetalsSupport.getBoolean());
		
		//end basemetals
		
		ConfigIgnore.config.setCategoryPropertyOrder(ConfigIgnore.ENABLE_MODS, propOrderModSupport);
	}
	
	public static boolean isOriginalGenerationDisabled(String forMod)
	{
		if (ConfigIgnore.MOD_GENERATION_MAP.get(forMod) == null) return false;
		
		return ConfigIgnore.MOD_GENERATION_MAP.get(forMod);
	}
	
	private static void testForModGenerationDisabled()
	{		
		List<String> propOrderModGeneration = new ArrayList<>();
		
		String[] allSupportedMods = Reference.getSupportedModIDs();

		int index = 0;		
		
		for (Map.Entry<String, String> entry : Reference.SUPPORTED_MOD_NAMES.entrySet())
		{
			String modName = entry.getKey(), formatted = entry.getValue();
			index++;
			
			if (!modName.equals("biomesoplenty") && Reference.hasCheapHax(modName))
			{
				Property propModGeneration = ConfigIgnore.config.get(ConfigIgnore.MOD_GENERATION, formatted, false);
				
				if (index == 0)	propModGeneration.setComment(
					"Setting any of these to true will attempt to disable the default ore spawning from other mods.\n"
				  + "Recommended if you want to stop their ores from spawning in the wrong stone types, but don't\n"
				  + "feel like changing their config files. This will require starting the game twice.\n"
				  + "Once you restart your game, these will be set back to false. That is normal. Currently, this will\n"
				  + "also remove comments from other config files. That will be fixed in the future.\n");

				ConfigIgnore.MOD_GENERATION_MAP.put(modName, propModGeneration.getBoolean());
				
				propOrderModGeneration.add(propModGeneration.getName());
				
				//These settings act as toggles and are reset upon use.
				propModGeneration.set(false);
			}
		}
		
		//biomesoplenty should be listed last--comment formatting
		
		Property propBiomesOPlentyGeneration = ConfigIgnore.config.get(ConfigIgnore.MOD_GENERATION, "Biomes O' Plenty", false);
		
		propBiomesOPlentyGeneration.setComment(
			"Using this for Biomes O' Plenty will change all biome configs. It could take a while to change them\n"
          + "back, if you change your mind.\n");
		
		propOrderModGeneration.add(propBiomesOPlentyGeneration.getName());
		
		ConfigIgnore.MOD_GENERATION_MAP.put("biomesoplenty", propBiomesOPlentyGeneration.getBoolean());
		
		propBiomesOPlentyGeneration.set(false);
		
		//end biomesoplenty
		
		ConfigIgnore.config.setCategoryPropertyOrder(ConfigIgnore.MOD_GENERATION, propOrderModGeneration);
	}
	
	private static boolean entriesEqual(String entry1, String entry2)
	{
		IBlockState state1 = CommonMethods.getBlockState(entry1);
		IBlockState state2 = CommonMethods.getBlockState(entry2);
		
		return state1.equals(state2);
	}
	
	public static boolean isShadeOverridden(String forRegistryName)
	{
		for (String entry : blocksCat.miscCat.shadeOverrides)
		{
			if (entry.equals(forRegistryName)) return true;
			
			if (entry.equals(CommonMethods.getOre(forRegistryName))) return true;
		}
		
		return false;
	}
	
	public static boolean disableVanillaVariants()
	{
		for (String modName : blocksCat.disableOresCat.autoDisableVanillaVariants)
		{
			if (Loader.isModLoaded(modName)) return true;
		}
		
		return false;
	}
	
	public static boolean isGenerationDisabledGlobally()
	{
		return Loader.isModLoaded("geolosys") && isSupportEnabled("geolosys");
	}
}