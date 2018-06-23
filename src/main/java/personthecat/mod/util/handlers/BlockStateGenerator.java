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

public class BlockStateGenerator
{
	private static final List<State> BASE = new ArrayList<>();
	private static final List<State> QUARK = new ArrayList<>();
	private static final List<State> MINERALOGY = new ArrayList<>();
	private static final List<State> MINERALOGY2 = new ArrayList<>();
	private static final List<State> UNDERGROUNDBIOMES1 = new ArrayList<>();
	private static final List<State> UNDERGROUNDBIOMES2 = new ArrayList<>();
	private static final List<State> UNDERGROUNDBIOMES3 = new ArrayList<>();
	
	//Yes, the same enum is used for all stone variants. Lazy? na.
	public enum State implements IStringSerializable
	{		
		//    					name,		meta,	dependency,		dep.name,		trans key,					bg registry,				(optional) force texture location	
		STONE(					"stone", 		0, 	BASE, 			"base", 		"stone.stone", 				"minecraft:stone"),
		ANDESITE(				"andesite", 	1, 	BASE, 			"base", 		"stone.andesite",			"minecraft:stone:5"),
		DIORITE(				"diorite", 		2, 	BASE, 			"base", 		"stone.diorite", 			"minecraft:stone:3"),
		GRANITE(				"granite", 		3, 	BASE, 			"base", 		"stone.granite",			"minecraft:stone:1"),
		
		QUARK_LIMESTONE(		"limestone", 	0, 	QUARK, 			"quark", 		"quark:stone_limestone", 	"quark:limestone"),
		QUARK_MARBLE(			"marble", 		1, 	QUARK, 			"quark", 		"quark:stone_marble", 		"quark:marble"),
		
		MINERALOGY_AMPHIBOLITE(	"amphibolite", 	0, 	MINERALOGY, 	"mineralogy", 	"mineralogy.amphibolite",	"mineralogy:amphibolite"),
		MINERALOGY_ANDESITE(	"andesite", 	1, 	MINERALOGY, 	"mineralogy", 	"mineralogy.andesite", 		"mineralogy:andesite"),
		MINERALOGY_BASALT(		"basalt", 		2, 	MINERALOGY, 	"mineralogy", 	"mineralogy.basalt", 		"mineralogy:basalt"),
		MINERALOGY_CHERT(		"chert", 		3, 	MINERALOGY, 	"mineralogy", 	"mineralogy.chert", 		"mineralogy:chert"),
		MINERALOGY_CONGLOMERATE("conglomerate", 4, 	MINERALOGY, 	"mineralogy", 	"mineralogy.conglomerate",	"mineralogy:conglomerate"),
		MINERALOGY_DIORITE(		"diorite", 		5, 	MINERALOGY, 	"mineralogy", 	"mineralogy.diorite", 		"mineralogy:diorite"),
		MINERALOGY_DOLOMITE(	"dolomite", 	6, 	MINERALOGY, 	"mineralogy", 	"mineralogy.dolomite", 		"mineralogy:dolomite"),
		MINERALOGY_GRANITE(		"granite", 		7, 	MINERALOGY, 	"mineralogy", 	"mineralogy.granite", 		"mineralogy:granite"),
		MINERALOGY_GYPSUM(		"gypsum", 		8, 	MINERALOGY, 	"mineralogy", 	"mineralogy.gypsum", 		"mineralogy:gypsum"),
		MINERALOGY_LIMESTONE(	"limestone", 	9, 	MINERALOGY, 	"mineralogy", 	"mineralogy.limestone", 	"mineralogy:limestone"),
		MINERALOGY_MARBLE(		"marble", 		10,	MINERALOGY, 	"mineralogy", 	"mineralogy.marble", 		"mineralogy:marble"),
		MINERALOGY_PEGMATITE(	"pegmatite", 	11, MINERALOGY, 	"mineralogy", 	"mineralogy.pegmatite", 	"mineralogy:pegmatite"),
		MINERALOGY_PHYLLITE(	"phyllite", 	12, MINERALOGY, 	"mineralogy", 	"mineralogy.phyllite", 		"mineralogy:phyllite"),
		MINERALOGY_PUMICE(		"pumice", 		13, MINERALOGY, 	"mineralogy", 	"mineralogy.pumice", 		"mineralogy:pumice"),
		MINERALOGY_RHYOLITE(	"rhyolite", 	14, MINERALOGY, 	"mineralogy", 	"mineralogy.rhyolite", 		"mineralogy:rhyolite"),
		MINERALOGY_SCHIST(		"schist",		15, MINERALOGY, 	"mineralogy",	"mineralogy.schist",		"mineralogy:schist"),
		
		MINERALOGY2_SHALE(		"shale", 		0, 	MINERALOGY2, 	"mineralogy", 	"mineralogy.shale", 		"mineralogy:shale"),
		MINERALOGY2_SLATE(		"slate",		1,	MINERALOGY2, 	"mineralogy",	"mineralogy.slate",			"mineralogy:slate"),
		
