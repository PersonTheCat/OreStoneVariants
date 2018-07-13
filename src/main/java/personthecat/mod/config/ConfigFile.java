package personthecat.mod.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import personthecat.mod.properties.DefaultProperties;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.Reference;
import personthecat.mod.util.ShortTrans;

public class ConfigFile
{
	public static Configuration config = null;
	
	protected static final String 
	
		WORLD = ShortTrans.unformatted("cfg.world") + ".",
		BLOCKS = ShortTrans.unformatted("cfg.blocks") + ".",
		DYN_BLOCKS = ShortTrans.unformatted("cfg.dynamicBlocks") + ".",
		MOD_SUPPORT = ShortTrans.unformatted("cfg.modSupport") + ".",
		DENSE_ORES = ShortTrans.unformatted("cfg.dense") + ".";
	
	protected static final String 
	
		GENERATION_DIMENSIONS = WORLD + ShortTrans.unformatted("cfg.world.dimensions"),
		REPLACE_GENERATION = WORLD + ShortTrans.unformatted("cfg.world.replace"),
		STONE_GENERATION = WORLD + ShortTrans.unformatted("cfg.world.stone"),
		ORE_GENERATION = WORLD + ShortTrans.unformatted("cfg.world.ore"),
		VARIANTS_DROP = BLOCKS + ShortTrans.unformatted("cfg.blocks.drop"),
		MISCELLANEOUS = BLOCKS + ShortTrans.unformatted("cfg.blocks.misc"),
		DISABLE_ORES	= BLOCKS + ShortTrans.unformatted("cfg.blocks.disable"),
		GENERAL_DENSE = DENSE_ORES + ShortTrans.unformatted("cfg.dense.general"),
		ADD_BLOCKS = DYN_BLOCKS + ShortTrans.unformatted("cfg.dynamicBlocks.adder"),
		PROPERTY_MODS = DYN_BLOCKS + ShortTrans.unformatted("cfg.dynamicBlocks.customOres"),
		ENABLE_MODS = MOD_SUPPORT + ShortTrans.unformatted("cfg.modSupport.enableMods"),
		MOD_GENERATION = MOD_SUPPORT + ShortTrans.unformatted("cfg.modSupport.modGeneration");
	
	private static final Map<String, Boolean> 
	
		MOD_SUPPORT_MAP = new HashMap<>(),
		MOD_GENERATION_MAP = new HashMap<>();
	
	/*
	 * To-do (MC 1.13?): Switch to an annotation-based config class to simplify 
	 * things even further and get automatic GUI support. 
	 */
	
	public static boolean
	
		//World Generation
		replaceVanillaStoneGeneration, stoneInLayers, automaticQuartzVariants,
		biomeSpecificOres, largeOreClusters,
		
		//Drops
		variantsDrop, variantsDropWithSilkTouch,
		
		//Textures
		shade, blendedTextures,	noTranslucent, overlaysFromRP,
		
		//Miscellaneous
		enableAdvancements, denseVariants, bgBlockImitation;
	
	public static double 
		
		denseVariantFrequency, largeClusterDefaultProbability;
	
	public static int
	
		dirtSize, gravelSize, andesiteSize, dioriteSize, graniteSize,
		stoneCount, andesiteLayer, dioriteLayer, graniteLayer;
	
	public static int[] 
		
		dimensionWhitelist;
	
	public static String[] 
	
		shadeOverrides, disabledOres, dynamicBlocks, autoDisableVanillaVariants, requestedCustomOres;
	
	public static void init()
	{		
		File configFile = new File(Loader.instance().getConfigDir(), Reference.MODID + ".cfg");
		config = new Configuration(configFile);	
		syncConfig();

		postInit();
	}
	
	private static void postInit()
	{
		DefaultProperties.postConfig();
		setVariantAdderCommentary();
		ConfigInterpreter.loadInterpreter();
	}

	public static Configuration getConfig()
	{
		return config;
	}
	
	public static boolean isSupportEnabled(String forMod)
	{
		if (MOD_SUPPORT_MAP.get(forMod) == null) return false;
		
		return MOD_SUPPORT_MAP.get(forMod);
	}
	
