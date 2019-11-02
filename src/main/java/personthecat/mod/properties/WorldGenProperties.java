package personthecat.mod.properties;

import static personthecat.mod.Main.logger;

import java.util.ArrayList;
import java.util.Arrays;
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

public class WorldGenProperties
{
    private double denseVariantRatio = 1.0;
    private int blockCount = 9, frequency = 2;
    private int[] heightRange = new int[] {0, 32};
    private List<String> biomeList = new ArrayList<>(), biomeBlacklist = new ArrayList<>();
    private List<Integer> dimensionList = new ArrayList<>(), dimensionBlacklist = new ArrayList<>();
    private String name;
    private WorldGenProperties masterProperties;
    private WorldGenProperties[] additionalProperties;

    private int uniqueId;

    public static final Map<String, WorldGenProperties> WORLDGEN_PROPERTY_MAP = new HashMap<>();

    public WorldGenProperties(String name, int blockCount, int frequency, int minHeight, int maxHeight, Type[] biomeType, String[] biomeLookup)
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

        setName(name);
        setBlockCount(blockCount);
        setFrequency(frequency);
        setHeightRange(new int[] {minHeight, maxHeight});
        setBiomeList(nameList);
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

    public OreProperties getOreProperties()
    {
        return OreProperties.propertiesOf(getName());
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Always returns the name of the original property.
     */
    public String getName()
    {
        if (hasMasterProperties()) return getMasterProperties().name;

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

    public void setFrequency(int frequency)
    {
        this.frequency = frequency;
    }

    /**
     * Small clusters become more frequent; Large cluster become larger.
     */
    public static void enableLargeClusterMode()
    {
        for (WorldGenProperties genProp : getWorldGenPropertyRegistry())
        {
            if (genProp.blockCount < 4) genProp.frequency *= 20;

            else
            {
                if (genProp.frequency > 1)
                {
                    genProp.frequency /= 2;

                    genProp.blockCount = 45;
                }
                else genProp.blockCount = 25;
            }
        }
    }

    public int getFrequency()
    {
        return frequency;
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

    public void setDenseVariantRatio(double ratio)
    {
        this.denseVariantRatio = ratio;
    }

    public double getDenseVariantRatio()
    {
        return denseVariantRatio;
    }

    public boolean hasBiomeMatcher()
    {
        return !biomeList.isEmpty();
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

    public boolean hasBiomeBlacklist()
    {
        return !biomeBlacklist.isEmpty();
    }

    public List<String> getBiomeBlacklist()
    {
        return biomeBlacklist;
    }

    public boolean hasDimensionMatcher()
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

    public boolean hasDimensionBlacklist()
    {
        return !dimensionBlacklist.isEmpty();
    }

    public List<Integer> getDimensionBlacklist()
    {
        return dimensionBlacklist;
    }

    /**
     * Compatibility method.
     */
    public int[] getDimensionListAsBlackList()
    {
        if (hasDimensionBlacklist()) return dimensionBlacklist.stream().mapToInt(i->i).toArray();

        if (dimensionList.isEmpty()) return new int[0];

        //Only doing -32 to 32. Kind of inefficient to do this anyway.
        int[] inverted = new int[65 - dimensionList.size()];

        int index = 0;

        for (int i = -32; i < 33; i++)
        {
            if (!dimensionList.contains(i))
            {
                inverted[index] = i;

                index++;
            }
        }

        return inverted;
    }

    public boolean isEmpty()
    {
        return (blockCount == 0) && (frequency == 0) && (getMaxHeight() == 0);
    }

    public boolean isValidProperty()
    {
        return !isEmpty() && getOreProperties().inUse();
    }

    public void setAdditionalProperties(WorldGenProperties... properties)
    {
        for (WorldGenProperties property : properties)
        {
            property.masterProperties = this;

            property.register();
        }

        this.additionalProperties = properties;
    }

    public boolean hasAdditionalProperties()
    {
        return !ArrayUtils.isEmpty(additionalProperties);
    }

    public boolean hasMasterProperties()
    {
        return masterProperties != null;
    }

    public WorldGenProperties getMasterProperties()
    {
        return masterProperties;
    }

    public WorldGenProperties[] getAdditionalProperties()
    {
        return additionalProperties;
    }

    /**
     * Generates a semi-unique integer based on the name of the parent properties.
     */
    private void generateUniqueId()
    {
        uniqueId = getNumberForName(getName());
    }

    public int getUniqueId()
    {
        if (uniqueId == 0)
        {
            logger.warn(
                "Error: World gen properties may not have been registered correctly. "
                + "Unable to retrieve a valid ID for this object. Please report this issue."
            );
        }

        return uniqueId;
    }

    /*
     * Could concatenate these numbers to produce a more unique
     * value of every string. It's easier for me to work with
     * smaller numbers, however.
     */
    private static int getNumberForName(String name)
    {
        int number = 0;

        for (Character c : name.toCharArray())
        {
            number += Character.getNumericValue(c);
        }

        return number;
    }

    public void register()
    {
        generateUniqueId();

        WORLDGEN_PROPERTY_MAP.put(name, this);
    }

    public static class FromJson
    {
        private final WorldGenProperties properties;
        private final JsonObject parent;
        private Map<JsonObject, WorldGenProperties> jsons = new HashMap<>();

        public FromJson(JsonObject json, String fileName)
        {
            fileName = fileName.replaceAll(".zip", "");

            if (WORLDGEN_PROPERTY_MAP.get(fileName) != null)
            {
                this.properties = WORLDGEN_PROPERTY_MAP.get(fileName);
            }
            else this.properties = new WorldGenProperties();

            jsons.put(json, properties);

            this.parent = json;

            addAdditionalObjects();

            properties.setName(fileName);
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

                if (obj.get("chance") != null) property.setFrequency(obj.get("chance").getAsInt());

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
