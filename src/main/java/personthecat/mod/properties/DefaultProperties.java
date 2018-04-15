package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraftforge.common.BiomeDictionary.Type;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.JsonReader;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.NameReader;

public class DefaultProperties
{
	public static final PropertyGroup VANILLA = new PropertyGroup("minecraft"); 
	public static final PropertyGroup ICEANDFIRE = new PropertyGroup("iceandfire");
	public static final PropertyGroup SIMPLEORES = new PropertyGroup("simpleores"); 
	public static final PropertyGroup BASEMETALS = new PropertyGroup("basemetals");
	public static final PropertyGroup BIOMESOPLENTY = new PropertyGroup("biomesoplenty");
	public static final PropertyGroup GLASSHEARTS = new PropertyGroup("glasshearts");
	public static final PropertyGroup THERMALFOUNDATION = new PropertyGroup("thermalfoundation");
	public static final PropertyGroup DONT_SPAWN = new PropertyGroup("impossiblemodthatdoesntexist"); 
	
	private static final String SAME = "thisvaluedoesntactuallymatter";
	private static final String GUESS_TEXTURE = "neitherdoesthisone";
	private static final String SECOND_GUESS = "loldevyusolazy";
	public static final String IGNORE_LAZILY = "fhjzjklsfhawpet";
	private static final String BUILTIN = "alreadyhavethese";
	
