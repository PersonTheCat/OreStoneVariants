package personthecat.mod.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import personthecat.mod.util.Reference;

//I hope to completely reformat this class, next time I get the chance.
public class ConfigFile
{
	protected static Configuration config = null;
	
	protected static final String WORLD = I18n.translateToLocal("cfg.world") + ".";
	protected static final String BLOCKS = I18n.translateToLocal("cfg.blocks") + ".";
	protected static final String DYN_BLOCKS = I18n.translateToLocal("cfg.dynamicBlocks") + ".";
	protected static final String MOD_SUPPORT = I18n.translateToLocal("cfg.modSupport") + ".";
	protected static final String DENSE_ORES = I18n.translateToLocal("cfg.dense") + ".";
	
	protected static final String GENERATION_DIMENSIONS = WORLD + I18n.translateToLocal("cfg.world.dimensions");
	protected static final String REPLACE_GENERATION = WORLD + I18n.translateToLocal("cfg.world.replace");
	protected static final String STONE_GENERATION = WORLD + I18n.translateToLocal("cfg.world.stone");
	protected static final String ORE_GENERATION = WORLD + I18n.translateToLocal("cfg.world.ore");
	protected static final String VARIANTS_DROP = BLOCKS + I18n.translateToLocal("cfg.blocks.drop");
	protected static final String MISCELLANEOUS = BLOCKS + I18n.translateToLocal("cfg.blocks.misc");
	protected static final String DISABLE_ORES	= BLOCKS + I18n.translateToLocal("cfg.blocks.disable");
	protected static final String GENERAL_DENSE = DENSE_ORES + I18n.translateToLocal("cfg.dense.general");
	protected static final String ADD_BLOCKS = DYN_BLOCKS + I18n.translateToLocal("cfg.dynamicBlocks.adder");
	protected static final String ENABLE_MODS = MOD_SUPPORT + I18n.translateToLocal("cfg.modSupport.enableMods");
	
	public static boolean replaceVanillaStoneGeneration, stoneInLayers,
	variantsDrop, biomeSpecificOres, variantsDropWithSilkTouch, shade, blendedTextures, enableAdvancements,
	noTranslucent, denseVariants, vanillaSupport, quarkSupport, iceAndFireSupport, simpleOresSupport, 
	baseMetalsSupport, biomesOPlentySupport, glassHeartsSupport, thermalFoundationSupport;
	
	public static int dirtSize, gravelSize, andesiteSize, dioriteSize, graniteSize, 
	dirtSizeActual, gravelSizeActual, andesiteSizeActual, dioriteSizeActual, graniteSizeActual,
	stoneCount, andesiteLayer, dioriteLayer, graniteLayer;
	
	public static int[] dimensionWhitelist;
	
	public static String[] shadeOverrides, disabledOres, dynamicBlocks;
	
	public static void preInit()
	{	
		File configFile = new File(Loader.instance().getConfigDir(), Reference.MODID + ".cfg");
		config = new Configuration(configFile);	
		ConfigInterpreter.fixOldConfigEntries();
		syncFromFiles();
	}

	public static Configuration getConfig()
	{
		return config;
	}
	
	public static void clientPreInit()
	{
		MinecraftForge.EVENT_BUS.register(new ConfigEventHandler());
	}
	
	public static void syncFromFiles()
	{
		syncConfig(true, true);
	}

	public static void syncFromFields()
	{
		syncConfig(false, false);
	}
	
