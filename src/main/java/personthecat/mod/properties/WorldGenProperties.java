package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class WorldGenProperties
{
	private boolean hasBiomeMatcher, hasBiomeBlacklist;
	private int blockCount, chance, minHeight, maxHeight;
	private String name;
	private List<String> biomeList;
	private List<String> biomeBlacklist;
	
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
		//Experimentally offloading some more of the calculations that happen on world generation to HOPEFULLY increase performance. See WorldGenCustomOres.java.
		//This is supposed to create fewer loops and thereby fewer things to test for for each chunk generated. Might not matter all that much, though.
		for (Type type : biomeType)
		{
			for (Biome biome : BiomeDictionary.getBiomes(type))
			{				
				biomeLookup.add(biome.getRegistryName().toString());
			}
		}
		
		this.name = name;
		this.blockCount = blockCount;
		this.chance = chance;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		this.biomeList = biomeLookup;
		
		
		if (biomeLookup.size() > 0) this.hasBiomeMatcher = true;
	}
	
	private WorldGenProperties() {}
	
	public static Collection<WorldGenProperties> getWorldGenPropertyRegistry()
	{
		return WORLDGEN_PROPERTY_MAP.values();
	}
	
	public static WorldGenProperties getDenseProperties(WorldGenProperties property)
	{		
		return new WorldGenProperties("dense_" + property.getName(), 3, 1400, property.getMinHeight(), property.getMaxHeight(), new ArrayList<Type>(), property.getBiomeList());
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setBlockCount(int count)
	{
		this.blockCount = count;
	}
	
	public int getBlockCount()
	{
		return blockCount;
	}
	
	public void setChance(int chance)
	{
		this.chance = chance;
	}
	
	public int getChance()
	{
		return chance;
	}
	
	public void setMinHeight(int height)
	{
		this.minHeight = height;
	}
	
	public int getMinHeight()
	{
		return minHeight;
	}
	
	public void setMaxHeight(int height)
	{
		this.maxHeight = height;
	}
	
	public int getMaxHeight()
	{
		return maxHeight;
	}
	
	public boolean getHasBiomeMatcher()
	{
		return this.hasBiomeMatcher;
	}
	
	public void setBiomeList(List<String> biomes)
	{
		this.biomeList = biomes;
	}
	
	public List<String> getBiomeList()
	{
		return this.biomeList;
	}
	
	public void setUseBiomeBlacklist()
	{
		this.biomeBlacklist = this.biomeList;
		
		this.biomeList.clear();
		
		this.hasBiomeBlacklist = true;
	}
	
	public boolean getHasBiomeBlacklist()
	{
		return this.hasBiomeBlacklist;
	}
	
	public List<String> getBiomeBlacklist()
	{
		return biomeBlacklist;
	}
	
	public void register()
	{
		WORLDGEN_PROPERTY_MAP.put(name, this);
	}
}