		UB1_ANDESITE(		"andesite", 	0, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.andesite", 		"undergroundbiomes:igneous_stone:3"),
		UB1_BASALT(			"basalt",		1, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.basalt", 		"undergroundbiomes:igneous_stone:5"),
		UB1_BLACK_GRANITE(	"black_granite",2, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.black_granite", 	"undergroundbiomes:igneous_stone:1"),
		UB1_DACITE(			"dacite",		3, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.dacite", 		"undergroundbiomes:igneous_stone:7"),
		UB1_GABBRO(			"gabbro",		4, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.gabbro", 		"undergroundbiomes:igneous_stone:4"),
		UB1_KOMATIITE(		"komatiite",	5, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.komatiite", 		"undergroundbiomes:igneous_stone:6"),
		UB1_RED_GRANITE(	"red_granite",	6, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.red_granite", 	"undergroundbiomes:igneous_stone:0"),
		UB1_RHYOLITE(		"rhyolite",		7, UNDERGROUNDBIOMES1, "undergroundbiomes", "igneous_stone.rhyolite", 		"undergroundbiomes:igneous_stone:2"),
		
		UB2_BLUE_SCHIST(	"blue_schist", 	0, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.blueschist",	"undergroundbiomes:metamorphic_stone:4"),
		UB2_ECLOGITE(		"eclogite", 	1, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.greenschist","undergroundbiomes:metamorphic_stone:1"),
		UB2_GNEISS(			"gneiss",	 	2, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.gneiss",		"undergroundbiomes:metamorphic_stone:0"),
		UB2_GREEN_SCHIST(	"green_schist",	3, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.greenschist","undergroundbiomes:metamorphic_stone:5"),
		UB2_MARBLE(			"marble",		4, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.marble",		"undergroundbiomes:metamorphic_stone:2"),
		UB2_MIGMATITE(		"migmatite", 	5, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.migmatite",	"undergroundbiomes:metamorphic_stone:7"),
		UB2_QUARTZITE(		"quartzite", 	6, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.quartzite",	"undergroundbiomes:metamorphic_stone:3"),
		UB2_SOAPSTONE(		"soapstone", 	7, UNDERGROUNDBIOMES2, "undergroundbiomes", "metamorphic_stone.soapstone",	"undergroundbiomes:metamorphic_stone:6"),
		
		UB3_CHALK(			"chalk",		0, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.chalk", 		"undergroundbiomes:sedimentary_stone:1"),
		UB3_CHERT(			"chert",		1, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.chert",		"undergroundbiomes:sedimentary_stone:7"),
		UB3_DOLOMITE(		"dolomite",		2, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.dolomite", 	"undergroundbiomes:sedimentary_stone:5"),
		UB3_GREYWACKE(		"greywacke",	3, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.greywacke", 	"undergroundbiomes:sedimentary_stone:6"),
		UB3_LIGNITE(		"lignite",		4, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.lignite", 	"undergroundbiomes:sedimentary_stone:4"),
		UB3_LIMESTONE(		"limestone",	5, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.limestone", 	"undergroundbiomes:sedimentary_stone:0"),
		UB3_SHALE(			"shale",		6, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.shale", 		"undergroundbiomes:sedimentary_stone:2"),
		UB3_SILTSTONE(		"siltstone",	7, UNDERGROUNDBIOMES3, "undergroundbiomes", "sedimentary_stone.siltstone", 	"undergroundbiomes:sedimentary_stone:3");

		private String name, dependency, languageKey, backgroundBlock, forceTextureLocation;
		private int meta;
		private boolean isDependencyMet, hasForcibleTexture;

		private State(String name, int meta, List<State> dependency, String dependencyName, String languageKey, String backgroundBlock)
		{
			this.isDependencyMet = dependencyName == "base" ? true : Loader.isModLoaded(dependencyName);
			dependency.add(this);
			
			this.name = name;
			this.meta = meta;
			this.dependency = dependencyName;
			this.languageKey = languageKey;
			this.backgroundBlock = backgroundBlock;
		}
		
		//I expected this to be necessary for Underground Biomes. It wasn't. I may remove it ~soon.
		private State(String name, int meta, List<State> dependency, String dependencyName, String languageKey, String backgroundBlock, String forceTextureLocation)
		{
			this(name, meta, dependency, dependencyName, languageKey, backgroundBlock);
			
			this.forceTextureLocation = forceTextureLocation;
			this.hasForcibleTexture = true;
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
		}
		
		public static Collection<State> getStatesForModName(String name)
		{
			return MOD_MAP.get(name);
		}
		
		public static List<State> getStateListForModName(String name)
		{
			return MOD_MAP.get(name);
		}
		
		@Override
		public String getName() 
		{
			return name;
		}
		
		public int getMeta()
		{
			return meta;
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
		
		public boolean hasForcibleTexture()
		{
			return hasForcibleTexture;
		}
		
		public String getForceTextureLocation()
		{
			return forceTextureLocation;
		}
		
		public String getUnlocalizedName() 
		{
			return name;
		}
		
		public String getLocalizedName()
		{
			return ShortTrans.unformatted("tile." + this.languageKey + ".name");
		}
		
		@Override
		public String toString() 
		{
			return name;
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