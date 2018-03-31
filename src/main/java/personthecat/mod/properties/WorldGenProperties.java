package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.BiomeDictionary.Type;

public class WorldGenProperties
{
	private boolean hasBiomeNameMatcher, hasBiomeTypeMatcher;
	private int blockCount, chance, minHeight, maxHeight;
	private String name;
	private List<Type> biomeTypeList;
	private List<String> biomeNameList;
	
	private static final List<WorldGenProperties> WORLDGEN_PROPERTY_REGISTRY = new ArrayList<WorldGenProperties>();
	public static final Map<String, WorldGenProperties> WORLDGEN_PROPERTY_MAP = new HashMap<String, WorldGenProperties>();
	
	public WorldGenProperties(String name, int blockCount, int chance, int minHeight, int maxHeight, List<Type> biomeType, List<String> biomeLookup)
	{
		setAll(name, blockCount, chance, minHeight, maxHeight, biomeType, biomeLookup);
	}
	
	public WorldGenProperties(String name, int blockCount, int chance, int minHeight, int maxHeight, Type[] biomeType, String[] biomeLookup)
	{
		List<Type> typeList = new ArrayList<Type>();
		for (Type type : biomeType)
		{
			typeList.add(type);
		}
		
		List<String> nameList = new ArrayList<String>();
		for (String biome : biomeLookup)
		{
			nameList.add(biome);
		}
		
		setAll(name, blockCount, chance, minHeight, maxHeight, typeList, nameList);
	}
	
	private void setAll(String name, int blockCount, int chance, int minHeight, int maxHeight, List<Type> biomeType, List<String> biomeLookup)
	{
		this.name = name;
		this.blockCount = blockCount;
		this.chance = chance;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		this.biomeTypeList = biomeType;
		this.biomeNameList = biomeLookup;
		this.hasBiomeTypeMatcher = biomeType.size() > 0 ? true : false;
		this.hasBiomeNameMatcher = biomeLookup.size() > 0 ? true : false;
		
		WORLDGEN_PROPERTY_REGISTRY.add(this);
		WORLDGEN_PROPERTY_MAP.put(name, this);
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getBlockCount()
	{
		return blockCount;
	}
	
	public int getChance()
	{
		return chance;
	}
	
	public int getMinHeight()
	{
		return minHeight;
	}
	
	public int getMaxHeight()
	{
		return maxHeight;
	}
	
	public boolean getHasBiomeNameMatcher()
	{
		return hasBiomeNameMatcher;
	}
	
	public List<String> getBiomeNameList()
	{
		return biomeNameList;
	}
	
	public boolean getHasBiomeTypeMatcher()
	{
		return hasBiomeTypeMatcher;
	}
	
	public List<Type> getBiomeTypeList()
	{
		return biomeTypeList;
	}
}