	private static void syncConfig(boolean loadFromConfigFile, boolean readFieldsFromConfig)
	{
		if(loadFromConfigFile) config.load();
		
		Property propertyDimensionGeneration = config.get(GENERATION_DIMENSIONS, I18n.translateToLocal("cfg.world.dimensions.whitelist"), new int[] {-1, 0, 1});
		propertyDimensionGeneration.setComment("Mainly for performance purposes. You may try removing -1 and 1 if you don't have any blocks spawning in the End or Nether. Or, you may need to add to this array if you want ores spawning in modded dimensions.");
		
		Property propertyReplaceVanillaStoneGeneration = config.get(REPLACE_GENERATION, I18n.translateToLocal("cfg.world.replace.vanilla"), true);
		propertyReplaceVanillaStoneGeneration.setComment("For better compatibility with some terrain gen mods. Set this to false if another terrain mod also spawns patches of gravel, andesite, etc.\n"
				+ "Future builds will provide options to attempt to disable other mods' ore spawning. For now, please disable ore spawning for iceandfire and simpleores manually in their config files for best results.");
		
		Property propertyDirtSize = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.dirtSize"), 0);
		propertyDirtSize.setComment("-2 = off; -1 = half size; 0 = vanilla size; 1 = 1.33 x vanilla; 2 = 1.58 x vanilla\n"
				+ "(2 may cause a small amount of cascading gen lag).\n");
		propertyDirtSize.setMinValue(-2);
		propertyDirtSize.setMaxValue(2);
		Property propertyGravelSize = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.gravelSize"), 0);
		propertyGravelSize.setMinValue(-2);
		propertyGravelSize.setMaxValue(2);
		Property propertyAndesiteSize = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.andesiteSize"), 0);
		propertyAndesiteSize.setMinValue(-2);
		propertyAndesiteSize.setMaxValue(2);
		Property propertyDioriteSize = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.dioriteSize"), 0);
		propertyDioriteSize.setMinValue(-2);
		propertyDioriteSize.setMaxValue(2);
		Property propertyGraniteSize = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.graniteSize"), 0);
		propertyGraniteSize.setMinValue(-2);
		propertyGraniteSize.setMaxValue(-2);
		Property propertyStoneCount = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.stoneCount"), 0);
		propertyStoneCount.setComment("-1 = half count; 0 = vanilla count; 1 = 2 x vanilla; 2 = 4 x vanilla.\n");
		propertyStoneCount.setMinValue(-1);
		propertyStoneCount.setMaxValue(2);
		Property propertyStoneInLayers = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.layerToggle"), false);
		propertyStoneInLayers.setComment("Generates stone variants in layers. 1 = y(0 - 20); 2 = y(25 - 45); 3 = y(40 - 80).\n");
		Property propertyAndesiteLayer = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.andesiteLayer"), 2);
		propertyAndesiteLayer.setMinValue(1);
		propertyAndesiteLayer.setMaxValue(3);
		Property propertyDioriteLayer = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.dioriteLayer"), 3);
		propertyDioriteLayer.setMinValue(1);
		propertyDioriteLayer.setMaxValue(3);
		Property propertyGraniteLayer = config.get(STONE_GENERATION, I18n.translateToLocal("cfg.world.stone.graniteLayer"), 1);
		propertyGraniteLayer.setMinValue(1);
		propertyGraniteLayer.setMaxValue(3);
		
		Property propertyOreGen = config.get(ORE_GENERATION, I18n.translateToLocal("cfg.world.ore.settings"), "");
		propertyOreGen.setComment("More coming soon...");
		Property propertyBiomeSpecific = config.get(ORE_GENERATION, I18n.translateToLocal("cfg.world.ore.biomeSpecific"), true);
		
		Property propertyVariantsDrop = config.get(VARIANTS_DROP, I18n.translateToLocal("cfg.blocks.drop.variantsDrop"), false);
		propertyVariantsDrop.setComment("These settings are server-wide.\n");
		Property propertyVariantsDropWithSilkTouch = config.get(VARIANTS_DROP, I18n.translateToLocal("cfg.blocks.drop.variantsDropSilkTouch"), true);
		
		
		Property propertyShade = config.get(MISCELLANEOUS, I18n.translateToLocal("cfg.blocks.misc.overlaysShaded"), false);
		propertyShade.setComment("These settings can be changed per-client.\n\n" + "Set this to true if you're using a resource pack or overlay textures with transparency for a better appearance.");
		Property propertyShadeOverrides = config.get(MISCELLANEOUS, I18n.translateToLocal("cfg.blocks.misc.shadeOverrides"), new String[] {});
		config.setCategoryComment(MISCELLANEOUS, "Add the names of any blocks you would like to be shaded or not shaded, opposite of the global setting.\n"
				+ "For custom blocks, the name follows this model:\n\n"
				+ "			oreType_ore_backgroundBlockName or oreType_ore_backgroundBlockName_metaValue\n"
				+ "                Example 1:  coal_ore_stone or diamond_ore_sand_1\n"
				+ "                Example 2:  basemetals_copper_ore_quark_limestone\n\n"
				+ "You do have to put the name of the mod for each ore type and for each stone type (unless vanilla). See example 2.\n\n");
		
		Property propertyBlendedTextures = config.get(MISCELLANEOUS, I18n.translateToLocal("cfg.blocks.misc.blendedTextures"), true);
		propertyBlendedTextures.setComment("To enable built-in textures with shaded backgrounds.\n"
				+ "Currently only supports Biomes O' Plenty and Glass Hearts textures, for sylistic consistency.\n"
				+ "This should not affect resource packs (only one texture needs to be replaced).");
		Property propertyNoTranslucent = config.get(MISCELLANEOUS, I18n.translateToLocal("cfg.blocks.misc.transparency"), false);
		propertyNoTranslucent.setComment("Experimental. Setting this to true will disable the overlay transparency for better compatibility with shaders. \n"
				+ "Currently does nothing.");
		
		Property propertyEnableAdvancements = config.get(MISCELLANEOUS, I18n.translateToLocal("cfg.blocks.misc.enableAdvancements"), true);
		
		Property propertyDisableOres = config.get(DISABLE_ORES, I18n.translateToLocal("cfg.blocks.disable.names"), new String[] {});
		propertyDisableOres.setComment("Enter the names of any ores you would like to not be automatically created by the mod.\n"
				+ "A full list of applicable ores can be found under \"Variant Adder.\"");
		
		config.setCategoryComment(ADD_BLOCKS, "You can add as many new ore types as you like using any background block at all, blocks from other mods\n"
											+ "included. A block model will be dynamically generated for each block and they will automatically be added\n"
											+ "to the world generation, where they will generate in the correct blocks (within height restrictions per\n"
											+ "ore type). The ores retain all properties of their original counterparts. These blocks currently obey\n"
											+ "global shade settings, but can still be overridden per-block. The easiest way to find out which name to\n"
											+ "enter is to press f3 + h in-game to see the block's full name."
											+ "\n\nThis is the basic syntax:  ore_type, domain:block_name:(with or without meta)\n"
											+ "The domain is also configured to be optional (defaults to Minecraft:) \n\n"
											+ "                Example 1:  coal_ore, minecraft:sandstone:0\n"
											+ "                Example 2:  iron_ore, red_sandstone\n"
											+ "                Example 3:  minecraft, stained_hardened_clay:6\n"
											+ "                Example 4:  coal_ore, stained_hardened_clay:*\n"
											+ "                Example 5:  simpleores, stained_hardened_clay:*\n"
											+ "You can also enter the given mod's namespace in place of \"x_ore\" and it will create all of the mod's\n"
											+ "ore types inside of that block. See example 3.\n"
											+ "If you would like to add all blockstates for any given block, substitute the block's meta with an asterisk (*).\n"
											+ "See examples 4 and 5.\n\n"
											+ "Formatting: Just place a comma between the ore type and the background block. Spaces are ignored.\n\n"
											+ "Compatible ores:\n"
											+ "vanilla: coal_ore, diamond_ore, emerald_ore, gold_ore, iron_ore, lapis_ore, redstone_ore\n"
											+ "iceandfire: iceandfire_sapphire_ore, iceandfire_silver_ore\n"
											+ "simpleores: simpleores_adamantium_ore, simpleores_copper_ore, simpleores_mythril_ore, simpleores_tin_ore\n"
											+ "basemetals: basemetals_antimony_ore, basemetals_bismuth_ore, basemetals_copper_ore, basemetals_lead_ore, "
											+ "basemetals_mercury_ore, basemetals_nickel_ore, basemetals_pewter_ore, basemetals_platinum_ore, basemetals_silver_ore, "
											+ "basemetals_tin_ore, basemetals_zinc_ore, basemetals_adamantine_ore, basemetals_coldiron_ore, basemetals_cupronickel_ore, "
											+ "basemetals_starsteel_ore\n"
											+ "biomesoplenty: biomesoplenty_amber_ore, biomesoplenty_malachite_ore, biomesoplenty_peridot_ore, biomesoplenty_ruby_ore, "
											+ "biomesoplenty_sapphire_ore, biomesoplenty_tanzanite_ore, biomesopenty_topaz_ore, biomesoplenty_amethyst_ore\n"
											+ "glasshearts: glasshearts_agate_ore, glasshearts_amethyst_ore, glasshearts_onyx_ore, glasshearts_opal_ore, "
											+ "glasshearts_ruby_ore, glasshearts_sapphire_ore, glasshearts_topaz_ore"
											+ "thermalfoundation: thermalfoundation_aluminum_ore, thermalfoundation_copper_ore, thermalfoundation_iridium_ore,\n"
											+ "thermalfoundation_lead_ore, thermalfoundation_mithril_ore, thermalfoundation_nickel_ore, thermalfoundation_platinum_ore\n"
											+ "thermalfoundation_silver_ore, thermalfoundation_tin_ore");
		Property propertyAddBlocks = config.get(ADD_BLOCKS, I18n.translateToLocal("cfg.dynamicBlocks.adder.add"), new String[] {""});
		
		Property propertyDenseVariants = config.get(GENERAL_DENSE, I18n.translateToLocal("cfg.dense.general.enable"), false);
		propertyDenseVariants.setComment("Adds a second dense variant of every ore. Drops 2 ores instead of 1.");
		
		Property propertyVanillaSupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.vanilla"), true);
		propertyVanillaSupport.setComment("Set any of these to false to disable creation and spawning of new ore variants, relative to each mod.");
		Property propertyQuarkSupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.quark"), true);
		Property propertyIceAndFireSupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.iceandfire"), true);
		Property propertySimpleOresSupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.simpleores"), true);
		Property propertyBiomesOPlentySupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.biomesoplenty"), true);
		Property propertyGlassHeartsSupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.glasshearts"), true);
		Property propertyThermalFoundationSupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.thermalfoundation"), true);
		Property propertyBaseMetalsSupport = config.get(ENABLE_MODS, I18n.translateToLocal("cfg.modSupport.enableMods.basemetals"), true);
		propertyBaseMetalsSupport.setComment("For easiest compatibility with Base Metals, set both using_orespawn and fallback_orespawn to false\n"
				+ "in BaseMetals.cfg, and subsequently disable OreSpawn itself.\n"
				+ "This is because both mods when combined will otherwise spawn twice as many ores as necessary.\n"
				+ "Only if you prefer to avoid modifying the jsons under /config/orespawn3.");
		
		List<String> propertyOrderDimensions = new ArrayList<String>();
		propertyOrderDimensions.add(propertyDimensionGeneration.getName());
		config.setCategoryPropertyOrder(GENERATION_DIMENSIONS, propertyOrderDimensions);
		
		List<String> propertyOrderReplaceGeneration = new ArrayList<String>();
		propertyOrderReplaceGeneration.add(propertyReplaceVanillaStoneGeneration.getName());
		config.setCategoryPropertyOrder(REPLACE_GENERATION, propertyOrderReplaceGeneration);
		
		List<String> propertyOrderStoneGeneration = new ArrayList<String>();
		propertyOrderStoneGeneration.add(propertyDirtSize.getName());
		propertyOrderStoneGeneration.add(propertyGravelSize.getName());
		propertyOrderStoneGeneration.add(propertyAndesiteSize.getName());
		propertyOrderStoneGeneration.add(propertyDioriteSize.getName());
		propertyOrderStoneGeneration.add(propertyGraniteSize.getName());
		propertyOrderStoneGeneration.add(propertyStoneCount.getName());
		propertyOrderStoneGeneration.add(propertyStoneInLayers.getName());
		propertyOrderStoneGeneration.add(propertyAndesiteLayer.getName());
		propertyOrderStoneGeneration.add(propertyDioriteLayer.getName());
		propertyOrderStoneGeneration.add(propertyGraniteLayer.getName());
		config.setCategoryPropertyOrder(STONE_GENERATION, propertyOrderStoneGeneration);
		
		List<String> propertyOrderOreGeneration = new ArrayList<String>();
		propertyOrderOreGeneration.add(propertyOreGen.getName());
		propertyOrderOreGeneration.add(propertyBiomeSpecific.getName());
		config.setCategoryPropertyOrder(ORE_GENERATION, propertyOrderOreGeneration);
		
		List<String> propertyOrderVariants = new ArrayList<String>();
		propertyOrderVariants.add(propertyVariantsDrop.getName());
		propertyOrderVariants.add(propertyVariantsDropWithSilkTouch.getName());
		config.setCategoryPropertyOrder(VARIANTS_DROP, propertyOrderVariants);
		
		List<String> propertyOrderMisc = new ArrayList<String>();
		propertyOrderMisc.add(propertyShade.getName());
		propertyOrderMisc.add(propertyShadeOverrides.getName());
		propertyOrderMisc.add(propertyBlendedTextures.getName());
		propertyOrderMisc.add(propertyNoTranslucent.getName());
		propertyOrderMisc.add(propertyEnableAdvancements.getName());
		config.setCategoryPropertyOrder(MISCELLANEOUS, propertyOrderMisc);
		
		List<String> propertyOrderDisableOres = new ArrayList<String>();
		propertyOrderDisableOres.add(propertyDisableOres.getName());
		config.setCategoryPropertyOrder(DISABLE_ORES, propertyOrderDisableOres);
		
		List<String> propertyOrderAddBlocks = new ArrayList<String>();
		propertyOrderAddBlocks.add(propertyAddBlocks.getName());
		config.setCategoryPropertyOrder(ADD_BLOCKS, propertyOrderAddBlocks);
		
		List<String> propertyOrderDenseVariants = new ArrayList<String>();
		propertyOrderDenseVariants.add(propertyDenseVariants.getName());
		config.setCategoryPropertyOrder(GENERAL_DENSE, propertyOrderDenseVariants);
		
		List<String> propertyOrderModSupport = new ArrayList<String>();
		propertyOrderModSupport.add(propertyVanillaSupport.getName());
		propertyOrderModSupport.add(propertyQuarkSupport.getName());
		propertyOrderModSupport.add(propertyIceAndFireSupport.getName());
		propertyOrderModSupport.add(propertySimpleOresSupport.getName());
		propertyOrderModSupport.add(propertyBiomesOPlentySupport.getName());
		propertyOrderModSupport.add(propertyGlassHeartsSupport.getName());
		propertyOrderModSupport.add(propertyThermalFoundationSupport.getName());
		propertyOrderModSupport.add(propertyBaseMetalsSupport.getName());
		config.setCategoryPropertyOrder(ENABLE_MODS, propertyOrderModSupport);
		
		if (readFieldsFromConfig)
		{		
			dimensionWhitelist = propertyDimensionGeneration.getIntList();
			replaceVanillaStoneGeneration = propertyReplaceVanillaStoneGeneration.getBoolean();
			dirtSize = propertyDirtSize.getInt();
			gravelSize = propertyGravelSize.getInt();
			andesiteSize = propertyAndesiteSize.getInt();
			dioriteSize = propertyDioriteSize.getInt();
			graniteSize = propertyGraniteSize.getInt();
			stoneCount = propertyStoneCount.getInt();
			stoneInLayers = propertyStoneInLayers.getBoolean();
			andesiteLayer = propertyAndesiteLayer.getInt();
			dioriteLayer = propertyDioriteLayer.getInt();
			graniteLayer = propertyGraniteLayer.getInt();
			biomeSpecificOres = propertyBiomeSpecific.getBoolean();
			variantsDrop = propertyVariantsDrop.getBoolean();
			variantsDropWithSilkTouch = propertyVariantsDropWithSilkTouch.getBoolean();
			shade = propertyShade.getBoolean();
			shadeOverrides = propertyShadeOverrides.getStringList();
			disabledOres = propertyDisableOres.getStringList();
			blendedTextures = propertyBlendedTextures.getBoolean();
			enableAdvancements = propertyEnableAdvancements.getBoolean();
			noTranslucent = propertyNoTranslucent.getBoolean();
			dynamicBlocks = propertyAddBlocks.getStringList();
			denseVariants = propertyDenseVariants.getBoolean();
			vanillaSupport = propertyVanillaSupport.getBoolean();
			quarkSupport = propertyQuarkSupport.getBoolean();
			iceAndFireSupport = propertyIceAndFireSupport.getBoolean();
			simpleOresSupport = propertySimpleOresSupport.getBoolean();
			baseMetalsSupport = propertyBaseMetalsSupport.getBoolean();
			biomesOPlentySupport = propertyBiomesOPlentySupport.getBoolean();
			glassHeartsSupport = propertyGlassHeartsSupport.getBoolean();
			thermalFoundationSupport = propertyThermalFoundationSupport.getBoolean();
		}
		
		dirtSize = dirtSize == -2 ? 0 : dirtSize == -1 ? 15 : dirtSize == 0 ? 33 : dirtSize == 1 ? 44 : dirtSize == 2? 52 : 0;
		gravelSize = gravelSize == -2 ? 0 : gravelSize == -1 ? 15 : gravelSize == 0 ? 33 : gravelSize == 1 ? 44 : gravelSize == 2? 52 : 0;
		andesiteSize = andesiteSize == -2 ? 0 : andesiteSize == -1 ? 15 : andesiteSize == 0 ? 33 : andesiteSize == 1 ? 44 : andesiteSize == 2? 52 : 0;
		dioriteSize = dioriteSize == -2 ? 0 : dioriteSize == -1 ? 15 : dioriteSize == 0 ? 33 : dioriteSize == 1 ? 44 : dioriteSize == 2? 52 : 0;
		graniteSize = graniteSize == -2 ? 0 : graniteSize == -1 ? 15 : graniteSize == 0 ? 33 : graniteSize == 1 ? 44 : graniteSize == 2? 52 : 0;		
		
		if(config.hasChanged()) config.save();
		
		ConfigInterpreter.loadInterpreter();
	}	
	
	//Was planning to add a GUI for 2.0. Maybe later / hopefully soon. 
	public static class ConfigEventHandler
	{
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void onEvent(ConfigChangedEvent.OnConfigChangedEvent event)
		{
			if (event.getModID().equals(Reference.MODID))
				syncFromFiles();
		}
	}	
}
