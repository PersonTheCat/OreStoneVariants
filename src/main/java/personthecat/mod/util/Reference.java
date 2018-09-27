package personthecat.mod.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class Reference
{
	public static final String
	
		MODID = "ore_stone_variants",
		NAME = "Ore Stone Variants",
		VERSION = "4.0",
		AUTHORS = "PersonTheCat",
		CREDITS = "pupnewfster, Harry's Tech Reviews, CJMinecraft",
		CLIENT = "personthecat.mod.proxy.ClientProxy",
		COMMON = "personthecat.mod.proxy.CommonProxy",
		DEPENDENCIES =
		
			"after:quark;			after:iceandfire;		after:simpleores;"
		  + "after:basemetals;		after:biomesoplenty;	after:glasshearts;"
		  + "after:thermalfoundation;after:embers;			after:undergroundbiomes;"
		  + "after:mineralogy;		after:thaumcraft;		after:immersiveengineering;"
		  + "after:earthworks;		after:modernmetals;		after:geolosys;"
		  + "after:rustic;";
	
	/**
	 * To be loaded before the lang file.
	 */
	public static final Map<String, String> SUPPORTED_MOD_NAMES = new LinkedHashMap<>();
	
	static { put(new String[][]
	{
		/**ID					formatted				   // # */
		{ "minecraft",			"Vanilla"				}, // 0
		{ "quark",				"Quark"					}, // 1
		{ "iceandfire",			"Ice and Fire"			}, // 2
		{ "simpleores",			"SimpleOres"			}, // 3
		{ "basemetals",			"BaseMetals"			}, // 4
		{ "biomesoplenty",		"Biomes O' Plenty"		}, // 5
		{ "glasshearts",		"Glass Hearts"			}, // 6
		{ "thermalfoundation",	"Thermal Foundation"	}, // 7
		{ "embers",				"Embers"				}, // 8
		{ "undergroundbiomes",	"Underground Biomes"	}, // 9
		{ "mineralogy",			"Mineralogy"			}, // 10
		{ "thaumcraft",			"Thaumcraft"			}, // 11
		{ "immersiveengineering","Immersive Engineering"}, // 12
		{ "earthworks",			"Earthworks"			}, // 13
		{ "modernmetals",		"Modern Metals"			}, // 14
		{ "geolosys",			"Geolosys"				}, // 15
		{ "rustic",				"Rustic"				}  // 16
	});}
	
	public static String[] getSupportedModIDs()
	{
		return new ArrayList<String>(SUPPORTED_MOD_NAMES.keySet()).toArray(new String[0]);
	}
	
	private static final int[] MODS_WITH_CHEAP_HAX = new int[]
	{
	 	2, 3, 4, 5, 6, 8, 11, 12
	};
	
	public static boolean hasCheapHax(String modid)
	{
		List<String> values = new ArrayList<>(SUPPORTED_MOD_NAMES.keySet());

		return ArrayUtils.contains(MODS_WITH_CHEAP_HAX, values.indexOf(modid));
	}
	
	private static void put(String[]... entries)
	{
		for (String[] entry : entries)
		{
			SUPPORTED_MOD_NAMES.put(entry[0], entry[1]);
		}
	}
}