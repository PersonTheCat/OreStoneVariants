package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.advancements.DynamicTrigger;
import personthecat.mod.config.JsonReader;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.ShortTrans;
import scala.actors.threadpool.Arrays;

public class OreProperties
{	
	//Some default values.
	private boolean hasBuiltInTextures = true;
	private DropProperties[] dropProperties = new DropProperties[] {new DropProperties()};
	private float hardness = 3.0F, lightLevel = 0F;
	private int level = 2;
	private String name, languageKey, backgroundMatcher = "assets/minecraft/textures/blocks/stone.png", originalTexture;

	private static final Map<String, OreProperties> ORE_PROPERTY_MAP = new HashMap<String, OreProperties>();
	public static final List<String> CUSTOM_PROPERTY_NAMES = new ArrayList<>();
	
	public OreProperties(String name, String languageKey, float hardness, int level, DropProperties... drops)
	{			
		if (!languageKey.endsWith(".name")) languageKey = languageKey + ".name";
		if (!languageKey.startsWith("tile.")) languageKey = "tile." + languageKey;
		
		this.name = name;
		this.languageKey = languageKey;
		this.hardness = hardness;
		this.level = level;
		this.dropProperties = drops;

		register();
	}
	
	private OreProperties() {}
	
	public static Collection<OreProperties> getOrePropertyRegistry()
	{
		return ORE_PROPERTY_MAP.values();
	}

	public static OreProperties propertiesOf(String name)
	{
		if (name.contains("lit_")) return ORE_PROPERTY_MAP.get("lit_redstone_ore");

		return ORE_PROPERTY_MAP.get(NameReader.getOreIgnoreDense(name));
	}
		
	public WorldGenProperties getWorldGenProperties()
	{
		return WorldGenProperties.WORLDGEN_PROPERTY_MAP.get(name) != null ? WorldGenProperties.WORLDGEN_PROPERTY_MAP.get(name) : WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("gold_ore");
	}
	
	public RecipeProperties getRecipeProperties()
	{
		return RecipeProperties.RECIPE_PROPERTY_MAP.get(name) != null ? RecipeProperties.RECIPE_PROPERTY_MAP.get(name) : RecipeProperties.DO_NOTHING;
	}
	
	public void setDropProperties(DropProperties... properties)
	{
		this.dropProperties = properties;
	}
	
	public void addDropProperties(DropProperties... properties)
	{
		this.dropProperties = (DropProperties[]) ArrayUtils.addAll(dropProperties, properties);
	}
	
	public DropProperties[] getDropProperties()
	{
		return dropProperties;
	}
	
	public DropProperties[] getDropPropertiesByChance(World worldIn, EntityPlayer playerIn)
	{
		if (dropProperties.length == 1) return dropProperties;
		
		List<DropProperties> drops = new ArrayList<>();
		
		for (DropProperties props : dropProperties)
		{
			if (props.requiresAdvancement() && playerIn instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP) playerIn;
				
				if (!DynamicTrigger.playerHasAdvancement(DynamicTrigger.getAdvancement(props.getRequiredAdvancement(), worldIn), player)) continue;
			}
			
			if ((props.getChance() / 100.0) >= worldIn.rand.nextDouble()) drops.add(props);
		}
		
		return drops.toArray(new DropProperties[] {});
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setBackgroundMatcher(String location)
	{
		this.backgroundMatcher = location;
	}
	
	public String getBackgroundMatcher()
	{
		return backgroundMatcher;
	}
	
	public void setOriginalTexture(String location)
	{
		this.hasBuiltInTextures = false;
		
		this.originalTexture = location;
	}
	
	public String getOriginalTexture()
	{
		return originalTexture;
	}

	public boolean getHasBuiltInTextures()
	{
		return hasBuiltInTextures;
	}
	
	public void setLanguageKey(String key)
	{
		this.languageKey = key;
	}
	
	public String getLanguageKey()
	{			
		return languageKey;
	}
	
	public String getLocalizedName()
	{		
		if (!ShortTrans.canTranslate(languageKey.replaceAll("  ", "")))
		{
			return ShortTrans.unformatted("tile." + languageKey.replaceAll("  ", "") + ".name");
		}
		
		return ShortTrans.unformatted(getLanguageKey()).replaceAll("  ", "");
	}
	
	public void setHardness(float hardness)
	{
		this.hardness = hardness;
	}
	
	public float getHardness()
	{
		return hardness;
	}
	
