package personthecat.mod.util.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.util.ShortTrans;

/*
 * For GitHub viewers looking for the default background block list,
 * open this file as a raw file, and then paste it into a text editor
 * such as notepad++ for proper tab spacing.
 * 
 * The default ore list(s) can be found under
 * personthecat.mod.properties.DefaultProperties.
 */
public class BlockStateGenerator
{
	private static final List<State> BASE = new ArrayList<>();
	private static final List<State> QUARK = new ArrayList<>();
	private static final List<State> MINERALOGY = new ArrayList<>();
	private static final List<State> MINERALOGY2 = new ArrayList<>();
	private static final List<State> UNDERGROUNDBIOMES1 = new ArrayList<>();
	private static final List<State> UNDERGROUNDBIOMES2 = new ArrayList<>();
	private static final List<State> UNDERGROUNDBIOMES3 = new ArrayList<>();
	private static final List<State> EARTHWORKS = new ArrayList<>();

	private static final String MOD = "usethedependencyname";
	private static final String BLK = "gettheblockfromname";
	
	private static final String IGN_STN = "igneous_stone";
	private static final String MET_STN = "metamorphic_stone";
	private static final String SED_STN = "sedimentary_stone";
	
	//Yes, the same enum is used for all stone variants. Lazy? na.
	public enum State implements IStringSerializable
	{		
		//    					dependency,		dep.name,			trans key,					bg registry	
		STONE(					BASE, 			"base", 			"stone." + BLK, 		"minecraft:stone"),
		ANDESITE(				BASE, 			"base", 			"stone." + BLK,			"minecraft:stone:5"),
		DIORITE(				BASE, 			"base", 			"stone." + BLK, 		"minecraft:stone:3"),
		GRANITE(				BASE, 			"base", 			"stone." + BLK,			"minecraft:stone:1"),
		
		QUARK_LIMESTONE(		QUARK, 			"quark", 			MOD + ":stone_" + BLK, 	MOD + ":" + BLK),
		QUARK_MARBLE(			QUARK, 			"quark", 			MOD + ":stone_" + BLK, 	MOD + ":" + BLK),
		