	private static void testForModSupport()
	{		
		List<String> propOrderModSupport = new ArrayList<>();
		
		for (String modName : getAllSupportedMods())
		{
			if (!modName.equals("basemetals"))
			{
				Property propModSupport = config.get(ENABLE_MODS, ShortTrans.unformatted("cfg.modSupport.enableMods." + modName), true);
				
				if (getAllSupportedMods().indexOf(modName) == 0)
				{
					propModSupport.setComment(
						"Set any of these to false to disable creation and spawning of new ore variants, relative to each mod.\n");
				}
				
				propOrderModSupport.add(propModSupport.getName());

				MOD_SUPPORT_MAP.put(modName, propModSupport.getBoolean());
			}
		}	
			
		//basemetals should be listed last--comment formatting
		
		Property propBaseMetalsSupport = config.get(ENABLE_MODS, ShortTrans.unformatted("cfg.modSupport.enableMods.basemetals"), true);
			
		propBaseMetalsSupport.setComment(
			"For easiest compatibility with Base Metals, set both using_orespawn and fallback_orespawn to false\n"
          + "in BaseMetals.cfg, and subsequently disable OreSpawn itself.\n"
          + "This is because both mods when combined will otherwise spawn twice as many ores as necessary.\n"
          + "Only if you prefer to avoid modifying the jsons under /config/orespawn3.\n");
		
		propOrderModSupport.add(propBaseMetalsSupport.getName());
		
		MOD_SUPPORT_MAP.put("basemetals", propBaseMetalsSupport.getBoolean());
		
		//end basemetals
		
		config.setCategoryPropertyOrder(ENABLE_MODS, propOrderModSupport);
		
		config.save();
	}
	
	public static boolean isOriginalGenerationDisabled(String forMod)
	{
		if (MOD_GENERATION_MAP.get(forMod) == null) return false;
		
		return MOD_GENERATION_MAP.get(forMod);
	}
	
	private static void testForModGenerationDisabled()
	{		
		List<String> propOrderModGeneration = new ArrayList<>();
		
		List<String> allSupportedMods = getAllSupportedMods();
		
		//These do not support hacky config file changes
		Arrays.asList(new String[]
		{
		 	"mineralogy", "quark", "vanilla", "undergroundbiomes",
		 	"thermalfoundation", "earthworks", "modernmetals",
		 	"geolosys"
		}
		).forEach(mod -> allSupportedMods.remove(mod));

		for (String modName : allSupportedMods)
		{
			if (!modName.equals("biomesoplenty") && !modName.equals("quark"))
			{
				Property propModGeneration = config.get(MOD_GENERATION, ShortTrans.unformatted("cfg.modSupport.modGeneration." + modName), false);
				
				if (allSupportedMods.indexOf(modName) == 0)
				{
					propModGeneration.setComment(
							"Setting any of these to true will attempt to disable the default ore spawning from other mods.\n"
						  + "Recommended if you want to stop their ores from spawning in the wrong stone types, but don't\n"
						  + "feel like changing their config files. This will require starting the game twice.\n"
						  + "Once you restart your game, these will be set back to false. That is normal. Currently, this will\n"
						  + "also remove comments from other config files. That will be fixed in the future.\n");
				}

				MOD_GENERATION_MAP.put(modName, propModGeneration.getBoolean());
				
				propOrderModGeneration.add(propModGeneration.getName());
				
				//These settings act as toggles and are reset upon use.
				propModGeneration.set(false);
			}
		}
		
		//biomesoplenty should be listed last--comment formatting
		
		Property propBiomesOPlentyGeneration = config.get(MOD_GENERATION, ShortTrans.unformatted("cfg.modSupport.modGeneration.biomesoplenty"), false);
		
		propBiomesOPlentyGeneration.setComment(
			"Using this for Biomes O' Plenty will change all biome configs. It could take a while to change them\n"
          + "back, if you change your mind.\n");
		
		propOrderModGeneration.add(propBiomesOPlentyGeneration.getName());
		
		propBiomesOPlentyGeneration.set(false);
		
		MOD_GENERATION_MAP.put("biomesoplenty", propBiomesOPlentyGeneration.getBoolean());
		
		//end biomesoplenty
		
		config.setCategoryPropertyOrder(MOD_GENERATION, propOrderModGeneration);
		
		config.save();
	}
	