	public void setLevel(int level)
	{
		this.level = level;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void setLightLevel(float level)
	{
		this.lightLevel = level;
	}
	
	public float getLightLevel()
	{
		return lightLevel;
	}
	
	public void setPropertyGroup(PropertyGroup group)
	{
		PropertyGroup.unassignProperty(this);
		
		group.addProperties(this);
	}
	
	private void register()
	{
		ORE_PROPERTY_MAP.put(name, this);
	}
	
	public static class DropProperties
	{
		//More default values.
		private double chance = 100.0;
		private int dropMeta = 0, dropAltMeta = 0;
		private int[] dropRange = new int[] {1, 1}, xpRange = new int[] {0, 0};
		private ResourceLocation dropLookup = new ResourceLocation(""), dropAltLookup = new ResourceLocation(""), requiredAdvancement = new ResourceLocation("");
		
		public DropProperties(boolean isDropBlock, String drop, String dropAlt, int[] dropRange, int[] xpRange)
		{
			setFullDropLookup(drop);
			setFullDropAltLookup(dropAlt);
			
			Arrays.sort(dropRange); Arrays.sort(xpRange);
			
			this.dropRange = dropRange;
			this.xpRange = xpRange;
		}
		
		private DropProperties() {}
		
		public boolean isDropBlock()
		{
			return ForgeRegistries.BLOCKS.containsKey(dropLookup);
		}
		
		public void setFullDropLookup(String fullDrop)
		{
			String[] dropComponents = fullDrop.split(":");
			
			String dropLastComponent = dropComponents[dropComponents.length - 1];
			
			if (StringUtils.isNumeric(dropLastComponent))
			{
				fullDrop = fullDrop.replaceAll(":" + dropLastComponent, "");
				
				this.dropMeta = Integer.parseInt(dropLastComponent);
			}
			
			this.dropLookup = new ResourceLocation(fullDrop);
		}
		
		public ResourceLocation getDropLookup()
		{
			return dropLookup;
		}
		
		public void setDropMeta(int meta)
		{
			this.dropMeta = meta;
		}
		
		public int getDropMeta()
		{
			return dropMeta;
		}
		
		public void setFullDropAltLookup(String fullDrop)
		{
			String[] dropComponents = fullDrop.split(":");
			
			String dropLastComponent = dropComponents[dropComponents.length - 1];
			
			if (StringUtils.isNumeric(dropLastComponent))
			{
				fullDrop = fullDrop.replaceAll(":" + dropLastComponent, "");
				
				this.dropAltMeta = Integer.parseInt(dropLastComponent);
			}
			
			this.dropAltLookup = new ResourceLocation(fullDrop);
		}
		
		public ResourceLocation getDropAltLookup()
		{
			return dropAltLookup;
		}
		
		public void setDropAltMeta(int meta)
		{
			this.dropAltMeta = meta;
		}
		
		public int getDropAltMeta()
		{
			return dropAltMeta;
		}
		
		public void setDropRange(int[] range)
		{
			Arrays.sort(range);
			
			this.dropRange = range;
		}
		
		public void setLeastDrop(int leastDrop)
		{
			this.dropRange[0] = leastDrop;
		}
		
		public int getLeastDrop()
		{
			return dropRange[0];
		}
		
		public void setMostDrop(int mostDrop)
		{
			this.dropRange[dropRange.length - 1] = mostDrop;
		}
		
		public int getMostDrop()
		{
			return dropRange[dropRange.length - 1];
		}
		
		public void setXpRange(int[] range)
		{
			Arrays.sort(range);
			
			this.xpRange = range;
		}
		
		public void setLeastXp(int leastXp)
		{
			this.xpRange[0] = leastXp;
		}
		
		public int getLeastXp()
		{
			return xpRange[0];
		}
		
		public void setMostXp(int mostXp)
		{
			this.xpRange[xpRange.length - 1] = mostXp;
		}
		
		public int getMostXp()
		{
			return xpRange[xpRange.length - 1];
		}
		
		public void setRequiredAdvancement(String location)
		{
			this.requiredAdvancement = new ResourceLocation(location);
		}
		
		public ResourceLocation getRequiredAdvancement()
		{
			return requiredAdvancement;
		}
		
		public boolean requiresAdvancement()
		{
			return !StringUtils.isEmpty(requiredAdvancement.toString());
		}
		
		public void setChance(double chance)
		{
			this.chance = chance;
		}
		
		public double getChance()
		{
			return chance;
		}
	}
	
	public static class FromJson
	{
		private OreProperties properties;
		private JsonObject parent;
		private Map<JsonObject, DropProperties> jsons = new HashMap<>();
		
		public FromJson(JsonObject json, String filename)
		{
			if (ORE_PROPERTY_MAP.get(filename) != null)
			{
				this.properties = ORE_PROPERTY_MAP.get(filename);
				
				jsons.put(json, properties.getDropProperties()[0]);
			}
			
			else
			{
				this.properties = new OreProperties();
				
				jsons.put(json, new DropProperties());
				
				CUSTOM_PROPERTY_NAMES.add(filename);
			}

			this.parent = json;
			
			addAdditionalDropObjects();
			
			properties.setName(filename);
			setPrimaryValues(json);
			setAllDrops();
			
			properties.register();
		}
		
		public OreProperties getProperties()
		{
			return properties;
		}
		
		private void addAdditionalDropObjects()
		{
			if (parent.get("additionalDropKeys") != null)
			{
				for (JsonElement element : parent.get("additionalDropKeys").getAsJsonArray())
				{
					if (parent.get(element.getAsString()) != null)
					{						
						jsons.put(parent.get(element.getAsString()).getAsJsonObject(), new DropProperties());
					}
				}
			}
		}
		
		public DropProperties[] getAdditionalDrops()
		{
			List<DropProperties> drops = new ArrayList<>();
			
			for (DropProperties drop : jsons.values())
			{
				if (!drop.equals(jsons.get(parent))) drops.add(drop);
			}
			
			return drops.toArray(new DropProperties[jsons.size() - 2]);
		}
		
		private void setPrimaryValues(JsonObject parent)
		{			
			if (parent.get("languageKey") != null) properties.setLanguageKey(parent.get("languageKey").getAsString());
			
			if (parent.get("hardness") != null) properties.setHardness(parent.get("hardness").getAsFloat());
			
			if (parent.get("level") != null) properties.setLevel(parent.get("level").getAsInt());
			
			if (parent.get("lightLevel") != null) properties.setLightLevel(parent.get("lightLevel").getAsFloat());
			
			if (parent.get("backgroundMatcher") != null) properties.setBackgroundMatcher(parent.get("backgroundMatcher").getAsString());
			
			if (parent.get("originalTexture") != null) properties.setOriginalTexture(parent.get("originalTexture").getAsString());
			
			if (parent.get("createOverworldVariants") != null && parent.get("createOverworldVariants").getAsBoolean()) properties.setPropertyGroup(PropertyGroup.CUSTOM_PROPERTY_GROUP);
			
			if (parent.get("addToDefaultCustomPropertyGroup") != null && parent.get("addToDefaultCustomPropertyGroup").getAsBoolean()) properties.setPropertyGroup(PropertyGroup.CUSTOM_PROPERTY_GROUP);
			
			if (parent.get("addToCustomPropertyGroup") != null)
			{
				PropertyGroup group = PropertyGroup.locateOrCreateGroup(parent.get("addToCustomPropertyGroup").getAsString());
				
				properties.setPropertyGroup(group);
			}
		}
		
		private void setAllDrops()
		{			
			List<DropProperties> dropPropsList = new ArrayList<>();
			
			for (JsonObject obj : jsons.keySet())
			{
				DropProperties dropProps = jsons.get(obj);
				
				if (getArray(obj, "Drop") != null) dropProps.setDropRange(getArray(obj, "Drop"));
				
				if (getArray(obj, "Xp") != null) dropProps.setXpRange(getArray(obj, "Xp"));
				
				if (obj.get("dropLookup") != null) dropProps.setFullDropLookup(obj.get("dropLookup").getAsString());
				
				if (obj.get("dropAltLookup") != null) dropProps.setFullDropAltLookup(obj.get("dropAltLookup").getAsString());
				
				if (obj.get("dropMeta") != null) dropProps.setDropMeta(obj.get("dropMeta").getAsInt());
				
				if (obj.get("dropAltMeta") != null) dropProps.setDropAltMeta(obj.get("dropAltMeta").getAsInt());
				
				if (obj.get("chance") != null) dropProps.setChance(obj.get("chance").getAsDouble());

				dropPropsList.add(dropProps);
			}
			
			properties.setDropProperties(dropPropsList.toArray(new DropProperties[] {}));
		}
		
		private static int[] getArray(JsonObject obj, String partialKey)
		{
			return JsonReader.getArray(obj, partialKey, "least", "most");
		}
	}
}