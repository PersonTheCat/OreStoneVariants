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
import personthecat.mod.properties.DefaultProperties.DefaultRecipeProperties;
import personthecat.mod.properties.DefaultProperties.DefaultWorldGenProperties;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.ShortTrans;

//Enjoy this unusually horizontal class.
public class DefaultProperties
{
	public static final PropertyGroup VANILLA = new PropertyGroup("minecraft"); 
	public static final PropertyGroup ICEANDFIRE = new PropertyGroup("iceandfire");
	public static final PropertyGroup SIMPLEORES = new PropertyGroup("simpleores"); 
	public static final PropertyGroup BASEMETALS = new PropertyGroup("basemetals");
	public static final PropertyGroup BIOMESOPLENTY = new PropertyGroup("biomesoplenty");
	public static final PropertyGroup GLASSHEARTS = new PropertyGroup("glasshearts");
	public static final PropertyGroup THERMALFOUNDATION = new PropertyGroup("thermalfoundation");
	public static final PropertyGroup IMMERSIVEENGINEERING = new PropertyGroup("immersiveengineering");
	public static final PropertyGroup THAUMCRAFT = new PropertyGroup("thaumcraft");
	public static final PropertyGroup EMBERS = new PropertyGroup("embers");
	public static final PropertyGroup MINERALOGY = new PropertyGroup("mineralogy");
	
//	public static final PropertyGroup QUARK = new PropertyGroup("quark");
	
	public static final PropertyGroup DONT_SPAWN = new PropertyGroup("unassigned_property_group"); 
	
	private static final String SAME = "thisvaluedoesntactuallymatter";
	private static final String GUESS_TEXTURE = "neitherdoesthisone";
	private static final String GUESS_REVERSE = "whydoitypethesethingsout";
	private static final String GUESS_THERMAL = "loldevyusolazy";
	private static final String GUESS_EMBERS = "lollearnhow2codesrsly";
	private static final String IGNORE_LAZILY = "fhjzjklsfhawpet";
	private static final String BUILTIN = "alreadyhavethese";
	
	public enum DefaultOreProperties
	{
			//				languageKey, 		   hardness, level, drop, 			 			dropAlt, 		 	   leastDrop, mostDrop, leastXp, mostXp,	originalTexture, blendOverlay,	group
			COAL_ORE( 		"oreCoal", 					3.0F, 0, "coal",						"coal_ore",						1, 		1, 		0, 		2,		GUESS_TEXTURE,	false, VANILLA),
			DIAMOND_ORE(	"oreDiamond", 				3.0F, 2, "diamond",	 					"diamond_ore",					1, 		1, 		3, 		7,		GUESS_TEXTURE,	false, VANILLA),
			EMERALD_ORE(	"oreEmerald", 				3.0F, 2, "emerald",  					"emerald_ore",					1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, VANILLA),
			GOLD_ORE(		"oreGold", 					3.0F, 2, "gold_ore", 					SAME,							1, 		1, 		0, 		0,		GUESS_TEXTURE,	false, VANILLA),
			IRON_ORE(		"oreIron", 					3.0F, 1, "iron_ore", 					SAME,							1, 		1, 		0, 		0,		GUESS_TEXTURE,	false, VANILLA),
			LAPIS_ORE(		"oreLapis", 				3.0F, 2, "dye:4", 						"lapis_ore",					4, 		8, 		2, 		5,		GUESS_TEXTURE,	false, VANILLA),
			REDSTONE_ORE(	"oreRedstone", 				3.0F, 2, "redstone", 					"redstone_ore",					4,	 	5, 		1, 		5,		GUESS_TEXTURE,	false, VANILLA),
		LIT_REDSTONE_ORE(	"oreRedstone", 				3.0F, 2, "redstone",					"redstone_ore",					4, 		5, 		1, 		5,		GUESS_TEXTURE,	false, VANILLA), //Still has to get created.
		    QUARTZ_ORE(		"netherquartz",				3.0F, 1, "quartz", 						"quartz_ore",					1,		1,		2,		5,		BUILTIN,		true, DONT_SPAWN),
		    
// QUARK_BIOTITE_ORE(			"quark:biotite_ore",		3.0F, 1, "quark:biotite",				"quark:biotite_ore",			1,		1,		1,		3,		GUESS_TEXTURE,	false, QUARK),
		    
