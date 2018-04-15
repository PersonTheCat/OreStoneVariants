package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import personthecat.mod.config.JsonReader;
import scala.actors.threadpool.Arrays;

public class WorldGenProperties
{
	private int blockCount = 9, chance = 2;
	private int[] heightRange = new int[] {0, 32};
	private List<String> biomeList = new ArrayList<>(), biomeBlacklist = new ArrayList<>();
	private List<Integer> dimensionList = new ArrayList<>(), dimensionBlacklist = new ArrayList<>();
	private String name;
	private WorldGenProperties[] additionalProperties;
	
	public static final Map<String, WorldGenProperties> WORLDGEN_PROPERTY_MAP = new HashMap<>();
	
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

		for (Type type : biomeType)
		{
			for (Biome biome : BiomeDictionary.getBiomes(type))
			{				
				nameList.add(biome.getRegistryName().toString());
			}
		}
		
		this.name = name;
		this.blockCount = blockCount;
		this.chance = chance;
		this.heightRange = new int[] {minHeight, maxHeight};
		this.biomeList = nameList;
	}
	
	private WorldGenProperties() {}
	
	public static Collection<WorldGenProperties> getWorldGenPropertyRegistry()
	{
		return WORLDGEN_PROPERTY_MAP.values();
	}
	
	public static WorldGenProperties getDenseProperties(WorldGenProperties property)
	{		
		return new WorldGenProperties("dense_" + property.getName(), 3, 1400, property.getMinHeight(), property.getMaxHeight(), new Type[0], property.getBiomeList().toArray(new String[property.getBiomeList().size()]));
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
	
	public void setHeightRange(int[] range)
	{
		Arrays.sort(range);
		
		this.heightRange = range;
	}
	
	public void setMinHeight(int height)
	{
		this.heightRange[0] = height;
	}
	
	public int getMinHeight()
	{
		return heightRange[0];
	}
	
	public void setMaxHeight(int height)
	{
		this.heightRange[heightRange.length - 1] = height;
	}
	
	public int getMaxHeight()
	{
		return heightRange[heightRange.length - 1];
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
		return !ArrayUtils.isEmpty(additionalProperties);
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
			if (parent.get("additionalPropertyKeys") != null)
			{
				for (JsonElement element : parent.get("additionalPropertyKeys").getAsJsonArray())
				{
					if (parent.get(element.getAsString()) != null)
					{
						System.out.println("answer: additionalPropertyKey detected: " + element.getAsString());
						
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
				
				if (getArray(obj, "Height") != null) property.setHeightRange(getArray(obj, "Height"));
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
		
		private int[] getArray(JsonObject obj, String partialKey)
		{			
			return JsonReader.getArray(obj, partialKey, "min", "max");
		}
	}
}
