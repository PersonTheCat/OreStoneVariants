package personthecat.mod.util.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.Main;
import scala.actors.threadpool.Arrays;

public class BlockStateGenerator
{
	
	private static List<State> base = new ArrayList<State>();
	private static List<State> quark = new ArrayList<State>();
	
	//Yes, the same enum is used for all stone variants. Lazy? na.
	public enum State implements IStringSerializable
	{		
		STONE("stone", 0, base, "base", "stone.stone", "minecraft:stone"),
		ANDESITE("andesite", 1, base, "base", "stone.andesite", "minecraft:stone:5"),
		DIORITE("diorite", 2, base, "base", "stone.diorite", "minecraft:stone:3"),
		GRANITE("granite", 3, base, "base", "stone.granite", "minecraft:stone:1"),
		LIMESTONE("limestone", 0, quark, "quark", "quark:stone_limestone", "quark:limestone"),
		MARBLE("marble", 1, quark, "quark", "quark:stone_marble", "quark:marble");

		private String name, dependency, languageKey, backgroundBlock;
		private int meta;
		private boolean isDependencyMet;

		private State(String name, int meta, List<State> dependency, String dependencyName, String languageKey, String backgroundBlock)
		{
			dependency.add(this);
			this.isDependencyMet = dependencyName == "base" ? true : Loader.isModLoaded(dependencyName);
			
			this.name = name;
			this.meta = meta;
			this.dependency = dependencyName;
			this.languageKey = languageKey;
			this.backgroundBlock = backgroundBlock;
		}
		
		private static final Map<String, List<State>> MOD_MAP = new HashMap<String, List<State>>();
		
		static
		{
			MOD_MAP.put("base", base);
			MOD_MAP.put("quark", quark);
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
				state = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(splitBGString[0], splitBGString[1])).getStateFromMeta(Integer.parseInt(splitBGString[2]));
			else
				state = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(backgroundBlock)).getDefaultState();
			
			return state;
		}
		
		public ModelResourceLocation getBackgroundModelLocation()
		{
			BlockStateMapper stateMapper = new BlockStateMapper();
			IBlockState state = this.getBackgroundBlockState();
			Map<IBlockState, ModelResourceLocation> locationMapped = stateMapper.getVariants(state.getBlock());
			return locationMapped.get(state);			
		}
		
		public String getUnlocalizedName() 
		{
			return name;
		}
		
		public String getLocalizedName()
		{
			return I18n.translateToLocal("tile." + this.languageKey + ".name");
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