 ICEANDFIRE_SAPPHIRE_ORE(	"iceandfire.sapphireOre",	3.0F, 2, "iceandfire:sapphire_gem",		"iceandfire:sapphire_ore",		1, 		1,		0,		0,		GUESS_TEXTURE,	false, ICEANDFIRE),
 ICEANDFIRE_SILVER_ORE(		"iceandfire.silverOre",		3.0F, 2, "iceandfire:silver_ore", 		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	false, ICEANDFIRE),
 
 SIMPLEORES_ADAMANTIUM_ORE(	"adamantium_ore",			5.0F, 2, "simpleores:adamantium_ore", 	SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_COPPER_ORE(		"copper_ore",				1.7F, 1, "simpleores:copper_ore",		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_MYTHRIL_ORE(	"mythril_ore",				4.0F, 2, "simpleores:mythril_ore",		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_TIN_ORE(		"tin_ore",					3.0F, 1, "simpleores:tin_ore",	 		SAME,							1, 		1,		0,		0,		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_ONYX_ORE(		"onyx_ore",					7.0F, 3, "simpleores:onyx_gem",			"simpleores:onyx_ore",			1,		1,		0,		0,		IGNORE_LAZILY,	false, DONT_SPAWN),
 
 BASEMETALS_ANTIMONY_ORE( 	"basemetals.antimony_ore",	1.0F, 0, "basemetals:antimony_ore",	 	SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_BISMUTH_ORE( 	"basemetals.bismuth_ore",	1.0F, 0, "basemetals:bismuth_ore",	 	SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_COPPER_ORE( 	"basemetals.copper_ore",	4.0F, 1, "basemetals:copper_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_LEAD_ORE( 		"basemetals.lead_ore",		1.0F, 0, "basemetals:lead_ore",	 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_MERCURY_ORE( 	"basemetals.mercury_ore",	1.0F, 0, "basemetals:mercury_ore",	 	SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_NICKEL_ORE( 	"basemetals.nickel_ore",	4.0F, 1, "basemetals:nickel_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_PEWTER_ORE( 	"basemetals.pewter_ore",	1.0F, 0, "basemetals:pewter_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_PLATINUM_ORE( 	"basemetals.platinum_ore",	3.0F, 1, "basemetals:platinum_ore",		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_SILVER_ORE( 	"basemetals.silver_ore",	5.0F, 1, "basemetals:silver_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_TIN_ORE( 		"basemetals.tin_ore", 		1.0F, 1, "basemetals:tin_ore", 			SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_ZINC_ORE( 		"basemetals.zinc_ore",		1.0F, 0, "basemetals:zinc_ore", 		SAME,							1,		1,		0,		0,		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_ADAMANTINE_ORE( "basemetals.adamantine_ore",12.0F,4, "basemetals:adamantine_ore", 	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	false, DONT_SPAWN),
 BASEMETALS_COLDIRON_ORE(	"basemetals.coldiron_ore",	7.0F, 2, "basemetals:coldiron_ore",	 	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	false, DONT_SPAWN),
 BASEMETALS_CUPRONICKEL_ORE("basemetals.cupronickel_ore",6.0F,2, "basemetals:cupronickel_ore",	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	false, DONT_SPAWN),
 BASEMETALS_STARSTEEL_ORE(	"basemetals.starsteel_ore", 10.0F,3, "basemetals:starstell_ore", 	SAME,							1,		1,		0,		0,		IGNORE_LAZILY,	false, DONT_SPAWN),
 
 BIOMESOPLENTY_AMBER_ORE(	"gem_ore.amber_ore",		3.0F, 2, "biomesoplenty:gem:7",  		"biomesoplenty:gem_ore:7", 		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_MALACHITE_ORE("gem_ore.malachite_ore",	3.0F, 2, "biomesoplenty:gem:5", 		"biomesoplenty:gem_ore:5",		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_PERIDOT_ORE(	"gem_ore.peridot_ore",		3.0F, 2, "biomesoplenty:gem:2",  		"biomesoplenty:gem_ore:2", 		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_RUBY_ORE(	"gem_ore.ruby_ore",			3.0F, 2, "biomesoplenty:gem:1", 		"biomesoplenty:gem_ore:1", 		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_SAPPHIRE_ORE("gem_ore.sapphire_ore",		3.0F, 2, "biomesoplenty:gem:6",  		"biomesoplenty:gem_ore:6", 		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_TANZANITE_ORE("gem_ore.tanzanite_ore",	3.0F, 2, "biomesoplenty:gem:4", 		"biomesoplenty:gem_ore:4", 		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_TOPAZ_ORE(	"gem_ore.topaz_ore",		3.0F, 2, "biomesoplenty:gem:3",  		"biomesoplenty:gem_ore:3", 		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_AMETHYST_ORE("gem_ore.amethyst_ore",		3.0F, 3, "biomesoplenty:gem",  			"biomesoplenty:gem_ore", 		1, 		1, 		3, 		7,		GUESS_TEXTURE,	true, BIOMESOPLENTY),
 
 GLASSHEARTS_AGATE_ORE(		"glasshearts.ore.agate",	3.0F, 2, "glasshearts:gem:7",  			"glasshearts:ore:7", 			1, 		1, 		0, 		0,		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_AMETHYST_ORE(	"glasshearts.ore.amethyst",	3.0F, 2, "glasshearts:gem",  			"glasshearts:ore", 				1, 		1, 		0, 		0,		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_ONYX_ORE(		"glasshearts.ore.onyx",		3.0F, 2, "glasshearts:gem:5",  			"glasshearts:ore:5", 			1, 		1, 		0, 		0,		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_OPAL_ORE(		"glasshearts.ore.opal",		3.0F, 2, "glasshearts:gem:4", 			"glasshearts:ore:4", 			1, 		1, 		0, 		0,		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_RUBY_ORE(		"glasshearts.ore.ruby",		3.0F, 2, "glasshearts:gem:1",  			"glasshearts:ore:1", 			1, 		1, 		0, 		0,		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_SAPPHIRE_ORE(	"glasshearts.ore.sapphire",	3.0F, 2, "glasshearts:gem:3",  			"glasshearts:ore:3", 			1, 		1, 		0, 		0,		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_TOPAZ_ORE(		"glasshearts.ore.topaz",	3.0F, 2, "glasshearts:gem:2",  			"glasshearts:ore:2", 			1, 		1, 		0, 		0,		GUESS_TEXTURE,	true, GLASSHEARTS),
 
THERMALFOUNDATION_ALUMINUM_ORE("thermalfoundation.ore.aluminum",3.0F, 1, "thermalfoundation:ore:4", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_COPPER_ORE("thermalfoundation.ore.copper",	3.0F, 1, "thermalfoundation:ore",   SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_IRIDIUM_ORE("thermalfoundation.ore.iridium",	3.0F, 1, "thermalfoundation:ore:7", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_LEAD_ORE("thermalfoundation.ore.lead",		3.0F, 1, "thermalfoundation:ore:3", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_MITHRIL_ORE("thermalfoundation.ore.mithril",	3.0F, 1, "thermalfoundation:ore:8", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_NICKEL_ORE("thermalfoundation.ore.nickel",	3.0F, 1, "thermalfoundation:ore:5", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_PLATINUM_ORE("thermalfoundation.ore.platinum",3.0F, 1, "thermalfoundation:ore:6", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_SILVER_ORE("thermalfoundation.ore.silver",	3.0F, 1, "thermalfoundation:ore:2", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_TIN_ORE(	"thermalfoundation.ore.tin",		3.0F, 1, "thermalfoundation:ore:1", SAME,						1,		1,		0,		0,		GUESS_THERMAL,	false, THERMALFOUNDATION),

IMMERSIVEENGINEERING_ALUMINUM_ORE("immersiveengineering.ore.aluminum",3.0F, 1, "immersiveengineering:ore:1", SAME,				1,		1,		0,		0,		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_COPPER_ORE("immersiveengineering.ore.copper",	  3.0F, 1, "immersiveengineering:ore:0", SAME,				1,		1,		0,		0,		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_LEAD_ORE("immersiveengineering.ore.lead",		  3.0F, 1, "immersiveengineering:ore:2", SAME,				1,		1,		0,		0,		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_NICKEL_ORE("immersiveengineering.ore.nickel",	  3.0F, 2, "immersiveengineering:ore:4", SAME,				1,		1,		0,		0,		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_SILVER_ORE("immersiveengineering.ore.silver",	  3.0F, 2, "immersiveengineering:ore:3", SAME,				1,		1,		0,		0,		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_URANIUM_ORE("immersiveengineering.ore.uranium",  3.0F, 2, "immersiveengineering:ore:5", SAME,				1,		1,		0,		0,		BUILTIN,		true, IMMERSIVEENGINEERING),

 EMBERS_ALUMINUM_ORE(		"ore_aluminum",				1.6F, 1, "embers:ore_aluminum",			SAME,							1,		1,		0,		0,		GUESS_EMBERS,	false, EMBERS),
 EMBERS_COPPER_ORE(			"ore_copper",				1.8F, 1, "embers:ore_copper",			SAME,							1,		1,		0,		0,		GUESS_EMBERS,	false, EMBERS),
 EMBERS_LEAD_ORE(			"ore_lead",					2.5F, 2, "embers:ore_lead",				SAME,							1,		1,		0,		0,		GUESS_EMBERS,	false, EMBERS),
 EMBERS_NICKEL_ORE(			"ore_nickel",				2.2F, 1, "embers:ore_nickel",			SAME,							1,		1,		0,		0,		GUESS_EMBERS,	false, EMBERS),
 EMBERS_SILVER_ORE(			"ore_silver",				2.5F, 2, "embers:ore_silver",			SAME,							1,		1,		0,		0,		GUESS_EMBERS,	false, EMBERS),
 EMBERS_TIN_ORE(			"ore_tin",					1.3F, 1, "embers:ore_tin",				SAME,							1,		1,		0,		0,		GUESS_EMBERS,	false, EMBERS),
 
 //Can't verify xp values.
 THAUMCRAFT_AMBER_ORE(		"ore_amber",				2.0F, 1, "thaumcraft:amber",			"thaumcraft:ore_amber",			1,		1,		1, 		1,		BUILTIN,		true, THAUMCRAFT),
 THAUMCRAFT_CINNABAR_ORE(	"ore_cinnabar",				2.0F, 2, "thaumcraft:ore_cinnabar",		SAME,							1,		1,		0,		0,		BUILTIN,		true, THAUMCRAFT),
 
 MINERALOGY_PHOSPHOROUS_ORE("mineralogy.phosphorous_ore",1.5F,0, "mineralogy:phosphorous_dust",	"mineralogy:phosphorous_ore",	1,		4,		0,		0,		GUESS_TEXTURE,	true, MINERALOGY),
 MINERALOGY_SULFUR_ORE(		"mineralogy.sulfur_ore",	1.5F, 0, "mineralogy:sulfur_dust",		"mineralogy:sulfer_ore",		1,		4,		0,		0,		GUESS_TEXTURE,	false, MINERALOGY);
		
		private DefaultOreProperties(String languageKey, float hardness, int level, String drop, String dropAlt, int leastDrop, int mostDrop, int leastXp, int mostXp, String originalTexture, boolean blendOverlay, PropertyGroup group)
		{
			if (originalTexture.equals(GUESS_TEXTURE)) originalTexture = guessTexture();
			if (originalTexture.equals(GUESS_THERMAL)) originalTexture = guessThermalTextures();
			if (originalTexture.equals(GUESS_EMBERS)) originalTexture = guessEmbersTextures();
			if (originalTexture.equals(GUESS_REVERSE)) originalTexture = guessReverse();

			dropAlt = dropAlt.equals(SAME) ? drop : dropAlt;
			
			OreProperties newProperties = new OreProperties(toString().toLowerCase(), languageKey, hardness, level, new DropProperties(drop, dropAlt, new int[] {leastDrop, mostDrop}, new int[] {leastXp, mostXp}));
			
			if (toString().equals("QUARTZ_ORE") && ConfigFile.automaticQuartzVariants)
			{
				newProperties.setPropertyGroup(VANILLA);
			}
			
			else newProperties.setPropertyGroup(group);
			
			if (blendOverlay) newProperties.setUseBlendedTextures();
			
			if (!originalTexture.equals(BUILTIN)) newProperties.setOriginalTexture(originalTexture);
		}
		
		private String getDomain()
		{
			return NameReader.getMod(toString()).replaceAll("vanilla", "minecraft");
		}
		
		private String getBlockName()
		{
			return NameReader.getOreWithoutMod(toString());
		}
		
		private String[] getBlockNameSplit()
		{
			return NameReader.getOreWithoutMod(toString()).split("_");
		}
		
		private String guessTexture()
		{
			return "assets/" + getDomain() + "/textures/blocks/" + getBlockName() + ".png";
		}
		
		private String guessReverse()
		{			
			return "assets/" + getDomain() + "/textures/blocks/" + getBlockNameSplit()[1] + "_" + getBlockNameSplit()[0] + ".png";	
		}
		
		private String guessThermalTextures()
		{
			return "assets/thermalfoundation/textures/blocks/ore/" + getBlockNameSplit()[1] + "_" + getBlockNameSplit()[0] + ".png";
		}
		
		private String guessEmbersTextures()
		{
			return "assets/embers/textures/blocks/" + getBlockNameSplit()[1] + "_" + getBlockNameSplit()[0] + "_vanilla" + ".png";			
		}
		
		public static String getFormattedBlockList()
		{
			String blockList = "";
			
			for (PropertyGroup group : PropertyGroup.getSortedPropertyGroups())
			{
				if (!group.getProperties().isEmpty())
				{
					String modName = group.getModName();
					
					blockList += modName + ":\n\n\t";
					
					int lineLength = 0;
					
					for (OreProperties property : group.getProperties())
					{
						String propertyName = property.getName();
						int nameLength = propertyName.length();
						
						//Previous line is too long? Add a new line + tab, reset.
						if (lineLength + nameLength > 105)
						{
							blockList += "\n\t";
							
							lineLength = 0;
						}
						
						//Not the first entry on the line? Add a comma + space.
						if (lineLength > 0)
						{
							blockList += ", ";
							
							lineLength += 2;
						}
						
						blockList += propertyName;
						
						lineLength += nameLength;
					}
					
					blockList += "\n\n";
				}
			}
			
			return blockList;
		}
		
		static
		{			
			OreProperties.propertiesOf("lit_redstone_ore").setLightLevel(0.625F);
			OreProperties.propertiesOf("thermalfoundation_iridium_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_platinum_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_silver_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_mithril_ore").setLightLevel(8.0F);
			OreProperties.propertiesOf("mineralogy_phosphorous_ore").setBackgroundMatcher("assets/mineralogy/textures/blocks/limestone.png");
//			OreProperties.propertiesOf("quark_biotite_ore").setBackgroundMatcher("assets/minecraft/textures/blocks/end_stone.png");
		}
	}
	
	public static void postConfig()
	{		
		VANILLA.setConditions(ConfigFile.isSupportEnabled("vanilla"));																	VANILLA.register();
		ICEANDFIRE.setConditions(Main.isIceAndFireLoaded() && ConfigFile.isSupportEnabled("iceandfire"));								ICEANDFIRE.register();
		SIMPLEORES.setConditions(Main.isSimpleOresLoaded() && ConfigFile.isSupportEnabled("simpleores"));								SIMPLEORES.register();
		BASEMETALS.setConditions(Main.isBaseMetalsLoaded() && ConfigFile.isSupportEnabled("basemetals"));								BASEMETALS.register();
		BIOMESOPLENTY.setConditions(Main.isBiomesOPlentyLoaded() && ConfigFile.isSupportEnabled("biomesoplenty"));						BIOMESOPLENTY.register();
		GLASSHEARTS.setConditions(Main.isGlassHeartsLoaded() && ConfigFile.isSupportEnabled("glasshearts"));							GLASSHEARTS.register();
		THERMALFOUNDATION.setConditions(Main.isThermalFoundationLoaded() && ConfigFile.isSupportEnabled("thermalfoundation"));			THERMALFOUNDATION.register();
		IMMERSIVEENGINEERING.setConditions(Main.isImmersiveEngineeringLoaded() && ConfigFile.isSupportEnabled("immersiveengineering"));	IMMERSIVEENGINEERING.register();
		THAUMCRAFT.setConditions(Main.isThaumcraftLoaded() && ConfigFile.isSupportEnabled("thaumcraft"));	 							THAUMCRAFT.register();
		EMBERS.setConditions(Main.isEmbersLoaded() && ConfigFile.isSupportEnabled("embers")); 											EMBERS.register();
		MINERALOGY.setConditions(Main.isMineralogyLoaded() && ConfigFile.isSupportEnabled("mineralogy"));								MINERALOGY.register();
//		QUARK.setConditions(false); 																									QUARK.register();
		
		DONT_SPAWN.setConditions(false); 																								DONT_SPAWN.register();
	}
	
	private static final Type[] NO_TYPE = new Type[] {};
	private static final String[] NO_NAMES = new String[] {};
	
	public enum DefaultWorldGenProperties
	{
			//						size, count,	minY, 	maxY,	biomeMatcher
			COAL_ORE(		 		20, 	17, 	0, 		128,	NO_TYPE, 								NO_NAMES),
			DIAMOND_ORE(	 		8, 		1, 		0, 		16,		NO_TYPE, 								NO_NAMES),
			EMERALD_ORE(			3, 		2, 		0, 		32,		new Type[] {Type.MOUNTAIN}, 			NO_NAMES),
			GOLD_ORE(				9, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
			IRON_ORE(				9, 		20, 	0, 		64,		NO_TYPE, 								NO_NAMES),
			LAPIS_ORE(				7, 		2, 		0, 		32,		NO_TYPE, 								NO_NAMES),
			REDSTONE_ORE(			8, 		8, 		0, 		32,		NO_TYPE, 								NO_NAMES),
		LIT_REDSTONE_ORE(			0, 		0, 		0, 		0,		NO_TYPE, 								NO_NAMES),
			QUARTZ_ORE(				9,		20,		0,		128,	NO_TYPE,								NO_NAMES),
			
 QUARK_BIOTITE_ORE(					8,		8,		0,		128,	NO_TYPE,								NO_NAMES),
			
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
 THERMALFOUNDATION_COPPER_OCEAN(	8,		4,		20,		55,		new Type[] {Type.OCEAN},				NO_NAMES),
 THERMALFOUNDATION_COPPER_HIGH(		8,		8,		48,		96,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_LEAD_ORE(		8,		1,		5,		30,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_NICKEL_ORE(		4,		2,		5,		20,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_SILVER_ORE(		8,		3,		5,		30,		NO_TYPE,								NO_NAMES),
 THERMALFOUNDATION_TIN_ORE(			8,		7,		20,		55,		NO_TYPE,								NO_NAMES),
 
IMMERSIVEENGINEERING_ALUMINUM_ORE(	4,		8,		40,		72,		NO_TYPE,								NO_NAMES),
IMMERSIVEENGINEERING_COPPER_ORE(	8,		8,		40,		72,		NO_TYPE,								NO_NAMES),
IMMERSIVEENGINEERING_LEAD_ORE(		6,		4,		8,		36,		NO_TYPE,								NO_NAMES),
IMMERSIVEENGINEERING_NICKEL_ORE(	6,		2,		8,		24,		NO_TYPE,								NO_NAMES),
IMMERSIVEENGINEERING_SILVER_ORE(	8,		3,		8,		40,		NO_TYPE,								NO_NAMES),
IMMERSIVEENGINEERING_URANIUM_ORE(	4,		1,		8,		24,		NO_TYPE,								NO_NAMES),

 EMBERS_ALUMINUM_ORE(				6,		4,		0,		58,		NO_TYPE,								NO_NAMES),
 EMBERS_COPPER_ORE(					12,		6,		0,		64,		NO_TYPE,								NO_NAMES),
 EMBERS_LEAD_ORE(					8,		4,		0,		64,		NO_TYPE,								NO_NAMES),
 EMBERS_NICKEL_ORE(					6,		4,		0,		24,		NO_TYPE,								NO_NAMES),
 EMBERS_SILVER_ORE(					6,		4,		0,		28,		NO_TYPE,								NO_NAMES),
 EMBERS_TIN_ORE(					6,		6,		0,		48,		NO_TYPE,								NO_NAMES),
 
 //Also difficult to verify.
 THAUMCRAFT_AMBER_ORE(				3,		18,		0,		128,	NO_TYPE,								NO_NAMES),
 THAUMCRAFT_CINNABAR_ORE(			3,		18,		0,		128,	NO_TYPE,								NO_NAMES),
 
 MINERALOGY_PHOSPHOROUS_ORE(		16,		1,		16,		64,		NO_TYPE,								NO_NAMES),
 MINERALOGY_SULFUR_ORE(				16,		1,		16,		64,		NO_TYPE,								NO_NAMES);
		
		private DefaultWorldGenProperties(int blockCount, int chance, int minHeight, int maxHeight, Type[] biomeType, String[] biomeLookup)
		{
			WorldGenProperties genProp = new WorldGenProperties(toString().toLowerCase(), blockCount, chance, minHeight, maxHeight, biomeType, biomeLookup);
				
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
		
QUARK_BIOTITE_ORE(				"quark:biotite",				0,		1,		1.0F),
		
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
THERMALFOUNDATION_TIN_ORE(		"thermalfoundation:material",	129,	1,		0F),

//Continue working ores.
IMMERSIVEENGINEERING_ALUMINUM_ORE("immersiveengineering:metal",	1,		1,		0.3F),
IMMERSIVEENGINEERING_COPPER_ORE(  "immersiveengineering:metal",	0,		1,		0.3F),
IMMERSIVEENGINEERING_LEAD_ORE(    "immersiveengineering:metal",	2,		1,		0.7F),
IMMERSIVEENGINEERING_NICKEL_ORE(  "immersiveengineering:metal",	4,		1,		1.0F),
IMMERSIVEENGINEERING_SILVER_ORE(  "immersiveengineering:metal",	3,		1,		1.0F),
IMMERSIVEENGINEERING_URANIUM_ORE( "immersiveengineering:metal",	5,		1,		1.0F),

EMBERS_ALUMINUM_ORE(			"embers:ingot_aluminum",		0,		1,		0.55F),
EMBERS_COPPER_ORE(				"embers:ingot_copper",			0,		1,		0.65F),
EMBERS_LEAD_ORE(				"embers:ingot_lead",			0,		1,		0.35F),
EMBERS_NICKEL_ORE(				"embers:ingot_nickel",			0,		1,		0.55F),
EMBERS_SILVER_ORE(				"embers:ingot_silver",			0,		1,		0.35F),
EMBERS_TIN_ORE(					"embers:ingot_tin",				0,		1,		0.55F),

THAUMCRAFT_AMBER_ORE(			"thaumcraft:amber",				0,		1,		1.0F),
THAUMCRAFT_CINNABAR_ORE(		"thaumcraft:cluster",			6,		1,		1.0F),

//Couldn't verify xp levels
MINERALOGY_PHOSPHOROUS_ORE(		"mineralogy:phosphorous_dust",	0, 		1,		1.0F),
MINERALOGY_SULFUR_ORE(			"mineralogy:sulfur_dust",		0,		1,		1.0F);
		
		DefaultRecipeProperties(String result, int resultMeta, int quantity, float xp)
		{
			JsonObject obj = JsonReader.getProperties(toString().toLowerCase(), "RecipeProperties.json");
			
			if (obj != null)
			{
				result = obj.get("result") != null ? obj.get("result").getAsString() : result;
				resultMeta = obj.get("resultMeta") != null ? obj.get("resultMeta").getAsInt() : resultMeta;
				quantity = obj.get("quantity") != null ? obj.get("quantity").getAsInt() : quantity;
				xp = obj.get("xp") != null ? obj.get("xp").getAsFloat() : xp;
			}
			
			new RecipeProperties(toString().toLowerCase(), result, resultMeta, quantity, xp);
		}
	}
}
