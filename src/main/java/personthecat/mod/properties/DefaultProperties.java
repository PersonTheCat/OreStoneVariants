package personthecat.mod.properties;

import com.google.gson.JsonObject;

import net.minecraftforge.common.BiomeDictionary.Type;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.JsonReader;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.NameReader;

/**
 * Default stone types are listed under 
 * personthecat.mod.util.handlers.BlockStateGenerator.
 * 
 * For anyone searching for a list of default properties, open this as a raw file on GitHub,
 * and then paste it into another program such as notepad++ for correct tab spacing.
 * 
 * Enjoy this unusually horizontal class!
 */
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
	public static final PropertyGroup MODERNMETALS = new PropertyGroup("modernmetals");
	
	public static final PropertyGroup QUARK = new PropertyGroup("quark");
	
	public static final PropertyGroup DONT_SPAWN = new PropertyGroup("unassigned_property_group"); 
	
	private static final String MOD = "insertnamespace";
	private static final String ORE = "thenameoftheore";
	private static final String ERO = "reversedorename";
	
	private static final String SAME = "thesameasthelastvalue";
	private static final String GUESS_TEXTURE = "usethenormaltexturepathformat";
	private static final String GUESS_REVERSE = "samebuttheorenameisreversed";
	private static final String GUESS_THERMAL = "thermalfoundationsformat";
	private static final String GUESS_EMBERS = "embersformat";
	private static final String BUILTIN = "alreadyhavethese";
	
	public enum DefaultOreProperties
	{
			//					languageKey, 	   hardness, level, drop, 			 	dropAlt, 			dropRange, 	xpRange,	originalTexture, blendOverlay,	group
			COAL_ORE( 			"oreCoal", 				3.0F, 0, "coal",				ORE,				rng(1), 	rng(0, 2),	GUESS_TEXTURE,	false, VANILLA),	
			DIAMOND_ORE(		"oreDiamond", 			3.0F, 2, "diamond",	 			ORE,				rng(1), 	rng(3, 7),	GUESS_TEXTURE,	false, VANILLA),
			EMERALD_ORE(		"oreEmerald", 			3.0F, 2, "emerald",  			ORE,				rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, VANILLA),
			GOLD_ORE(			"oreGold", 				3.0F, 2, ORE, 					SAME,				rng(1), 	rng(0),		GUESS_TEXTURE,	false, VANILLA),
			IRON_ORE(			"oreIron", 				3.0F, 1, ORE, 					SAME,				rng(1), 	rng(0),		GUESS_TEXTURE,	false, VANILLA),
			LAPIS_ORE(			"oreLapis", 			3.0F, 2, "dye:4", 				ORE,				rng(4, 8), 	rng(2, 5),	GUESS_TEXTURE,	false, VANILLA),
			REDSTONE_ORE(		"oreRedstone", 			3.0F, 2, "redstone", 			ORE,				rng(4, 5), 	rng(1, 5),	GUESS_TEXTURE,	false, VANILLA),
		LIT_REDSTONE_ORE(		"oreRedstone", 			3.0F, 2, "redstone",			"redstone_ore",		rng(4, 5), 	rng(1, 5),	GUESS_TEXTURE,	false, DONT_SPAWN),
		    QUARTZ_ORE(			"netherquartz",			3.0F, 1, "quartz", 				ORE,				rng(1),		rng(2, 5),	BUILTIN,		true, DONT_SPAWN),
		    
 QUARK_BIOTITE_ORE(				MOD + ":" + ORE,		3.0F, 1, MOD + ":biotite",		MOD + ":" + ORE,	rng(1),		rng(1, 3),	GUESS_TEXTURE,	true, DONT_SPAWN),
		    
 ICEANDFIRE_SAPPHIRE_ORE(		MOD + ".sapphireOre",	3.0F, 2, MOD + ":sapphire_gem",	MOD + ":" + ORE,	rng(1),		rng(0),		GUESS_TEXTURE,	false, ICEANDFIRE),
 ICEANDFIRE_SILVER_ORE(			MOD + ".silverOre",		3.0F, 2, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, ICEANDFIRE),
 
 SIMPLEORES_ADAMANTIUM_ORE(		ORE,					5.0F, 2, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_COPPER_ORE(			ORE,					1.7F, 1, MOD + ":" + ORE,		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_MYTHRIL_ORE(		ORE,					4.0F, 2, MOD + ":" + ORE,		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_TIN_ORE(			ORE,					3.0F, 1, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, SIMPLEORES),
 SIMPLEORES_ONYX_ORE(			ORE,					7.0F, 3, MOD + ":onyx_gem",		MOD + ":" + ORE,	rng(1),		rng(0),		BUILTIN,		false, DONT_SPAWN),
 
 BASEMETALS_ANTIMONY_ORE( 		MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_BISMUTH_ORE( 		MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_COPPER_ORE( 		MOD + "." + ORE,		4.0F, 1, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_LEAD_ORE( 			MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_MERCURY_ORE( 		MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_NICKEL_ORE( 		MOD + "." + ORE,		4.0F, 1, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_PEWTER_ORE( 		MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_PLATINUM_ORE( 		MOD + "." + ORE,		3.0F, 1, MOD + ":" + ORE,		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_SILVER_ORE( 		MOD + "." + ORE,		5.0F, 1, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_TIN_ORE( 			MOD + "." + ORE, 		1.0F, 1, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_ZINC_ORE( 			MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, BASEMETALS),
 BASEMETALS_ADAMANTINE_ORE( 	MOD + "." + ORE,		12.0F,4, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		BUILTIN,		false, DONT_SPAWN),
 BASEMETALS_COLDIRON_ORE(		MOD + "." + ORE,		7.0F, 2, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		BUILTIN,		false, DONT_SPAWN),
 BASEMETALS_CUPRONICKEL_ORE(	MOD + "." + ORE,		6.0F, 2, MOD + ":" + ORE,		SAME,				rng(1),		rng(0),		BUILTIN,		false, DONT_SPAWN),
 BASEMETALS_STARSTEEL_ORE(		MOD + "." + ORE, 		10.0F,3, MOD + ":" + ORE, 		SAME,				rng(1),		rng(0),		BUILTIN,		false, DONT_SPAWN),
 
 BIOMESOPLENTY_AMBER_ORE(		"gem_ore." + ORE,		3.0F, 2, MOD + ":gem:7",  		MOD + ":gem_ore:7",	rng(1),		rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_MALACHITE_ORE(	"gem_ore." + ORE,		3.0F, 2, MOD + ":gem:5", 		MOD + ":gem_ore:5",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_PERIDOT_ORE(		"gem_ore." + ORE,		3.0F, 2, MOD + ":gem:2",  		MOD + ":gem_ore:2",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_RUBY_ORE(		"gem_ore." + ORE,		3.0F, 2, MOD + ":gem:1", 		MOD + ":gem_ore:1",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_SAPPHIRE_ORE(	"gem_ore." + ORE,		3.0F, 2, MOD + ":gem:6",  		MOD + ":gem_ore:6",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_TANZANITE_ORE(	"gem_ore." + ORE,		3.0F, 2, MOD + ":gem:4", 		MOD + ":gem_ore:4",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_TOPAZ_ORE(		"gem_ore." + ORE,		3.0F, 2, MOD + ":gem:3",  		MOD + ":gem_ore:3",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 BIOMESOPLENTY_AMETHYST_ORE(	"gem_ore." + ORE,		3.0F, 3, MOD + ":gem",  		MOD + ":gem_ore", 	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, BIOMESOPLENTY),
 
 GLASSHEARTS_AGATE_ORE(			MOD + ".ore.agate",		3.0F, 2, MOD + ":gem:7",  		MOD + ":ore:7", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_AMETHYST_ORE(		MOD + ".ore.amethyst",	3.0F, 2, MOD + ":gem",  		MOD + ":ore", 		rng(1), 	rng(0),		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_ONYX_ORE(			MOD + ".ore.onyx",		3.0F, 2, MOD + ":gem:5",  		MOD + ":ore:5", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_OPAL_ORE(			MOD + ".ore.opal",		3.0F, 2, MOD + ":gem:4", 		MOD + ":ore:4", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_RUBY_ORE(			MOD + ".ore.ruby",		3.0F, 2, MOD + ":gem:1",  		MOD + ":ore:1", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_SAPPHIRE_ORE(		MOD + ".ore.sapphire",	3.0F, 2, MOD + ":gem:3",  		MOD + ":ore:3", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, GLASSHEARTS),
 GLASSHEARTS_TOPAZ_ORE(			MOD + ".ore.topaz",		3.0F, 2, MOD + ":gem:2",  		MOD + ":ore:2", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, GLASSHEARTS),
 
THERMALFOUNDATION_ALUMINUM_ORE(	MOD + ".ore.aluminum",	3.0F, 1, MOD + ":ore:4", 		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_COPPER_ORE(	MOD + ".ore.copper",	3.0F, 1, MOD + ":ore",   		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_IRIDIUM_ORE(	MOD + ".ore.iridium",	3.0F, 1, MOD + ":ore:7", 		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_LEAD_ORE(		MOD + ".ore.lead",		3.0F, 1, MOD + ":ore:3", 		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_MITHRIL_ORE(	MOD + ".ore.mithril",	3.0F, 1, MOD + ":ore:8", 		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_NICKEL_ORE(	MOD + ".ore.nickel",	3.0F, 1, MOD + ":ore:5", 		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_PLATINUM_ORE(	MOD + ".ore.platinum",	3.0F, 1, MOD + ":ore:6", 		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, DONT_SPAWN),
THERMALFOUNDATION_SILVER_ORE(	MOD + ".ore.silver",	3.0F, 1, MOD + ":ore:2", 		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, THERMALFOUNDATION),
THERMALFOUNDATION_TIN_ORE(		MOD + ".ore.tin",		3.0F, 1, MOD + ":ore:1",		SAME,				rng(1),		rng(0),		GUESS_THERMAL,	false, THERMALFOUNDATION),

IMMERSIVEENGINEERING_ALUMINUM_ORE(MOD + ".ore.aluminum",3.0F, 1, MOD + ":ore:1", 		SAME,				rng(1),		rng(0),		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_COPPER_ORE(MOD + ".ore.copper",	3.0F, 1, MOD + ":ore", 			SAME,				rng(1),		rng(0),		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_LEAD_ORE(	MOD + ".ore.lead",		3.0F, 1, MOD + ":ore:2", 		SAME,				rng(1),		rng(0),		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_NICKEL_ORE(MOD + ".ore.nickel",	3.0F, 2, MOD + ":ore:4", 		SAME,				rng(1),		rng(0),		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_SILVER_ORE(MOD + ".ore.silver",	3.0F, 2, MOD + ":ore:3", 		SAME,				rng(1),		rng(0),		BUILTIN,		true, IMMERSIVEENGINEERING),
IMMERSIVEENGINEERING_URANIUM_ORE(MOD + ".ore.uranium",	3.0F, 2, MOD + ":ore:5", 		SAME,				rng(1),		rng(0),		BUILTIN,		true, IMMERSIVEENGINEERING),

 EMBERS_ALUMINUM_ORE(			ERO,					1.6F, 1, MOD + ":" + ERO,		SAME,				rng(1),		rng(0),		GUESS_EMBERS,	false, EMBERS),
 EMBERS_COPPER_ORE(				ERO,					1.8F, 1, MOD + ":" + ERO,		SAME,				rng(1),		rng(0),		GUESS_EMBERS,	false, EMBERS),
 EMBERS_LEAD_ORE(				ERO,					2.5F, 2, MOD + ":" + ERO,		SAME,				rng(1),		rng(0),		GUESS_EMBERS,	false, EMBERS),
 EMBERS_NICKEL_ORE(				ERO,					2.2F, 1, MOD + ":" + ERO,		SAME,				rng(1),		rng(0),		GUESS_EMBERS,	false, EMBERS),
 EMBERS_SILVER_ORE(				ERO,					2.5F, 2, MOD + ":" + ERO,		SAME,				rng(1),		rng(0),		GUESS_EMBERS,	false, EMBERS),
 EMBERS_TIN_ORE(				ERO,					1.3F, 1, MOD + ":" + ERO,		SAME,				rng(1),		rng(0),		GUESS_EMBERS,	false, EMBERS),
 
 //Can't verify xp values.
 THAUMCRAFT_AMBER_ORE(			ERO,					2.0F, 1, MOD + ":amber",		MOD + ":" + ERO,	rng(1),		rng(1),		BUILTIN,		true, THAUMCRAFT),
 THAUMCRAFT_CINNABAR_ORE(		ERO,					2.0F, 2, MOD + ":" + ERO,		SAME,				rng(1),		rng(0),		BUILTIN,		true, THAUMCRAFT),
 
 MINERALOGY_PHOSPHOROUS_ORE(	MOD + "." + ORE,	1.5F, 0, MOD + ":phosphorous_dust",	MOD + ":" + ORE,	rng(1, 4),	rng(0),		GUESS_TEXTURE,	true, MINERALOGY),
 MINERALOGY_SULFUR_ORE(			MOD + "." + ORE,		1.5F, 0, MOD + ":sulfur_dust",	MOD + ":" + ORE,	rng(1, 4),	rng(0),		GUESS_TEXTURE,	false, MINERALOGY),
 
 MODERNMETALS_ALUMINUM_ORE(		MOD + "." + ORE,		3.75F,1, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
//MODERNMETALS_ALUMINUMBRASS_ORE(MOD + "." + ORE,		7.5F, 2, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_BERYLLIUM_ORE(	MOD + "." + ORE,		2.5F, 0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_BORON_ORE(		MOD + "." + ORE,		8.0F, 2, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_CADMIUM_ORE(		MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_CHROMIUM_ORE(		MOD + "." + ORE,		3.0F, 1, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
//MODERNMETALS_GALVANIZEDSTEEL_ORE(MOD + "." + ORE,		15.25F,5,MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_IRIDIUM_ORE(		MOD + "." + ORE,		3.0F, 1, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_MAGNESIUM_ORE(	MOD + "." + ORE,		3.5F, 1, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_MANGANESE_ORE(	MOD + "." + ORE,		2.75F,0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
//MODERNMETALS_NICHROME_ORE(	MOD + "." + ORE,		15.5F,5, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_OSMIUM_ORE(		MOD + "." + ORE,		2.75F,0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_PLUTONIUM_ORE(	MOD + "." + ORE,		7.5F, 2, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_RUTILE_ORE(		MOD + "." + ORE,		0.25F,0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
//MODERNMETALS_STAINLESSSTEEL_ORE(MOD + "." + ORE,		16.0F,5, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_TANTALUM_ORE(		MOD + "." + ORE,		17.5F,5, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_THORIUM_ORE(		MOD + "." + ORE,		1.0F, 0, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
//MODERNMETALS_TITANIUM_ORE(	MOD + "." + ORE,		16.25F,5,MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_TUNGSTEN_ORE(		MOD + "." + ORE,		12.5F,4, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_URANIUM_ORE(		MOD + "." + ORE,		16.25F,5,MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS),
 MODERNMETALS_ZIRCONIUM_ORE(	MOD + "." + ORE,		3.5F, 1, MOD + ":" + ORE,	 	SAME,				rng(1),		rng(0),		GUESS_TEXTURE,	false, MODERNMETALS);
		
		private DefaultOreProperties(String languageKey, float hardness, int level, String drop, String dropAlt, int[] dropRange, int[] xpRange, String originalTexture, boolean blendOverlay, PropertyGroup group)
		{
			languageKey = insertNameSpace(insertOreName(languageKey));
			drop = insertNameSpace(insertOreName(drop));
			dropAlt = insertNameSpace(insertOreName(dropAlt));
			dropAlt = dropAlt.equals(SAME) ? drop : dropAlt;
			
			DropProperties newDroperties = new DropProperties(drop, dropAlt, dropRange, xpRange);
			OreProperties newProperties = new OreProperties(toString().toLowerCase(), languageKey, hardness, level, newDroperties);
			
			setPropertyGroup(newProperties, group);

			setOriginalTexture(newProperties, originalTexture);
			
			if (blendOverlay) newProperties.setUseBlendedTextures();
		}
		
		private void setPropertyGroup(OreProperties props, PropertyGroup group)
		{
			if (toString().equals("QUARTZ_ORE") && ConfigFile.automaticQuartzVariants)
			{
				props.setPropertyGroup(VANILLA);
			}
			
			else props.setPropertyGroup(group);
		}
		
		private void setOriginalTexture(OreProperties props, String originalTexture)
		{
			if (!originalTexture.equals(BUILTIN))
			{
				props.setOriginalTexture(getTextureGuesser(originalTexture));
			}
		}
		
		private static int[] rng(int... range)
		{
			return range;
		}
		
		private String insertNameSpace(String s)
		{
			if (s.contains(MOD)) return s.replaceAll(MOD, getDomain());
			
			return s;
		}
		
		private String insertOreName(String s)
		{
			s = s.replaceAll(ORE, getOreName());

			s = s.replaceAll(ERO, getOreNameSplit()[1] + "_" + getOreNameSplit()[0]);
			
			return s;
		}
		
		private String getTextureGuesser(String s)
		{
			if (s.equals(GUESS_TEXTURE)) return guessTexture();
			if (s.equals(GUESS_THERMAL)) return guessThermalTextures();
			if (s.equals(GUESS_EMBERS)) return guessEmbersTextures();
			if (s.equals(GUESS_REVERSE)) return guessReverse();
			
			return "";
		}
		
		private String getDomain()
		{
			return NameReader.getMod(toString()).replaceAll("vanilla", "minecraft");
		}
		
		private String getOreName()
		{
			return NameReader.getOreWithoutMod(toString());
		}
		
		private String[] getOreNameSplit()
		{
			return getOreName().split("_");
		}
		
		private String guessTexture()
		{
			return "assets/" + getDomain() + "/textures/blocks/" + getOreName() + ".png";
		}
		
		private String guessReverse()
		{			
			return "assets/" + getDomain() + "/textures/blocks/" + getOreNameSplit()[1] + "_" + getOreNameSplit()[0] + ".png";	
		}
		
		private String guessThermalTextures()
		{
			return "assets/thermalfoundation/textures/blocks/ore/" + getOreNameSplit()[1] + "_" + getOreNameSplit()[0] + ".png";
		}
		
		private String guessEmbersTextures()
		{
			return "assets/embers/textures/blocks/" + getOreNameSplit()[1] + "_" + getOreNameSplit()[0] + "_vanilla" + ".png";			
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
		
		static //add light levels--there are too few of these to warrant an additional column.
		{			
			OreProperties.propertiesOf("lit_redstone_ore").setLightLevel(0.625F);
			OreProperties.propertiesOf("thermalfoundation_iridium_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_platinum_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_silver_ore").setLightLevel(4.0F);
			OreProperties.propertiesOf("thermalfoundation_mithril_ore").setLightLevel(8.0F);
			OreProperties.propertiesOf("mineralogy_phosphorous_ore").setBackgroundMatcher("assets/mineralogy/textures/blocks/limestone.png");
			OreProperties.propertiesOf("quark_biotite_ore").setBackgroundMatcher("assets/minecraft/textures/blocks/end_stone.png");
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
		QUARK.setConditions(Main.isQuarkLoaded() && ConfigFile.isSupportEnabled("quark"));												QUARK.register();
		MODERNMETALS.setConditions(Main.isModernMetalsLoaded() && ConfigFile.isSupportEnabled("modernmetals"));							MODERNMETALS.register();
		
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
 MINERALOGY_SULFUR_ORE(				16,		1,		16,		64,		NO_TYPE,								NO_NAMES),
 
 MODERNMETALS_ALUMINUM_ORE(			10,		8,		0,		96,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_BERYLLIUM_ORE(		5,		20,		0,		128,	NO_TYPE,								NO_NAMES),
 MODERNMETALS_BORON_ORE(			8,		8,		0,		32,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_CADMIUM_ORE(			4,		8,		0,		96,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_CHROMIUM_ORE(			2,		3,		0,		32,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_IRIDIUM_ORE(			6,		5,		0,		64,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_MAGNESIUM_ORE(		6,		8,		0,		96,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_MANGANESE_ORE(		6,		6,		0,		64,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_OSMIUM_ORE(			10,		8,		0,		96,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_PLUTONIUM_ORE(		2,		4,		0,		32,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_RUTILE_ORE(			6,		6,		0,		64,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_TANTALUM_ORE(			6,		6,		0,		64,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_THORIUM_ORE(			8,		8,		0,		32,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_TUNGSTEN_ORE(			4,		6,		0,		32,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_URANIUM_ORE(			2,		6,		0,		32,		NO_TYPE,								NO_NAMES),
 MODERNMETALS_ZIRCONIUM_ORE(		8,		6,		0,		64,		NO_TYPE,								NO_NAMES);
		
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
MINERALOGY_SULFUR_ORE(			"mineralogy:sulfur_dust",		0,		1,		1.0F),

MODERNMETALS_ALUMINUM_ORE(		"modernmetals:aluminum_ingot",	0,		1,		0.1F),
MODERNMETALS_BERYLLIUM_ORE(		"modernmetals:beryllium_ingot",	0,		1,		0.1F),
MODERNMETALS_BORON_ORE(			"modernmetals:boron_ingot",		0,		1,		0.1F),
MODERNMETALS_CADMIUM_ORE(		"modernmetals:cadmium_ingot",	0,		1,		0.1F),
MODERNMETALS_CHROMIUM_ORE(		"modernmetals:chromium_ingot",	0,		1,		0.1F),
MODERNMETALS_IRIDIUM_ORE(		"modernmetals:iridium_ingot",	0,		1,		0.1F),
MODERNMETALS_MAGNESIUM_ORE(		"modernmetals:magnesium_ingot",	0,		1,		0.1F),
MODERNMETALS_MANGANESE_ORE(		"modernmetals:manganese_ingot",	0,		1,		0.1F),
MODERNMETALS_OSMIUM_ORE(		"modernmetals:osmium_ingot",	0,		1,		0.1F),
MODERNMETALS_PLUTONIUM_ORE(		"modernmetals:plutonium_ingot",	0,		1,		0.1F),
MODERNMETALS_RUTILE_ORE(		"modernmetals:rutile_ingot",	0,		1,		0.1F),
MODERNMETALS_TANTALUM_ORE(		"modernmetals:tantalum_ingot",	0,		1,		0.1F),
MODERNMETALS_THORIUM_ORE(		"modernmetals:thorium_ingot",	0,		1,		0.1F),
MODERNMETALS_TUNGSTEN_ORE(		"modernmetals:tungsten_ingot",	0,		1,		0.1F),
MODERNMETALS_URANIUM_ORE(		"modernmetals:uranium_ingot",	0,		1,		0.1F),
MODERNMETALS_ZIRCONIUM_ORE(		"modernmetals:zirconium_ingot",	0,		1,		0.45F);
		
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