	public static boolean isShadeOverridden(String forRegistryName)
	{
		for (String entry : shadeOverrides)
		{
			if (entry.equals(forRegistryName)) return true;
			
			if (entry.equals(NameReader.getOre(forRegistryName))) return true;
		}
		
		return false;
	}
	
	private static void setVariantAdderCommentary()
	{
		config.setCategoryComment(ADD_BLOCKS, 
				  "You can add as many new ore types as you like using any background block at all, blocks from other mods\n"
				+ "included. A block model will be dynamically generated for each block and they will automatically be added\n"
				+ "to the world generation, where they will generate in the correct blocks (within height restrictions per\n"
				+ "ore type). The ores retain all properties of their original counterparts. These blocks currently obey\n"
				+ "global shade settings, but can still be overridden per-block. The easiest way to find out which name to\n"
				+ "enter is to press f3 + h in-game to see the block's full name.\n\n"
				
				+ "This is the basic syntax:  ore_type, domain:block_name:(with or without meta)\n"
				+ "The domain is also configured to be optional (defaults to Minecraft:) \n\n"
				
				+ "                Example 1:  coal_ore, minecraft:sandstone:0\n"
				+ "                Example 2:  iron_ore, red_sandstone\n"
				+ "                Example 3:  minecraft, stained_hardened_clay:6\n"
				+ "                Example 4:  coal_ore, stained_hardened_clay:*\n"
				+ "                Example 5:  simpleores, stained_hardened_clay:*\n\n"
				
				+ "You can also enter the given mod's namespace in place of \"x_ore\" and it will create all of the mod's\n"
				+ "ore types inside of that block. See example 3.\n"
				+ "If you would like to add all blockstates for any given block, substitute the block's meta with an asterisk (*).\n"
				+ "See examples 4 and 5.\n\n"
				+ "Formatting: Just place a comma between the ore type and the background block. Spaces are ignored.\n\n"
				
				+ "                                        Compatible Ores:\n"
				
				+ DefaultProperties.DefaultOreProperties.getFormattedBlockList());
				
		config.save();
	}
	
	public static boolean disableVanillaVariants()
	{
		for (String modName : autoDisableVanillaVariants)
		{
			if (Loader.isModLoaded(modName)) return true;
		}
		
		return false;
	}
	
	public static boolean isGenerationDisabledGlobally()
	{
		return Loader.isModLoaded("geolosys") && isSupportEnabled("geolosys");
	}

	private static List<String> getAllSupportedMods()
	{		
		List<String> allModNames = new ArrayList<>();
		
		allModNames.add("vanilla");
		
		String[] allDependencies = Reference.DEPENDENCIES.replaceAll("after:", "").replaceAll("before:", "").replace("\t", "").split(";");
		
		for (String modName : allDependencies)
		{
			if (!modName.trim().isEmpty()) allModNames.add(modName);
		}
		
		return allModNames;
	}
	
