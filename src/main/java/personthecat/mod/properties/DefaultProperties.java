package personthecat.mod.properties;

import com.google.gson.JsonObject;

import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.Loader;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.JsonReader;
import personthecat.mod.properties.OreProperties.DropProperties;

/**
 * Default stone types are listed under 
 * personthecat.mod.util.handlers.BlockStateGenerator.
 * 
 * For anyone searching for a list of default properties, open this as a raw file on GitHub,
 * and then paste it into another program such as notepad++ for correct tab spacing.
 * 
 * Substitutions for reading this as a block list:
 * 
 *  * mod() = the mod's name
 *  * ore() = block_ore
 *  * ero() = ore_block
 *    * e.g. mod() + "." ore() = "basemetals.copper_ore"
 *  * fullName() = modname_block_ore
 *  * SAME = repeat what came before it
 *  * rng(numbers) = a range of the numbers in parentheses.
 *  * Domains are automatically added to drops 
 *    * e.g. a drop of ore() = "simpleores:copper_ore"
 * 
 * Enjoy this unusually horizontal class!
 */
public class DefaultProperties
{
	/**
	 * Init PropertyGroups	
	 */
	public static final PropertyGroup
		
		VANILLA = new PropertyGroup("minecraft"),
		ICEANDFIRE = new PropertyGroup("iceandfire"),
		SIMPLEORES = new PropertyGroup("simpleores"),
		BASEMETALS = new PropertyGroup("basemetals"),
		BIOMESOPLENTY = new PropertyGroup("biomesoplenty"),
		GLASSHEARTS = new PropertyGroup("glasshearts"),
		THERMALFOUNDATION = new PropertyGroup("thermalfoundation"),
		IMMERSIVEENGINEERING = new PropertyGroup("immersiveengineering"),
		THAUMCRAFT = new PropertyGroup("thaumcraft"),
		EMBERS = new PropertyGroup("embers"),
		MINERALOGY = new PropertyGroup("mineralogy"),
		MODERNMETALS = new PropertyGroup("modernmetals"),
		QUARK = new PropertyGroup("quark"),
		DONT_SPAWN = new PropertyGroup("unassigned_property_group");

	/**
	 * Field shorteners
	 */
	private static final String 
		
		SAME = "thesameasthelastvalue",	
		GUESS_TEXTURE = "usethenormaltexturepathformat",
		GUESS_REVERSE = "samebuttheorenameisreversed",
		GUESS_THERMAL = "thermalfoundationsformat",
		GUESS_EMBERS = "embersformat",
		BUILTIN = "alreadyhavethese";
	
