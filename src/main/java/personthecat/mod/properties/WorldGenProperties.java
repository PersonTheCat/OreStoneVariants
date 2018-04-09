package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class WorldGenProperties
{
	private int blockCount = 9, chance = 2, minHeight = 0, maxHeight = 32;
	private String name;
	private List<String> biomeList = new ArrayList<>(), biomeBlacklist = new ArrayList<>();
	private List<Integer> dimensionList = new ArrayList<>(), dimensionBlacklist = new ArrayList<>();
	private WorldGenProperties[] additionalProperties;
	
	public static final Map<String, WorldGenProperties> WORLDGEN_PROPERTY_MAP = new HashMap<String, WorldGenProperties>();
	
	public WorldGenProperties(String name, int blockCount, int chance, int minHeight, int maxHeight, List<Type> biomeType, List<String> biomeLookup)
	{		
		setAll(name, blockCount, chance, minHeight, maxHeight, biomeType, biomeLookup);
	}
	
	public WorldGenProperties(String name, int blockCount, int chance, int minHeight, int maxHeight, Type[] biomeType, String[] biomeLookup)
	{		
		List<Type> typeList = new ArrayList<>();
		for (Type type : biomeType)
		{
			typeList.add(type);
		}
		
		List<String> nameList = new ArrayList<>();
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
		return biomeList.size() > 0;
	}
	
	public void setBiomeList(List<String> biomes)
	{
		this.biomeList = biomes;
	}
	
	public List<String> getBiomeList()
	{
		return biomeList;
	}
	
	public void setUseBiomeBlacklist()
	{
		this.biomeBlacklist = biomeList;
		
		this.biomeList.clear();
	}
	
	public boolean getHasBiomeBlacklist()
	{		
		return !biomeBlacklist.isEmpty();
	}
	
	public List<String> getBiomeBlacklist()
	{
		return biomeBlacklist;
	}
	
	public boolean getHasDimensionMatcher()
	{
		return !dimensionList.isEmpty();
	}
	
	public void setDimensionList(List<Integer> dimensions)
	{
		this.dimensionList = dimensions;
	}
	
	public List<Integer> getDimensionList()
	{
		return dimensionList;
	}
	
	public void setUseDimensionsBlacklist()
	{
		this.dimensionBlacklist = dimensionList;
		
		this.dimensionList.clear();
	}
	
	public boolean getHasDimensionBlacklist()
	{
		return !dimensionBlacklist.isEmpty();
	}
	
	public List<Integer> getDimensionBlacklist()
	{
		return dimensionBlacklist;
	}
	
	public void setAdditionalProperties(WorldGenProperties... properties)
	{		
		this.additionalProperties = properties;
	}
	
	public boolean getHasAdditionalProperties()
	{
		return additionalProperties.length > 0;
	}
	
	public WorldGenProperties[] getAdditionalProperties()
	{
		return additionalProperties;
	}
	
	public void register()
	{
		WORLDGEN_PROPERTY_MAP.put(name, this);
	}
	
	public static class FromJson
	{
		private WorldGenProperties properties;
		private JsonObject parent;
		private Map<JsonObject, WorldGenProperties> jsons = new HashMap<>();
		
		public FromJson(JsonObject json, String filename)
		{			
			if (WORLDGEN_PROPERTY_MAP.get(filename) != null)
			{
				this.properties = WORLDGEN_PROPERTY_MAP.get(filename);
			}
			
			else this.properties = new WorldGenProperties();

			jsons.put(json, properties);
			
			this.parent = json;
			
			addAdditionalObjects();
			
			properties.setName(filename);
			setPrimaryValues();
			setMatchers();
			addAdditionalPropertiesToParent();
			
			properties.register();
		}
		
		public WorldGenProperties getProperties()
		{
			return properties;
		}
		
		private void addAdditionalObjects()
		{
			if (parent.get("addtionalPropertyKeys") != null)
			{
				for (JsonElement element : parent.get("additionalDropKeys").getAsJsonArray())
				{
					if (parent.get(element.getAsString()) != null)
					{
						jsons.put(parent.get(element.getAsString()).getAsJsonObject(), new WorldGenProperties());
					}
				}
			}
		}
		
		private void setPrimaryValues()
		{
			for (JsonObject obj : jsons.keySet())
			{
				WorldGenProperties property = jsons.get(obj);
				
				if (obj.get("blockCount") != null) property.setBlockCount(obj.get("blockCount").getAsInt());
				
				if (obj.get("chance") != null) property.setChance(obj.get("chance").getAsInt());
				
				if (obj.get("minHeight") != null) property.setMinHeight(obj.get("minHeight").getAsInt());
				
				if (obj.get("minHeight") != null) property.setMinHeight(obj.get("minHeight").getAsInt());
			}
		}
		
		private void setMatchers()
		{
			for (JsonObject obj : jsons.keySet())
			{
				WorldGenProperties property = jsons.get(obj);
				
				List<String> biomeNameList = new ArrayList<>();
				
				if (obj.get("biomeNameList") != null)
				{
					for (JsonElement element : obj.get("biomeNameList").getAsJsonArray())
					{
						biomeNameList.add(element.getAsString());
					}
				}
				
				if (obj.get("biomeTypeList") != null)
				{
					for (JsonElement element : obj.get("biomeTypeList").getAsJsonArray())
					{
						for (Biome biome : BiomeDictionary.getBiomes(Type.getType(element.getAsString())))
						{				
							biomeNameList.add(biome.getRegistryName().toString());
						}
					}
				}
				
				property.setBiomeList(biomeNameList);
				
				if (obj.get("biomesAreBlacklist") != null)
				{
					if (obj.get("biomesAreBlacklist").getAsBoolean()) property.setUseBiomeBlacklist();
				}
				
				List<Integer> dimensionList = new ArrayList<>();

				if (obj.get("dimensionList") != null)
				{
					for (JsonElement element : obj.get("dimensionList").getAsJsonArray())
					{
						dimensionList.add(element.getAsInt());
					}
				}
				
				property.setDimensionList(dimensionList);
				
				if (obj.get("dimensionsAreBlacklist") != null)
				{
					if (obj.get("dimensionsAreBlacklist").getAsBoolean()) property.setUseDimensionsBlacklist();
				}
			}
		}
		
		private void addAdditionalPropertiesToParent()
		{
			List<WorldGenProperties> additionalProperties = new ArrayList<>();
			
			for (WorldGenProperties property : jsons.values())
			{
				if (!property.equals(properties)) additionalProperties.add(property);
			}
			
			properties.setAdditionalProperties(additionalProperties.toArray(new WorldGenProperties[additionalProperties.size()]));
		}
	}
}