	private static void syncConfig()
	{
		config.load();
		
		Property propDimensionGeneration = config.get(GENERATION_DIMENSIONS, Translations.dimensionWhitelist, new int[] {-1, 0, 1},
		    "Mainly for performance purposes. You may try removing -1 and 1 if you don't have any blocks spawning\n"
          + "in the End or Nether. Or, you may need to add to this array if you want ores spawning in modded dimensions.\n");
		
		Property propReplaceVanillaStoneGeneration = config.get(REPLACE_GENERATION, Translations.replaceVanillaStoneGeneration, true,
		    "For better compatibility with some terrain gen mods. Set this to false if another terrain mod also\n"
          + "spawns patches of gravel, andesite, etc.\n"
          + "Future builds will provide options to attempt to disable other mods' ore spawning. For now, please\n"
          + "disable ore spawning for iceandfire and simpleores manually in their config files for best results.\n");

		Property propDirtSize = config.get(STONE_GENERATION, Translations.dirtSize, 0, 
		    "-2 = off; -1 = half size; 0 = vanilla size; 1 = 1.33 x vanilla; 2 = 1.58 x vanilla\n"
          + "(2 may cause a small amount of cascading gen lag).\n");
		
		Property propGravelSize = config.get(STONE_GENERATION, Translations.gravelSize, 0);
		Property propAndesiteSize = config.get(STONE_GENERATION, Translations.andesiteSize, 0);
		Property propDioriteSize = config.get(STONE_GENERATION, Translations.dioriteSize, 0);
		Property propGraniteSize = config.get(STONE_GENERATION, Translations.graniteSize, 0);

		Arrays.asList(new Property[] {propDirtSize, propGravelSize, propAndesiteSize, propDioriteSize, propGraniteSize})
		    .forEach(prop -> setValueRange(prop, -2, 2));
		
		Property propStoneCount = config.get(STONE_GENERATION, Translations.stoneCount, 0,
		    "-1 = half count; 0 = vanilla count; 1 = 2 x vanilla; 2 = 4 x vanilla.\n");
		    setValueRange(propStoneCount, -1, 2);

		Property propStoneInLayers = config.get(STONE_GENERATION, Translations.stoneInLayers, false,
		    "Generates stone variants in layers. 1 = y(0 - 20); 2 = y(25 - 45); 3 = y(40 - 80).\n");
		
		Property propAndesiteLayer = config.get(STONE_GENERATION, Translations.andesiteLayer, 2);
		Property propDioriteLayer = config.get(STONE_GENERATION, Translations.dioriteLayer, 3);
		Property propGraniteLayer = config.get(STONE_GENERATION, Translations.graniteLayer, 1);
		
		Arrays.asList(new Property[] {propAndesiteLayer, propDioriteLayer, propGraniteLayer})
		    .forEach(prop -> setValueRange(prop, 1, 3));
		
		Property propBiomeSpecific = config.get(ORE_GENERATION, Translations.biomeSpecificOres, true);
		Property propAutomaticQuartz = config.get(ORE_GENERATION, Translations.automaticQuartzVariants, false);
		Property propLargeOreClusters = config.get(ORE_GENERATION, Translations.largeOreClusters, false,
		    "Ores most often generate near others of the same type.\n"
		  + "Large ore veins become larger; small ore veins become more frequent.\n"
		  + "World generation may be notably slower, but not severely.\n");
		
		Property propLargeClusterDefaultProbability = config.get(ORE_GENERATION, Translations.largeClusterDefaultProbability, 0.0,
		    "The probability that any ore will spawn outside of its selected regions. Allows more types to be easily found.\n"
		  + "Bear in mind that ore veins will still be relatively large and thus easy to find. Limited to a max of 5.0%.\n");
			setValueRange(propLargeClusterDefaultProbability, 0.0, 5.0);
		
		Property propVariantsDrop = config.get(VARIANTS_DROP, Translations.variantsDrop, false,
		    "These settings are server-wide.\n");
		
		Property propVariantsDropWithSilkTouch = config.get(VARIANTS_DROP, Translations.variantsDropWithSilkTouch, true);
		
		config.setCategoryComment(MISCELLANEOUS, 
			    "Add the names of any blocks you would like to be shaded or not shaded, opposite of the global setting.\n"
			  + "For custom blocks, the name follows this model:\n\n"
			  + "			oreType_ore_backgroundBlockName or oreType_ore_backgroundBlockName_metaValue\n"
			  + "                Example 1:  coal_ore_stone or diamond_ore_sand_1\n"
			  + "                Example 2:  basemetals_copper_ore_quark_limestone\n"
			  + "                Example 3:  coal_ore\n\n"
			  + "You do have to put the name of the mod for each ore type and for each stone type (unless vanilla). See example 2.\n"
			  + "You can simply put the ore type and all ores of that type will be overriden. See example 3.");
		
		Property propShade = config.get(MISCELLANEOUS, Translations.shade, false,
		    "These settings can be changed per-client.\n\n"
          + "Set this to true if you're using a resource pack or overlay textures with transparency for a better appearance.\n");
		
		Property propShadeOverrides = config.get(MISCELLANEOUS, Translations.shadeOverrides, new String[] {});
		
		Property propBlendedTextures = config.get(MISCELLANEOUS, Translations.blendedTextures, true,
		    "To enable built-in textures with shaded backgrounds.\n"
          + "Supports a number of blocks, including those from Biomes O' Plenty and Glass Hearts, for sylistic consistency.\n"
          + "This may effect resource packs. Check inside of /config/ore_stone_variants_mods/resources.zip for a way around it.\n");
		
		Property propNoTranslucent = config.get(MISCELLANEOUS, Translations.noTranslucent, false,
		    "Experimental. Setting this to true will disable the overlay transparency for better compatibility with shaders.\n");
		
		Property propEnableAdvancements = config.get(MISCELLANEOUS, Translations.enableAdvancements, true);
		
		Property propBgBlockImitation = config.get(MISCELLANEOUS, Translations.bgBlockImitation, true,
		    "Variants will imitate the properties of their background blocks, such as the ability to fall like sand,\n"
		  + "sustain leaves, etc.");
		
		Property propOverlaysFromRP = config.get(MISCELLANEOUS, Translations.overlaysFromRP, false,
		    "These often do not generate well, as of now.\n");
		
		Property propDisableOres = config.get(DISABLE_ORES, Translations.disabledOres, new String[] {},
		    "Enter the names of any ores you would like to not be automatically created by the mod.\n"
	      + "A full list of applicable ores can be found under \"Variant Adder.\"\n");
		
		Property propAutoDisableVanillaVariants = config.get(DISABLE_ORES, Translations.autoDisableVanillaVariants, new String[] {"mineralogy", "undergroundbiomes"},
		    "This will automatically disable vanilla ore variants (stone, andesite, diorite, and granite)\n"
          + "in the presence of any mod listed here.\n");
		
		Property propAddBlocks = config.get(ADD_BLOCKS, Translations.dynamicBlocks, new String[] {""});
		
		Property propCustomModGenerator = config.get(PROPERTY_MODS, Translations.requestedCustomOres, new String[] {""},
		    "This will generate a new zip file containing most of the necessary properties for new ore types to be added\n"
		  + "to the mod. It is not recommended to generate these from the server side.\n\n"
		    		
		  + "Enter the registry names of any ores you would like starter zips generated for. Expect that some values\n"
		  + "be inaccurate. Expect these ores to not generate in the world until a WorldGenProperties json has been\n"
		  + "manually created. Zip files will be placed in /config/ore_stone_variants_mods/.\n\n"
		  
		  + "Instructions for use:\n\n"

		  + "\t" + "1. Enter any registry names here. They will be removed once the game has loaded.\n"
		  + "\t" + "2. Load the game.\n"
		  + "\t" + "3. Load any world--this is necessary for some values to be retrieved.\n"
		  + "\t" + "4. Open or extract the zip file. Check the contents of each json. Some values are\n"
		  + "\t" + "   estimated and thus not exact.\n"
		  + "\t" + "5. (Optional) Manually create a WorldGenProperties json so that the ore can be\n"
		  + "\t" + "   generated in the world by the mod. See template.zip for more info\n");
		
		Property propDenseVariants = config.get(GENERAL_DENSE, Translations.denseVariants, false,
		    "Adds a second dense variant of every ore. Drops 1-3 ores instead of just 1.\n");
		
		Property propDenseRatio = config.get(GENERAL_DENSE, Translations.denseVariantFrequency, 0.09d,
		    "The 0 - 1 chance that dense ores will spawn.\n");
		    setValueRange(propDenseRatio, 0.0D, 1.0D);
		    
		List<String> dimGen = new ArrayList<>();
		
			dimGen.add(propDimensionGeneration.getName());
		
		config.setCategoryPropertyOrder(GENERATION_DIMENSIONS, dimGen);

		List<String> repGen = new ArrayList<>();
		
			repGen.add(propReplaceVanillaStoneGeneration.getName());
		
		config.setCategoryPropertyOrder(REPLACE_GENERATION, repGen);
		
		List<String> stnGen = new ArrayList<>();
		
			stnGen.add(propDirtSize.getName());
			stnGen.add(propGravelSize.getName());
			stnGen.add(propAndesiteSize.getName());
			stnGen.add(propDioriteSize.getName());
			stnGen.add(propGraniteSize.getName());
			stnGen.add(propStoneCount.getName());
			stnGen.add(propStoneInLayers.getName());
			stnGen.add(propAndesiteLayer.getName());
			stnGen.add(propDioriteLayer.getName());
			stnGen.add(propGraniteLayer.getName());
		
		config.setCategoryPropertyOrder(STONE_GENERATION, stnGen);

		List<String> oreGen = new ArrayList<>();
		
			oreGen.add(propBiomeSpecific.getName());
			oreGen.add(propAutomaticQuartz.getName());
			oreGen.add(propLargeOreClusters.getName());
			
		
		config.setCategoryPropertyOrder(ORE_GENERATION, oreGen);
		
		List<String> varDrop = new ArrayList<>();
		
			varDrop.add(propVariantsDrop.getName());
			varDrop.add(propVariantsDropWithSilkTouch.getName());
		
		config.setCategoryPropertyOrder(VARIANTS_DROP, varDrop);

		List<String> brokenMod = new ArrayList<>();		
	 	
			brokenMod.add(propShade.getName());
			brokenMod.add(propShadeOverrides.getName());
			brokenMod.add(propBlendedTextures.getName());
			brokenMod.add(propNoTranslucent.getName());
			brokenMod.add(propEnableAdvancements.getName());
			brokenMod.add(propBgBlockImitation.getName());
			brokenMod.add(propOverlaysFromRP.getName());
		
		config.setCategoryPropertyOrder(MISCELLANEOUS, brokenMod);

		List<String> disOres = new ArrayList<>();
		
			disOres.add(propDisableOres.getName());
			disOres.add(propAutoDisableVanillaVariants.getName());
		
		config.setCategoryPropertyOrder(DISABLE_ORES, disOres);
		
		List<String> addBlocks = new ArrayList<>();
		
			addBlocks.add(propAddBlocks.getName());
		
		config.setCategoryPropertyOrder(ADD_BLOCKS, addBlocks);
		
		List<String> customOres = new ArrayList<>();
		
			customOres.add(propCustomModGenerator.getName());
			
		config.setCategoryPropertyOrder(PROPERTY_MODS, customOres);
		
		List<String> generalDense = new ArrayList<>();
		
			generalDense.add(propDenseVariants.getName());
			generalDense.add(propDenseRatio.getName());
		
		config.setCategoryPropertyOrder(GENERAL_DENSE, generalDense);
		
		dimensionWhitelist = propDimensionGeneration.getIntList();
		replaceVanillaStoneGeneration = propReplaceVanillaStoneGeneration.getBoolean();
		largeOreClusters = propLargeOreClusters.getBoolean();
		largeClusterDefaultProbability = propLargeClusterDefaultProbability.getDouble();
		dirtSize = getAdjustedVeinSize(propDirtSize.getInt());
		gravelSize = getAdjustedVeinSize(propGravelSize.getInt());
		andesiteSize = getAdjustedVeinSize(propAndesiteSize.getInt());
		dioriteSize = getAdjustedVeinSize(propDioriteSize.getInt());
		graniteSize = getAdjustedVeinSize(propGraniteSize.getInt());
		stoneCount = propStoneCount.getInt();
		stoneInLayers = propStoneInLayers.getBoolean();
		andesiteLayer = propAndesiteLayer.getInt();
		dioriteLayer = propDioriteLayer.getInt();
		graniteLayer = propGraniteLayer.getInt();
		biomeSpecificOres = propBiomeSpecific.getBoolean();
		automaticQuartzVariants = propAutomaticQuartz.getBoolean();
		variantsDrop = propVariantsDrop.getBoolean();
		variantsDropWithSilkTouch = propVariantsDropWithSilkTouch.getBoolean();
		shade = propShade.getBoolean();
		shadeOverrides = propShadeOverrides.getStringList();
		disabledOres = propDisableOres.getStringList();
		autoDisableVanillaVariants = propAutoDisableVanillaVariants.getStringList();
		blendedTextures = propBlendedTextures.getBoolean();
		enableAdvancements = propEnableAdvancements.getBoolean();
		noTranslucent = propNoTranslucent.getBoolean();
		overlaysFromRP = propOverlaysFromRP.getBoolean();
		bgBlockImitation = propBgBlockImitation.getBoolean();
		dynamicBlocks = propAddBlocks.getStringList();
		requestedCustomOres = propCustomModGenerator.getStringList();
		denseVariants = propDenseVariants.getBoolean();
		denseVariantFrequency = propDenseRatio.getDouble();
		
		testForModSupport();
		testForModGenerationDisabled();
		
		propCustomModGenerator.set(new String[0]);
		
		if(config.hasChanged()) config.save();
	}
	