	public static class DefaultOreProperties
	{
		static
		{
//			modname_x_ore			languageKey, 	   		hardness, level, drop, 	dropSilkTouch,	dropRange, 	xpRange,	originalTexture, blend, spawn
			
		group(VANILLA);
			
			ore("coal"); 			c("oreCoal", 				3.0F, 0, "coal",		ore(),		rng(1), 	rng(0, 2),	GUESS_TEXTURE,	false, true);	
			ore("diamond");			c("oreDiamond", 			3.0F, 2, "diamond",	 	ore(),		rng(1), 	rng(3, 7),	GUESS_TEXTURE,	false, true);
			ore("emerald");			c("oreEmerald", 			3.0F, 2, "emerald",  	ore(),		rng(1), 	rng(3, 7),	BUILTIN,		true,  true);
			ore("gold");			c("oreGold", 				3.0F, 2, ore(), 		SAME,		rng(1), 	rng(0),		GUESS_TEXTURE,	false, true);
			ore("iron");			c("oreIron", 				3.0F, 1, ore(), 		SAME,		rng(1), 	rng(0),		GUESS_TEXTURE,	false, true);
			ore("lapis");			c("oreLapis", 				3.0F, 2, "dye:4", 		ore(),		rng(4, 8), 	rng(2, 5),	GUESS_TEXTURE,	false, true);
			ore("redstone");		c("oreRedstone", 			3.0F, 2, "redstone", 	ore(),		rng(4, 5), 	rng(1, 5),	GUESS_TEXTURE,	false, true);
			ore("lit_redstone");	c("oreRedstone", 			3.0F, 2, "redstone","redstone_ore",	rng(4, 5), 	rng(1, 5),	GUESS_TEXTURE,	false, false);
			ore("quartz");			c("netherquartz",			3.0F, 1, "quartz", 		ore(),		rng(1),		rng(2, 5),	BUILTIN,		true,  false);
	
		group(QUARK);
			    
			ore("biotite");			c(mod() + ":" + ore(),		3.0F, 1, "biotite",		ore(),		rng(1),		rng(1, 3),	GUESS_TEXTURE,	true, false);
		
	 	group(ICEANDFIRE);
	 
	 		ore("sapphire");		c(mod() + ".sapphireOre",	3.0F, 2,"sapphire_gem",	ore(),		rng(1),		rng(0),		BUILTIN,		false, true);
	 		ore("silver");			c(mod() + ".silverOre",		3.0F, 2, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 
	 	group(SIMPLEORES);
	 
	 		ore("adamantium");		c(ore(),					5.0F, 2, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("copper");			c(ore(),					1.7F, 1, ore(),			SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("mythril");			c(ore(),					4.0F, 2, ore(),			SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("tin");				c(ore(),					3.0F, 1, ore(),	 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("onyx");			c(ore(),					7.0F, 3, "onyx_gem",	ore(),		rng(1),		rng(0),		BUILTIN,		false, false);
	 
	 	group(BASEMETALS);
	 
	 		ore("antimony"); 		c(mod() + "." + ore(),		1.0F, 0, ore(),	 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("bismuth"); 		c(mod() + "." + ore(),		1.0F, 0, ore(),	 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("copper"); 			c(mod() + "." + ore(),		4.0F, 1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("lead"); 			c(mod() + "." + ore(),		1.0F, 0, ore(),			SAME,		rng(1),		rng(0),		BUILTIN,		false, true);
	 		ore("mercury"); 		c(mod() + "." + ore(),		1.0F, 0, ore(),			SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("nickel"); 			c(mod() + "." + ore(),		4.0F, 1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("pewter"); 			c(mod() + "." + ore(),		1.0F, 0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("platinum"); 		c(mod() + "." + ore(),		3.0F, 1, ore(),			SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("silver"); 			c(mod() + "." + ore(),		5.0F, 1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("tin"); 			c(mod() + "." + ore(), 		1.0F, 1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("zinc"); 			c(mod() + "." + ore(),		1.0F, 0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("adamantine"); 		c(mod() + "." + ore(),		12.0F,4, ore(), 		SAME,		rng(1),		rng(0),		BUILTIN,		false, false);
	 		ore("coldiron");		c(mod() + "." + ore(),		7.0F, 2, ore(),	 		SAME,		rng(1),		rng(0),		BUILTIN,		false, false);
	 		ore("cupronickel");		c(mod() + "." + ore(),		6.0F, 2, ore(),			SAME,		rng(1),		rng(0),		BUILTIN,		false, false);
	 		ore("starsteel");		c(mod() + "." + ore(), 		10.0F,3, ore(),			SAME,		rng(1),		rng(0),		BUILTIN,		false, false);
	 
		group(BIOMESOPLENTY);
	 
			ore("amber");			c("gem_ore." + ore(),		3.0F, 2, "gem:7",  	"gem_ore:7",	rng(1),		rng(3, 7),	GUESS_TEXTURE,	true, true);
			ore("malachite");		c("gem_ore." + ore(),		3.0F, 2, "gem:5", 	"gem_ore:5",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, true);
			ore("peridot");			c("gem_ore." + ore(),		3.0F, 2, "gem:2",  	"gem_ore:2",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, true);
			ore("ruby");			c("gem_ore." + ore(),		3.0F, 2, "gem:1", 	"gem_ore:1",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, true);
			ore("sapphire");		c("gem_ore." + ore(),		3.0F, 2, "gem:6",  	"gem_ore:6",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, true);
			ore("tanzanite");		c("gem_ore." + ore(),		3.0F, 2, "gem:4", 	"gem_ore:4",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, true);
			ore("topaz");			c("gem_ore." + ore(),		3.0F, 2, "gem:3",	"gem_ore:3",	rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, true);
			ore("amethyst");		c("gem_ore." + ore(),		3.0F, 3, "gem",  	"gem_ore", 		rng(1), 	rng(3, 7),	GUESS_TEXTURE,	true, false);
	 
	 	group(GLASSHEARTS);
	 
	 		ore("agate");			c(mod() + ".ore.agate",		3.0F, 2, "gem:7",  		"ore:7", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, true);
	 		ore("amethyst");		c(mod() + ".ore.amethyst",	3.0F, 2, "gem",  		"ore", 		rng(1), 	rng(0),		GUESS_TEXTURE,	true, true);
	 		ore("onyx");			c(mod() + ".ore.onyx",		3.0F, 2, "gem:5",  		"ore:5", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, true);
	 		ore("opal");			c(mod() + ".ore.opal",		3.0F, 2, "gem:4", 		"ore:4", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, true);
	 		ore("ruby");			c(mod() + ".ore.ruby",		3.0F, 2, "gem:1", 	 	"ore:1", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, true);
	 		ore("sapphire");		c(mod() + ".ore.sapphire",	3.0F, 2, "gem:3", 	 	"ore:3", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, true);
	 		ore("topaz");			c(mod() + ".ore.topaz",		3.0F, 2, "gem:2", 	 	"ore:2", 	rng(1), 	rng(0),		GUESS_TEXTURE,	true, true);
	 	
	 	group(THERMALFOUNDATION);
	 
	 		ore("aluminum");		c(mod() + ".ore.aluminum",	3.0F, 1, "ore:4", 		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, false);
	 		ore("copper");			c(mod() + ".ore.copper",	3.0F, 1, "ore",   		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, true);
	 		ore("iridium");			c(mod() + ".ore.iridium",	3.0F, 1, "ore:7", 		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, false);
	 		ore("lead");			c(mod() + ".ore.lead",		3.0F, 1, "ore:3", 		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, true);
	 		ore("mithril");			c(mod() + ".ore.mithril",	3.0F, 1, "ore:8", 		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, false);
	 		ore("nickel");			c(mod() + ".ore.nickel",	3.0F, 1, "ore:5", 		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, true);
	 		ore("platinum");		c(mod() + ".ore.platinum",	3.0F, 1, "ore:6", 		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, false);
	 		ore("silver");			c(mod() + ".ore.silver",	3.0F, 1, "ore:2", 		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, true);
	 		ore("tin");				c(mod() + ".ore.tin",		3.0F, 1, "ore:1",		SAME,		rng(1),		rng(0),		GUESS_THERMAL,	false, true);
	
		group(IMMERSIVEENGINEERING);
	
			ore("aluminum");		c(mod() + ".ore.aluminum",	3.0F, 1, "ore:1", 		SAME,		rng(1),		rng(0),		BUILTIN,		true, true);
			ore("copper");			c(mod() + ".ore.copper",	3.0F, 1, "ore", 		SAME,		rng(1),		rng(0),		BUILTIN,		true, true);
			ore("lead");			c(mod() + ".ore.lead",		3.0F, 1, "ore:2", 		SAME,		rng(1),		rng(0),		BUILTIN,		true, true);
			ore("nickel");			c(mod() + ".ore.nickel",	3.0F, 2, "ore:4", 		SAME,		rng(1),		rng(0),		BUILTIN,		true, true);
			ore("silver");			c(mod() + ".ore.silver",	3.0F, 2, "ore:3", 		SAME,		rng(1),		rng(0),		BUILTIN,		true, true);
			ore("uranium");			c(mod() + ".ore.uranium",	3.0F, 2, "ore:5", 		SAME,		rng(1),		rng(0),		BUILTIN,		true, true);
	
		group(EMBERS);
	
			ore("aluminum");		c(ero(),					1.6F, 1, ero(),			SAME,		rng(1),		rng(0),		GUESS_EMBERS,	false, true);
			ore("copper");			c(ero(),					1.8F, 1, ero(),			SAME,		rng(1),		rng(0),		GUESS_EMBERS,	false, true);
			ore("lead");			c(ero(),					2.5F, 2, ero(),			SAME,		rng(1),		rng(0),		GUESS_EMBERS,	false, true);
			ore("nickel");			c(ero(),					2.2F, 1, ero(),			SAME,		rng(1),		rng(0),		GUESS_EMBERS,	false, true);
			ore("silver");			c(ero(),					2.5F, 2, ero(),			SAME,		rng(1),		rng(0),		GUESS_EMBERS,	false, true);
			ore("tin");				c(ero(),					1.3F, 1, ero(),			SAME,		rng(1),		rng(0),		GUESS_EMBERS,	false, true);

		group(THAUMCRAFT); //Can't verify these xp values.
	 
			ore("amber");			c(ero(),					2.0F, 1, "amber",		ero(),		rng(1),		rng(1),		BUILTIN,		true, true);
			ore("cinnabar");		c(ero(),					2.0F, 2, ero(),			SAME,		rng(1),		rng(0),		BUILTIN,		true, true);
			
	 	group(MINERALOGY);
	 
	 		ore("phosphorous");		c(mod() + "." + ore(),		1.5F, 0, "phosphorous_dust",ore(),	rng(1, 4),	rng(0),		GUESS_TEXTURE,	true,  true);
	 		ore("sulfur");			c(mod() + "." + ore(),		1.5F, 0, "sulfur_dust",	ore(),		rng(1, 4),	rng(0),		GUESS_TEXTURE,	false, true);
	 
	 	group(MODERNMETALS);
	 
	 		ore("aluminum");		c(mod() + "." + ore(),		3.75F, 1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
//			ore("aluminumbrass");	c(mod() + "." + ore(),		7.5F,  2, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("beryllium");		c(mod() + "." + ore(),		2.5F,  0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("boron");			c(mod() + "." + ore(),		8.0F,  2, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("cadmium");			c(mod() + "." + ore(),		1.0F,  0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("chromium");		c(mod() + "." + ore(),		3.0F,  1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
//			ore("galvanizedsteel");	c(mod() + "." + ore(),		15.25F,5, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("iridium");			c(mod() + "." + ore(),		3.0F,  1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("magnesium");		c(mod() + "." + ore(),		3.5F,  1, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("manganese");		c(mod() + "." + ore(),		2.75F, 0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
//			ore("nichrome");		c(mod() + "." + ore(),		15.5F, 5, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("osmium");			c(mod() + "." + ore(),		2.75F, 0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("plutonium");		c(mod() + "." + ore(),		7.5F,  2, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("rutile");			c(mod() + "." + ore(),		0.25F, 0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
//			ore("stainlesssteel");	c(mod() + "." + ore(),		16.0F, 5, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("tantalum");		c(mod() + "." + ore(),		17.5F, 5, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("thorium");			c(mod() + "." + ore(),		1.0F,  0, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
//			ore("titanium");		c(mod() + "." + ore(),		16.25F,5, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("tungsten");		c(mod() + "." + ore(),		12.5F, 4, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("uranium");			c(mod() + "." + ore(),		16.25F,5, ore(), 		SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
	 		ore("zirconium"); 		c(mod() + "." + ore(),		3.5F,  1, ore(),	 	SAME,		rng(1),		rng(0),		GUESS_TEXTURE,	false, true);
			
	 		
	 		loadAdditionalSettings();
		}
		
		private static PropertyGroup propertyGroupStorage;
		private static String nameStorage;
		
		private static void c(String languageKey, float hardness, int level, String drop, String dropAlt, int[] dropRange, int[] xpRange, String originalTexture, boolean blendOverlay, boolean spawn)
		{
			dropAlt = dropAlt.equals(SAME) ? drop : dropAlt;

			DropProperties newDroperties = new DropProperties(drop(drop), drop(dropAlt), dropRange, xpRange);
			OreProperties newProperties = new OreProperties(fullName(), languageKey, hardness, level, newDroperties);
			
			setOriginalTexture(newProperties, originalTexture);
			if (blendOverlay) newProperties.setUseBlendedTextures();
			setPropertyGroup(newProperties, spawn);
		}
		
		private static void group(PropertyGroup group)
		{
			propertyGroupStorage = group;
		}
		
		private static void ore(String name)
		{
			nameStorage = name;
		}
		
		/**
		 * Property setters
		 */

		private static void setPropertyGroup(OreProperties props, boolean spawn)
		{
			if (nameStorage.equals("quartz") && ConfigFile.automaticQuartzVariants)
			{
				props.setPropertyGroup(VANILLA);
			}
			
			else
			{
				if (spawn) props.setPropertyGroup(propertyGroupStorage);
				
				else props.setPropertyGroup(DONT_SPAWN);
			}
		}
		
		private static void setOriginalTexture(OreProperties props, String originalTexture)
		{
			if (!originalTexture.equals(BUILTIN))
			{
				props.setOriginalTexture(getTextureGuesser(originalTexture));
				
				if (Loader.isModLoaded(mod())) props.testTextures();
			}
		}
		
		/**
		 * Field shorteners
		 */
		
		private static String mod()
		{
			return propertyGroupStorage.getModName();
		}
		
		private static String ore()
		{
			return nameStorage + "_ore";
		}
		
		private static String ero()
		{
			return "ore_" + nameStorage;
		}
		
		private static String fullName()
		{
			return mod().contains("minecraft") ? nameStorage + "_ore" : mod() + "_" + nameStorage + "_ore";
		}

		private static int[] rng(int... range)
		{
			return range;
		}
		
		private static String drop(String resourcePath)
		{
			return mod() + ":" + resourcePath;
		}
		
		/**
		 * Texture finders
		 */
		
		private static String getTextureGuesser(String s)
		{
			if (s.equals(GUESS_TEXTURE)) return path(ore());
			if (s.equals(GUESS_REVERSE)) return path(ero());
			if (s.equals(GUESS_THERMAL)) return path("ore/" + ero());
			if (s.equals(GUESS_EMBERS)) return path(ero() + "_vanilla");
			
			return s;
		}
		
		private static String path(String endOfPath)
		{
			return "assets/" + mod() + "/textures/blocks/" + endOfPath + ".png";
		}
		
		/**
		 * Miscellaneous
		 */
		
		public static String getFormattedBlockList()
		{
			String blockList = "";
			
			for (PropertyGroup group : PropertyGroup.getSortedPropertyGroups())
			{
				if (!group.getProperties().isEmpty()) //Don't just print group names
				{
					blockList += group.getModName() + ":\n\n\t";

					for (OreProperties property : group.getProperties())
					{
						int lineLength = blockList.substring(blockList.lastIndexOf("\n\t")).length();
						
						//Not the first entry on the line? Add a comma + space.
						if (lineLength > 4) blockList += ", ";
						
						//Previous line is too long? Add a new line + tab.
						if (lineLength + property.getName().length() > 105) blockList += "\n\t";
						
						blockList += property.getName();
					}
					
					blockList += "\n\n";
				}
			}
			
			return blockList;
		}
		
		private static void loadAdditionalSettings() //There are too few of these to warrant additional columns.
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
		for (PropertyGroup group : PropertyGroup.getPropertyGroupRegistry())
		{
			if (group.getModName().equals("minecraft"))
			{
				group.setConditions(ConfigFile.isSupportEnabled("vanilla"));
			}
			
			else if (group.getModName().equals("unassigned_property_group"))
			{
				group.setConditions(false);
			}
			
			else group.setDefaultConditions();
		}
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
		
		private DefaultWorldGenProperties(int blockCount, int frequency, int minHeight, int maxHeight, Type[] biomeType, String[] biomeLookup)
		{
			WorldGenProperties genProp = new WorldGenProperties(toString().toLowerCase(), blockCount, frequency, minHeight, maxHeight, biomeType, biomeLookup);
				
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