		MINERALOGY_AMPHIBOLITE(	MINERALOGY, 	"mineralogy", 		MOD + "." + BLK,		MOD + ":" + BLK),
		MINERALOGY_ANDESITE(	MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_BASALT(		MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_CHERT(		MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_CONGLOMERATE(MINERALOGY, 	"mineralogy", 		MOD + "." + BLK,		MOD + ":" + BLK),
		MINERALOGY_DIORITE(		MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_DOLOMITE(	MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_GRANITE(		MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_GYPSUM(		MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_LIMESTONE(	MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_MARBLE(		MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_PEGMATITE(	MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_PHYLLITE(	MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_PUMICE(		MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_RHYOLITE(	MINERALOGY, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY_SCHIST(		MINERALOGY, 	"mineralogy",		MOD + "." + BLK,		MOD + ":" + BLK),
		
		MINERALOGY2_SHALE(		MINERALOGY2, 	"mineralogy", 		MOD + "." + BLK, 		MOD + ":" + BLK),
		MINERALOGY2_SLATE(		MINERALOGY2, 	"mineralogy",		MOD + "." + BLK,		MOD + ":" + BLK),
		
		UB1_ANDESITE(		UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":3"),
		UB1_BASALT(			UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":5"),
		UB1_BLACK_GRANITE(	UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":1"),
		UB1_DACITE(			UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":7"),
		UB1_GABBRO(			UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":4"),
		UB1_KOMATIITE(		UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":6"),
		UB1_RED_GRANITE(	UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":0"),
		UB1_RHYOLITE(		UNDERGROUNDBIOMES1, "undergroundbiomes", IGN_STN + "." + BLK, 	MOD + ":" + IGN_STN + ":2"),
		
		UB2_BLUE_SCHIST(	UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":4"),
		UB2_ECLOGITE(		UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":1"),
		UB2_GNEISS(			UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":0"),
		UB2_GREEN_SCHIST(	UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":5"),
		UB2_MARBLE(			UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":2"),
		UB2_MIGMATITE(		UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":7"),
		UB2_QUARTZITE(		UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":3"),
		UB2_SOAPSTONE(		UNDERGROUNDBIOMES2, "undergroundbiomes", MET_STN + "." + BLK,	MOD + ":" + MET_STN + ":6"),
		
		UB3_CHALK(			UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":1"),
		UB3_CHERT(			UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":7"),
		UB3_DOLOMITE(		UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":5"),
		UB3_GREYWACKE(		UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":6"),
		UB3_LIGNITE(		UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":4"),
		UB3_LIMESTONE(		UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":0"),
		UB3_SHALE(			UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":2"),
		UB3_SILTSTONE(		UNDERGROUNDBIOMES3, "undergroundbiomes", SED_STN + "." + BLK,	MOD + ":" + SED_STN + ":3"),
		
		
		EARTHWORKS_CHALK(		EARTHWORKS,		"earthworks",		MOD + ".block_" + BLK,	MOD + ":block_" + BLK),
		EARTHWORKS_SLATE(		EARTHWORKS,		"earthworks",		MOD + ".block_" + BLK,	MOD + ":block_" + BLK),
		EARTHWORKS_SLATE_GREEN(	EARTHWORKS,		"earthworks",		MOD + ".block_" + BLK,	MOD + ":block_" + BLK),
		EARTHWORKS_SLATE_PURPLE(EARTHWORKS,		"earthworks",		MOD + ".block_" + BLK,	MOD + ":block_" + BLK);
		
		private String dependency, languageKey, backgroundBlock;
		private boolean isDependencyMet;
		
		private List<State> dependencyList;

		private State(List<State> dependency, String dependencyName, String languageKey, String backgroundBlock)
		{
			this.isDependencyMet = dependencyName == "base" ? true : Loader.isModLoaded(dependencyName);
			dependency.add(this);

			this.dependency = dependencyName;
			this.dependencyList = dependency;
			this.languageKey = insertModName(insertBlockName(languageKey));
			this.backgroundBlock = insertModName(insertBlockName(backgroundBlock));
		}
		
		private static final Map<String, List<State>> MOD_MAP = new HashMap<String, List<State>>();
		
		static
		{
			MOD_MAP.put("base", BASE);
			MOD_MAP.put("quark", QUARK);
			MOD_MAP.put("mineralogy", MINERALOGY);
			MOD_MAP.put("mineralogy2", MINERALOGY2);
			MOD_MAP.put("undergroundbiomes1", UNDERGROUNDBIOMES1);
			MOD_MAP.put("undergroundbiomes2", UNDERGROUNDBIOMES2);
			MOD_MAP.put("undergroundbiomes3", UNDERGROUNDBIOMES3);
			MOD_MAP.put("earthworks", EARTHWORKS);
		}
		
		public static Collection<State> getStatesForModName(String name)
		{
			return MOD_MAP.get(name);
		}
		
		//Originally thought this was neater than casting. May remove it. 
		public static List<State> getStateListForModName(String name)
		{
			return MOD_MAP.get(name);
		}
		
		private String getNameFromFull()
		{
			String[] nameSplit = toString().toLowerCase().split("_");
			
			if (nameSplit.length > 1)
			{
				return toString().toLowerCase().replaceAll(nameSplit[0] + "_", "");
			}
			
			return toString().toLowerCase();
		}
		
		private String insertBlockName(String s)
		{
			return s.replaceAll(BLK, getNameFromFull());
		}
		
		private String insertModName(String s)
		{
			return s.replaceAll(MOD, dependency);
		}
		
		@Override
		public String getName() 
		{
			return getNameFromFull();
		}
		
		public int getMeta()
		{
			return dependencyList.indexOf(this);
		}
		
		public String getDependency()
		{
			return dependency;
		}
		
		public boolean isDependencyMet()
		{
			return isDependencyMet;
		}
		
		public IBlockState getBackgroundBlockState()
		{
			IBlockState state = null;
			String[] splitBGString = backgroundBlock.split(":");
			
			if (splitBGString.length == 3)
			{
				state = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(splitBGString[0], splitBGString[1])).getStateFromMeta(Integer.parseInt(splitBGString[2]));
			}
			
			else state = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(backgroundBlock)).getDefaultState();
			
			return state;
		}
		
		public ModelResourceLocation getBackgroundModelLocation()
		{
			BlockStateMapper stateMapper = new BlockStateMapper();
			IBlockState state = this.getBackgroundBlockState();
			Map<IBlockState, ModelResourceLocation> locationMapped = stateMapper.getVariants(state.getBlock());
			return locationMapped.get(state);
		}
		
		//Why was this returning the normal name, again?
		public String getUnlocalizedName() 
		{
			return getNameFromFull();
		}
		
		public String getLocalizedName()
		{
			return ShortTrans.unformatted("tile." + this.languageKey + ".name");
		}
	
		public static State byMetadata(String forStateSet, int meta) 
		{
			State[] lookup = new State[getStateListForModName(forStateSet).size()];
			
			for (State state : getStateListForModName(forStateSet))
			{
				lookup[state.getMeta()] = state;
			}

			return lookup[meta];
		}		
	}
}