	public enum DefaultOreProperties
	{
			//				languageKey, 		hardness, level, dropsBlk, 	drop, 			 				dropAlt, 		 		leastDrop, mostDrop, leastXp, mostXp,	originalTexture,	group
			COAL_ORE( 		"oreCoal", 					3.0F, 0, false, 	"coal",							"coal_ore",						1, 		1, 		0, 		2,		GUESS_TEXTURE,	VANILLA),
			DIAMOND_ORE(	"oreDiamond", 				3.0F, 2, false, 	"diamond",	 					"diamond_ore",					1, 		1, 		3, 		7,		GUESS_TEXTURE,	VANILLA),
			EMERALD_ORE(	"oreEmerald", 				3.0F, 2, false, 	"emerald",  					"emerald_ore",					1, 		1, 		3, 		7,		BUILTIN,		VANILLA),
			GOLD_ORE(		"oreGold", 					3.0F, 2, true, 		"gold_ore", 					SAME,							1, 		1, 		0, 		0,		GUESS_TEXTURE,	VANILLA),
			IRON_ORE(		"oreIron", 					3.0F, 1, true,		"iron_ore", 					SAME,							1, 		1, 		0, 		0,		GUESS_TEXTURE,	VANILLA),
			LAPIS_ORE(		"oreLapis", 				3.0F, 2, false, 	"dye", 							"lapis_ore",					4, 		8, 		2, 		5,		GUESS_TEXTURE,	VANILLA),
			REDSTONE_ORE(	"oreRedstone", 				3.0F, 2, false, 	"redstone", 					"redstone_ore",					4,	 	5, 		1, 		5,		GUESS_TEXTURE,	VANILLA),
		LIT_REDSTONE_ORE(	"oreRedstone", 				3.0F, 2, false, 	"redstone",						"redstone_ore",					4, 		5, 		1, 		5,		GUESS_TEXTURE,	VANILLA), //Still has to get created.
		    QUARTZ_ORE(		"oreQuartz",				3.0F, 1, false,		"quartz", 						"quartz_ore",					1,		1,		2,		5,		BUILTIN,		DONT_SPAWN),
 ICEANDFIRE_SAPPHIRE_ORE(	"iceandfire.sapphireOre",	3.0F, 2, false, 	"iceandfire:sapphire_gem",		"iceandfire:sapphire_ore",		1, 		1,		0,		0,		GUESS_TEXTURE,	ICEANDFIRE),
 ICEANDFIRE_SILVER_ORE(		"iceandfire.silverOre",		3.0F, 2, true, 		"iceandfire:silver_ore", 		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	ICEANDFIRE),
 SIMPLEORES_ADAMANTIUM_ORE(	"adamantium_ore",			5.0F, 2, true, 		"simpleores:adamantium_ore", 	SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	SIMPLEORES),
 SIMPLEORES_COPPER_ORE(		"copper_ore",				1.7F, 1, true, 		"simpleores:copper_ore",		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	SIMPLEORES),
 SIMPLEORES_MYTHRIL_ORE(	"mythril_ore",				4.0F, 2, true, 		"simpleores:mythril_ore",		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	SIMPLEORES),
 SIMPLEORES_TIN_ORE(		"tin_ore",					3.0F, 1, true, 		"simpleores:tin_ore",	 		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	SIMPLEORES),
 SIMPLEORES_ONYX_ORE(		"onyx_ore",					7.0F, 3, false,		"simpleores:onyx_gem",			"simpleores:onyx_ore",			1,		1,		0,		0,		IGNORE_LAZILY,	DONT_SPAWN),
 BASEMETALS_ANTIMONY_ORE( 	"basemetals.antimony_ore",	1.0F, 0, true, 		"basemetals:antimony_ore",	 	SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_BISMUTH_ORE( 	"basemetals.bismuth_ore",	1.0F, 0, true, 		"basemetals:bismuth_ore",	 	SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_COPPER_ORE( 	"basemetals.copper_ore",	4.0F, 1, true, 		"basemetals:copper_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_LEAD_ORE( 		"basemetals.lead_ore",		1.0F, 0, true, 		"basemetals:lead_ore",	 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_MERCURY_ORE( 	"basemetals.mercury_ore",	1.0F, 0, true, 		"basemetals:mercury_ore",	 	SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_NICKEL_ORE( 	"basemetals.nickel_ore",	4.0F, 1, true, 		"basemetals:nickel_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_PEWTER_ORE( 	"basemetals.pewter_ore",	1.0F, 0, true,  	"basemetals:pewter_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_PLATINUM_ORE( 	"basemetals.platinum_ore",	3.0F, 1, true,  	"basemetals:platinum_ore",		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_SILVER_ORE( 	"basemetals.silver_ore",	5.0F, 1, true,  	"basemetals:silver_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_TIN_ORE( 		"basemetals.tin_ore", 		1.0F, 1, true, 		"basemetals:tin_ore", 			SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_ZINC_ORE( 		"basemetals.zinc_ore",		1.0F, 0, true,  	"basemetals:zinc_ore", 			SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	BASEMETALS),
 BASEMETALS_ADAMANTINE_ORE( "basemetals.adamantine_ore",12.0F,4, true,  	"basemetals:adamantine_ore", 	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	DONT_SPAWN),
 BASEMETALS_COLDIRON_ORE(	"basemetals.coldiron_ore",	7.0F, 2, true,  	"basemetals:coldiron_ore",	 	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	DONT_SPAWN),
 BASEMETALS_CUPRONICKEL_ORE("basemetals.cupronickel_ore",6.0F,2, true, 		"basemetals:cupronickel_ore",	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	DONT_SPAWN),
 BASEMETALS_STARSTEEL_ORE(	"basemetals.starsteel_ore", 10.0F,3, true,  	"basemetals:starstell_ore", 	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	DONT_SPAWN),
 BIOMESOPLENTY_AMBER_ORE(	"gem_ore.amber_ore",		3.0F, 2, false, 	"biomesoplenty:gem:7",  		"biomesoplenty:gem_ore:7", 		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 BIOMESOPLENTY_MALACHITE_ORE("gem_ore.malachite_ore",	3.0F, 2, false, 	"biomesoplenty:gem:5", 			"biomesoplenty:gem_ore:5",		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 BIOMESOPLENTY_PERIDOT_ORE(	"gem_ore.peridot_ore",		3.0F, 2, false,		"biomesoplenty:gem:2",  		"biomesoplenty:gem_ore:2", 		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 BIOMESOPLENTY_RUBY_ORE(	"gem_ore.ruby_ore",			3.0F, 2, false, 	"biomesoplenty:gem:1", 			"biomesoplenty:gem_ore:1", 		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 BIOMESOPLENTY_SAPPHIRE_ORE("gem_ore.sapphire_ore",		3.0F, 2, false,		"biomesoplenty:gem:6",  		"biomesoplenty:gem_ore:6", 		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 BIOMESOPLENTY_TANZANITE_ORE("gem_ore.tanzanite_ore",	3.0F, 2, false, 	"biomesoplenty:gem:4", 			"biomesoplenty:gem_ore:4", 		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 BIOMESOPLENTY_TOPAZ_ORE(	"gem_ore.topaz_ore",		3.0F, 2, false,		"biomesoplenty:gem:3",  		"biomesoplenty:gem_ore:3", 		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 BIOMESOPLENTY_AMETHYST_ORE("gem_ore.amethyst_ore",		3.0F, 3, false,		"biomesoplenty:gem",  			"biomesoplenty:gem_ore", 		1, 		1, 		3, 		7,		BUILTIN,		BIOMESOPLENTY),
 GLASSHEARTS_AGATE_ORE(		"glasshearts.ore.agate",	3.0F, 2, false,		"glasshearts:gem:7",  			"glasshearts:ore:7", 			1, 		1, 		0, 		0,		BUILTIN,		GLASSHEARTS),
 GLASSHEARTS_AMETHYST_ORE(	"glasshearts.ore.amethyst",	3.0F, 2, false,		"glasshearts:gem",  			"glasshearts:ore", 				1, 		1, 		0, 		0,		BUILTIN,		GLASSHEARTS),
 GLASSHEARTS_ONYX_ORE(		"glasshearts.ore.onyx",		3.0F, 2, false, 	"glasshearts:gem:5",  			"glasshearts:ore:5", 			1, 		1, 		0, 		0,		BUILTIN,		GLASSHEARTS),
 GLASSHEARTS_OPAL_ORE(		"glasshearts.ore.opal",		3.0F, 2, false, 	"glasshearts:gem:4", 			"glasshearts:ore:4", 			1, 		1, 		0, 		0,		BUILTIN,		GLASSHEARTS),
 GLASSHEARTS_RUBY_ORE(		"glasshearts.ore.ruby",		3.0F, 2, false,		"glasshearts:gem:1",  			"glasshearts:ore:1", 			1, 		1, 		0, 		0,		BUILTIN,		GLASSHEARTS),
 GLASSHEARTS_SAPPHIRE_ORE(	"glasshearts.ore.sapphire",	3.0F, 2, false, 	"glasshearts:gem:3",  			"glasshearts:ore:3", 			1, 		1, 		0, 		0,		BUILTIN,		GLASSHEARTS),
 GLASSHEARTS_TOPAZ_ORE(		"glasshearts.ore.topaz",	3.0F, 2, false, 	"glasshearts:gem:2",  			"glasshearts:ore:2", 			1, 		1, 		0, 		0,		BUILTIN,		GLASSHEARTS),
 THERMALFOUNDATION_ALUMINUM_ORE("thermalfoundation.ore.aluminum",3.0F,1,true,"thermalfoundation:ore:4",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	DONT_SPAWN),
 THERMALFOUNDATION_COPPER_ORE("thermalfoundation.ore.copper",3.0F,1,true,	"thermalfoundation:ore",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	THERMALFOUNDATION),
 THERMALFOUNDATION_IRIDIUM_ORE("thermalfoundation.ore.iridium",3.0F,1,true,	"thermalfoundation:ore:7",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	DONT_SPAWN),
 THERMALFOUNDATION_LEAD_ORE("thermalfoundation.ore.lead",3.0F,1,true,		"thermalfoundation:ore:3",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	THERMALFOUNDATION),
 THERMALFOUNDATION_MITHRIL_ORE("thermalfoundation.ore.mithril",3.0F,1,true,	"thermalfoundation:ore:8",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	DONT_SPAWN),
 THERMALFOUNDATION_NICKEL_ORE("thermalfoundation.ore.nickel",3.0F,1,true,	"thermalfoundation:ore:5",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	THERMALFOUNDATION),
 THERMALFOUNDATION_PLATINUM_ORE("thermalfoundation.ore.platinum",3.0F,1,true,"thermalfoundation:ore:6",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	DONT_SPAWN),
 THERMALFOUNDATION_SILVER_ORE("thermalfoundation.ore.silver",3.0F,1,true,	"thermalfoundation:ore:2",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	THERMALFOUNDATION),
 THERMALFOUNDATION_TIN_ORE(	"thermalfoundation.ore.tin",3.0F,1,true,		"thermalfoundation:ore:1",		SAME,							1,		1,		0,		0,		SECOND_GUESS,	THERMALFOUNDATION);
		
		private DefaultOreProperties(String languageKey, float hardness, int level, boolean isDropBlock, String drop, String dropAlt, int leastDrop, int mostDrop, int leastXp, int mostXp, String originalTexture, PropertyGroup group)
		{
			dropAlt = dropAlt.equals(SAME) ? drop : dropAlt;
			originalTexture = originalTexture.equals(GUESS_TEXTURE) ? guessTexture() : originalTexture.equals(SECOND_GUESS) ? guessThermalTextures() : originalTexture;
			boolean canCreateVariants = true;
			float lightLevel = 0F;
			
			OreProperties newProperties = new OreProperties(toString().toLowerCase(), languageKey, hardness, level, new DropProperties(isDropBlock, drop, dropAlt, new int[] {leastDrop, mostDrop}, new int[] {leastXp, mostXp}));
			newProperties.setLightLevel(lightLevel);
			
			if (!originalTexture.equals(BUILTIN)) newProperties.setOriginalTexture(originalTexture);
			
			if (canCreateVariants) group.addProperties(newProperties);
		}
		
		private String guessTexture()
		{
			String domain = NameReader.getMod(toString()).replaceAll("vanilla", "minecraft");
			String blockName = NameReader.getOreWithoutMod(toString());
			
			return "assets/" + domain + "/textures/blocks/" + blockName + ".png";
		}
		
		private String guessThermalTextures()
		{
			String[] blockNameSplit = NameReader.getOreWithoutMod(toString()).split("_");
			
			return "assets/thermalfoundation/textures/blocks/ore/" + blockNameSplit[1] + "_" + blockNameSplit[0] + ".png";
		}
		
		static
		{			
			OreProperties.propertiesOf("lit_redstone_ore").setLightLevel(0.625F);
			OreProperties.propertiesOf("thermalfoundation_iridium_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_platinum_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_silver_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_mithril_ore").setLightLevel(8.0F);
			
			VANILLA.setConditions(ConfigFile.vanillaSupport);														VANILLA.register();
			ICEANDFIRE.setConditions((Main.isIceAndFireLoaded() && ConfigFile.iceAndFireSupport));					ICEANDFIRE.register();
			SIMPLEORES.setConditions((Main.isSimpleOresLoaded() && ConfigFile.simpleOresSupport));					SIMPLEORES.register();
			BASEMETALS.setConditions((Main.isBaseMetalsLoaded() && ConfigFile.baseMetalsSupport));					BASEMETALS.register();
			BIOMESOPLENTY.setConditions((Main.isBiomesOPlentyLoaded() && ConfigFile.biomesOPlentySupport));			BIOMESOPLENTY.register();
			GLASSHEARTS.setConditions((Main.isGlassHeartsLoaded() && ConfigFile.glassHeartsSupport));				GLASSHEARTS.register();
			THERMALFOUNDATION.setConditions(Main.isThermalFoundationLoaded() && ConfigFile.thermalFoundationSupport);THERMALFOUNDATION.register();
			
			JsonReader.loadNewProperties(); //Strange placement--this has to happen at this time, though.
		}
	}
	
	private static final Type[] NO_TYPE = new Type[] {};
	private static final String[] NO_NAMES = new String[] {};
	
	public enum DefaultWorldGenProperties
	{
			//						Count, chance,	minY, 	maxY,	biomeMatcher
			COAL_ORE(		 		20, 	17, 	0, 		128,	NO_TYPE, 								NO_NAMES),
			DIAMOND_ORE(	 		8, 		1, 		0, 		16,		NO_TYPE, 								NO_NAMES),
			EMERALD_ORE(			3, 		2, 		0, 		32,		new Type[] {Type.MOUNTAIN}, 			NO_NAMES),
			GOLD_ORE(				9, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
			IRON_ORE(				9, 		20, 	0, 		64,		NO_TYPE, 								NO_NAMES),
			LAPIS_ORE(				7, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
			REDSTONE_ORE(			8, 		8, 		0, 		32,		NO_TYPE, 								NO_NAMES),
		LIT_REDSTONE_ORE(			0, 		0, 		0, 		0,		NO_TYPE, 								NO_NAMES),
			QUARTZ_ORE(				9,		20,		0,		128,	NO_TYPE,								NO_NAMES),
 ICEANDFIRE_SAPPHIRE_ORE(			3,		2,		4,		32,		NO_TYPE, 								new String[] {"iceandfire:glacier"}),
 ICEANDFIRE_SILVER_ORE(				9,		2,		4,		32,		NO_TYPE, 								NO_NAMES),
 SIMPLEORES_ADAMANTIUM_ORE(			6,		6,		1,		30,		NO_TYPE,								NO_NAMES),
 SIMPLEORES_COPPER_ORE(				10,		35,		1,		90,		NO_TYPE, 								NO_NAMES),
 SIMPLEORES_MYTHRIL_ORE(			8,		10,		1,		40,		NO_TYPE, 								NO_NAMES),
 SIMPLEORES_TIN_ORE(				10,		30,		1,		90,		NO_TYPE, 								NO_NAMES),
 SIMPLEORES_ONYX_ORE(				6,		6,		1,		127,	NO_TYPE,								NO_NAMES),
 BASEMETALS_ANTIMONY_ORE( 			0,		0,		0,		0,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_BISMUTH_ORE( 			0,		0,		0,		0,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_COPPER_ORE( 			8,		10,		0,		96,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_LEAD_ORE( 				8,		5,		0,		64,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_MERCURY_ORE( 			8,		3,		0,		32,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_NICKEL_ORE( 			8,		1,		32,		96,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_PEWTER_ORE( 			0,		0,		0,		0,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_PLATINUM_ORE( 			8,		1,		1,		32,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_SILVER_ORE( 			8,		4,		0,		32,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_TIN_ORE( 				8,		10,		0,		128,	NO_TYPE, 								NO_NAMES),
 BASEMETALS_ZINC_ORE( 				8,		5,		0,		96,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_ADAMANTINE_ORE(			8,		2,		0,		128,	NO_TYPE, 								NO_NAMES),
 BASEMETALS_COLDIRON_ORE(			8,		5,		0,		128,	NO_TYPE, 								NO_NAMES),
 BASEMETALS_CUPRONICKEL_ORE(		0,		0,		0,		0,		NO_TYPE, 								NO_NAMES),
 BASEMETALS_STARSTEEL_ORE(			8,		5,		0,		255,	NO_TYPE, 								NO_NAMES),
 BIOMESOPLENTY_AMBER_ORE(			4,		1,		4,		32,		new Type[] {Type.FOREST}, 				NO_NAMES),
 BIOMESOPLENTY_MALACHITE_ORE(		4,		1,		4,		32,		new Type[] {Type.SWAMP}, 				NO_NAMES),
 BIOMESOPLENTY_PERIDOT_ORE(			4,		1,		4,		32,		new Type[] {Type.PLAINS},				NO_NAMES),
 BIOMESOPLENTY_RUBY_ORE(			4,		1,		4,		32,		new Type[] {Type.DRY, Type.SANDY}, 		NO_NAMES),
 BIOMESOPLENTY_SAPPHIRE_ORE(		4,		1,		4,		32,		new Type[] {Type.OCEAN, Type.WATER},	NO_NAMES),
 BIOMESOPLENTY_TANZANITE_ORE(		4,		1,		4,		32,		new Type[] {Type.SNOWY}, 				NO_NAMES),
 BIOMESOPLENTY_TOPAZ_ORE(			4,		1,		4,		32,		new Type[] {Type.JUNGLE, Type.BEACH},	NO_NAMES),
 BIOMESOPLENTY_AMETHYST_ORE(		4,		1,		4,		32,		NO_TYPE, 								NO_NAMES),
 GLASSHEARTS_AGATE_ORE(				3, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
 GLASSHEARTS_AMETHYST_ORE(			3, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
 GLASSHEARTS_ONYX_ORE(				3, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
 GLASSHEARTS_OPAL_ORE( 				3, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
 GLASSHEARTS_RUBY_ORE(				3, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
 GLASSHEARTS_SAPPHIRE_ORE( 			3, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
 GLASSHEARTS_TOPAZ_ORE(				3, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
 THERMALFOUNDATION_COPPER_ORE(		8,		8, 		40,		75,		new Type[] {Type.OCEAN},				NO_NAMES),
 THERMALFOUNDATION_COPPER_OCEAN(	8,		4,		20,		55,		new Type[] {Type.OCEAN},				new String[] {}),
 THERMALFOUNDATION_COPPER_HIGH(		8,		8,		48,		96,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_LEAD_ORE(		8,		1,		5,		30,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_NICKEL_ORE(		4,		2,		5,		20,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_SILVER_ORE(		8,		3,		5,		30,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_TIN_ORE(			8,		7,		20,		55,		NO_TYPE,								NO_NAMES);
		
		private DefaultWorldGenProperties(int blockCount, int chance, int minHeight, int maxHeight, Type[] biomeType, String[] biomeLookup)
		{
			JsonObject obj = JsonReader.getProperties(toString().toLowerCase(), "WorldGenProperties.json");
			WorldGenProperties genProp = null;
			
			if (obj != null)
			{
				blockCount = obj.get("blockCount") != null ? obj.get("blockCount").getAsInt() : blockCount;
				chance = obj.get("chance") != null ? obj.get("chance").getAsInt() : chance;
				minHeight = obj.get("minHeight") != null ? obj.get("minHeight").getAsInt() : minHeight;
				maxHeight = obj.get("maxHeight") != null ? obj.get("maxHeight").getAsInt() : maxHeight;
				
				List<String> biomeNameList = new ArrayList<String>();
				List<Type> biomeTypeList = new ArrayList<Type>();
				
				JsonArray biomeNames = obj.get("biomeNameList").getAsJsonArray();
				for (JsonElement element : biomeNames)
				{
					biomeNameList.add(element.getAsString());
				}
				
				JsonArray biomeTypes = obj.get("biomeTypeList").getAsJsonArray();
				for (JsonElement element: biomeTypes)
				{
					Type type = Type.getType(element.getAsString());
					biomeTypeList.add(type);
				}
				
				genProp = new WorldGenProperties(toString().toLowerCase(), blockCount, chance, minHeight, maxHeight, biomeTypeList, biomeNameList);
			}
			
			genProp = new WorldGenProperties(toString().toLowerCase(), blockCount, chance, minHeight, maxHeight, biomeType, biomeLookup);
			
			genProp.register();
		}
		
		static
		{
			WorldGenProperties thermalfoundation_copper = WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_ore");
			thermalfoundation_copper.setUseBiomeBlacklist();
			thermalfoundation_copper.setAdditionalProperties(	WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_ocean"), 
																WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_high"));
		}
	}
	
	public enum DefaultRecipeProperties
	{
		//						result,							meta, quantity,	xp
		COAL_ORE(		 		"coal", 						0, 		1, 		0.1F),
		DIAMOND_ORE(	 		"diamond", 						0, 		1, 		1.0F),
		EMERALD_ORE(			"emerald", 						0, 		1, 		1.0F),
		GOLD_ORE(				"gold_ingot", 					0, 		1, 		1.0F),
		IRON_ORE(				"iron_ingot", 					0, 		1, 		0.7F),
		LAPIS_ORE(				"dye", 							4, 		1, 		0.2F),
		REDSTONE_ORE(			"redstone",						0, 		1, 		0.7F),
	LIT_REDSTONE_ORE(			"redstone", 					0, 		1, 		0.7F),
		QUARTZ_ORE(				"quartz",						0,		1,		0.2F),
ICEANDFIRE_SAPPHIRE_ORE(		"iceandfire:sapphire_gem",		0,		1,		1.0F),
ICEANDFIRE_SILVER_ORE(			"iceandfire:silver_ingot",		0,		1,		1.0F),
SIMPLEORES_ADAMANTIUM_ORE(		"simpleores:adamantium_ingot",	0,		1,		0.7F),
SIMPLEORES_COPPER_ORE(			"simpleores:copper_ingot",		0,		1,		0.4F),
SIMPLEORES_MYTHRIL_ORE(			"simpleores:mythril_ingot",		0,		1,		0.7F),
SIMPLEORES_TIN_ORE(				"simpleores:tin_ingot",			0,		1,		0.4F),
SIMPLEORES_ONYX_ORE(			"simpleores:onyx_gem",			0,		1,		0.7F),
BASEMETALS_ANTIMONY_ORE( 		"basemetals:antimony_ingot",	0,		1,		0.1F),
BASEMETALS_BISMUTH_ORE( 		"basemetals:bismuth_ingot",		0,		1,		0.1F),
BASEMETALS_COPPER_ORE( 			"basemetals:copper_ingot",		0,		1,		0.5F),
BASEMETALS_LEAD_ORE( 			"basemetals:lead_ingot",		0,		1,		0.1F),
BASEMETALS_MERCURY_ORE( 		"basemetals:mercury_ingot",		0,		1,		0.1F),
BASEMETALS_NICKEL_ORE( 			"basemetals:nickel_ingot",		0,		1,		0.1F),
BASEMETALS_PEWTER_ORE( 			"basemetals:pewter_ingot",		0,		1,		0.1F),
BASEMETALS_PLATINUM_ORE( 		"basemetals:platinum_ingot",	0,		1,		0.1F),
BASEMETALS_SILVER_ORE( 			"basemetals:silver_ingot",		0,		1,		0.1F),
BASEMETALS_TIN_ORE( 			"basemetals:tin_ingot",			0,		1,		0.1F),
BASEMETALS_ZINC_ORE( 			"basemetals:zinc_ingot",		0,		1,		0.1F),
BASEMETALS_ADAMANTINE_ORE(		"basemetals:adamantine_ingot",	0,		1,		0.0F),
BASEMETALS_COLDIRON_ORE(		"basemetals:coldiron_ingot",	0,		1,		0.7F),
BASEMETALS_CUPRONICKEL_ORE(		"basemetals:cupronickel_ingot",	0,		1,		0.6F),
BIOMESOPLENTY_AMBER_ORE(		"biomesoplenty:gem",			7,		1,		1.2F),
BIOMESOPLENTY_MALACHITE_ORE(	"biomesoplenty:gem",			5,		1,		1.2F),
BIOMESOPLENTY_PERIDOT_ORE(		"biomesoplenty:gem",			2,		1,		1.2F),
BIOMESOPLENTY_RUBY_ORE(			"biomesoplenty:gem",			1,		1,		1.2F),
BIOMESOPLENTY_SAPPHIRE_ORE(		"biomesoplenty:gem",			6,		1,		1.2F),
BIOMESOPLENTY_TANZANITE_ORE(	"biomesoplenty:gem",			4,		1,		1.2F),
BIOMESOPLENTY_TOPAZ_ORE(		"biomesoplenty:gem",			3,		1,		1.2F),
BIOMESOPLENTY_AMETHYST_ORE(		"biomesoplenty:gem",			0,		1,		1.2F),
GLASSHEARTS_AGATE_ORE(			"glasshearts:gem", 				7, 		1, 		1.2F),
GLASSHEARTS_AMETHYST_ORE(		"glasshearts:gem", 				0, 		1, 		1.2F),
GLASSHEARTS_ONYX_ORE(			"glasshearts:gem", 				5, 		1, 		1.2F),
GLASSHEARTS_OPAL_ORE( 			"glasshearts:gem", 				4, 		1, 		1.2F),
GLASSHEARTS_RUBY_ORE(			"glasshearts:gem", 				1, 		1, 		1.2F),
GLASSHEARTS_SAPPHIRE_ORE( 		"glasshearts:gem", 				3, 		1, 		1.2F),
GLASSHEARTS_TOPAZ_ORE(			"glasshearts:gem", 				2, 		1, 		1.2F),

//These don't actually work. It's probably the meta values. Not sure how to fix that, because they are correct. 
THERMALFOUNDATION_ALUMINUM_ORE(	"thermalfoundation:material",	132,	1,		0F),
THERMALFOUNDATION_COPPER_ORE(	"thermalfoundation:material",	128,	1,		0F),
THERMALFOUNDATION_IRIDIUM_ORE(	"thermalfoundation:material",	135,	1,		0F),
THERMALFOUNDATION_LEAD_ORE(		"thermalfoundation:material",	131,	1,		0F),
THERMALFOUNDATION_MITHRIL_ORE(	"thermalfoundation:material",	136,	1,		0F),
THERMALFOUNDATION_NICKEL_ORE(	"thermalfoundation:material",	133,	1,		0F),
THERMALFOUNDATION_PLATINUM_ORE(	"thermalfoundation:material",	134,	1,		0F),
THERMALFOUNDATION_SILVER_ORE(	"thermalfoundation:material",	130,	1,		0F),
THERMALFOUNDATION_TIN_ORE(		"thermalfoundation:material",	129,	1,		0F);
		
		DefaultRecipeProperties(String result, int resultMeta, int quantity, float xp)
		{
			JsonObject obj = JsonReader.getProperties(toString().toLowerCase(), "RecipeProperties.json");
			
			if (obj != null)
			{
				result = obj.get("result") != null ? obj.get("result").getAsString() : result;
				resultMeta = obj.get("resultMeta") != null ? obj.get("resultMeta").getAsInt() : resultMeta;
				quantity = obj.get("quantity") != null ? obj.get("quantiy").getAsInt() : quantity;
				xp = obj.get("xp") != null ? obj.get("xp").getAsFloat() : xp;
			}
			
			new RecipeProperties(toString().toLowerCase(), result, resultMeta, quantity, xp);
		}
	}
}