	private static void setValueRange(Property prop, int min, int max)
	{
		prop.setMinValue(min);
		prop.setMaxValue(max);
	}
	
	private static void setValueRange(Property prop, double min, double max)
	{
		prop.setMinValue(min);
		prop.setMaxValue(max);
	}
	
	private static int getAdjustedVeinSize(int fromInt)
	{
		return fromInt == -2 ? 0 :
		       fromInt == -1 ? 15 :
		       fromInt == 0 ? 33 :
		       fromInt == 1 ? 44 :
		       fromInt == 2 ? 52 :
		       0;
	}
	
	private static class Translations
	{
		private static String 
			
			replaceVanillaStoneGeneration = ShortTrans.unformatted("cfg.world.replace.vanilla"),
			stoneInLayers = ShortTrans.unformatted("cfg.world.stone.layerToggle"),
			automaticQuartzVariants = ShortTrans.unformatted("cfg.world.ore.automaticQuartz"),
			biomeSpecificOres = ShortTrans.unformatted("cfg.world.ore.biomeSpecific"),
			largeOreClusters = ShortTrans.unformatted("cfg.world.ore.largeOreClusters"),
			largeClusterDefaultProbability = ShortTrans.unformatted("cfg.world.ore.largeClusterDefaultProbability"),
			variantsDrop = ShortTrans.unformatted("cfg.blocks.drop.variantsDrop"),
			variantsDropWithSilkTouch = ShortTrans.unformatted("cfg.blocks.drop.variantsDropSilkTouch"),
			shade = ShortTrans.unformatted("cfg.blocks.misc.overlaysShaded"),
			blendedTextures = ShortTrans.unformatted("cfg.blocks.misc.blendedTextures"),
			noTranslucent = ShortTrans.unformatted("cfg.blocks.misc.transparency"),
			overlaysFromRP = ShortTrans.unformatted("cfg.blocks.misc.resourcePackOverlays"),
			enableAdvancements = ShortTrans.unformatted("cfg.blocks.misc.enableAdvancements"),
			denseVariants = ShortTrans.unformatted("cfg.dense.general.enable"),
			denseVariantFrequency = ShortTrans.unformatted("cfg.dense.general.frequency"),
			bgBlockImitation = ShortTrans.unformatted("cfg.blocks.misc.backgroundBlockImitation"),
			dirtSize = ShortTrans.unformatted("cfg.world.stone.dirtSize"),
			gravelSize = ShortTrans.unformatted("cfg.world.stone.gravelSize"),
			andesiteSize = ShortTrans.unformatted("cfg.world.stone.andesiteSize"),
			dioriteSize = ShortTrans.unformatted("cfg.world.stone.dioriteSize"),
			graniteSize = ShortTrans.unformatted("cfg.world.stone.graniteSize"),
			stoneCount = ShortTrans.unformatted("cfg.world.stone.stoneCount"),
			andesiteLayer = ShortTrans.unformatted("cfg.world.stone.andesiteLayer"),
			dioriteLayer = ShortTrans.unformatted("cfg.world.stone.dioriteLayer"),
			graniteLayer = ShortTrans.unformatted("cfg.world.stone.graniteLayer"),
			dimensionWhitelist = ShortTrans.unformatted("cfg.world.dimensions.whitelist"),
			shadeOverrides = ShortTrans.unformatted("cfg.blocks.misc.shadeOverrides"),
			disabledOres = ShortTrans.unformatted("cfg.blocks.disable.names"),
			dynamicBlocks = ShortTrans.unformatted("cfg.dynamicBlocks.adder.add"),
			requestedCustomOres = ShortTrans.unformatted("cfg.dynamicBlocks.customOres.generator"),
			autoDisableVanillaVariants = ShortTrans.unformatted("cfg.blocks.disable.autoVanilla");
	}
}