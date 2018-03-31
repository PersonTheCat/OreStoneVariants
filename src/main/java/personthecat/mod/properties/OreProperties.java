package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import personthecat.mod.util.NameReader;

public class OreProperties
{	
	private boolean isDropBlock, hasBuiltInTextures;
	private float hardness,lightLevel;
	private int level, leastDrop, mostDrop, leastXp, mostXp, dropMeta, dropAltMeta;
	private ResourceLocation dropLookup, dropAltLookup;
	private String name, languageKey, backgroundMatcher, originalTexture;
	
	public static final List<OreProperties> ORE_PROPERTY_REGISTRY = new ArrayList<OreProperties>();
	private static final Map<String, OreProperties> ORE_PROPERTY_MAP = new HashMap<String, OreProperties>();
	
	public OreProperties(String name, String languageKey, float hardness, int level, boolean isDropBlock, String drop, int dropMeta, String dropAlt, int dropAltMeta, int leastDrop, int mostDrop, int leastXp, int mostXp)
	{			
		this.name = name;
		this.languageKey = languageKey;
		this.hardness = hardness;
		this.level = level;
		this.isDropBlock = isDropBlock;
		this.dropLookup = new ResourceLocation(drop);
		this.dropMeta = dropMeta;
		this.dropAltLookup = new ResourceLocation(dropAlt);
		this.dropAltMeta = dropAltMeta;
		this.leastDrop = leastDrop;
		this.mostDrop = mostDrop;
		this.leastXp = leastXp;
		this.mostXp = mostXp;	
		this.lightLevel = 0F;

		ORE_PROPERTY_REGISTRY.add(this);
		ORE_PROPERTY_MAP.put(name, this);
	}
	
	//Capitalizes name and removes anything after "_ore", if applicable.
	public static OreProperties propertiesOf(String name)
	{
		if (name.contains("lit_")) return ORE_PROPERTY_MAP.get("lit_redstone_ore");
		
		name = NameReader.getOreIgnoreDense(name);

		return ORE_PROPERTY_MAP.get(name);
	}
		
	public WorldGenProperties getWorldGenProperties()
	{
		return WorldGenProperties.WORLDGEN_PROPERTY_MAP.get(name) != null ? WorldGenProperties.WORLDGEN_PROPERTY_MAP.get(name) : WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("gold_ore");
	}
	
	public RecipeProperties getRecipeProperties()
	{
		return RecipeProperties.RECIPE_PROPERTY_MAP.get(name) != null ? RecipeProperties.RECIPE_PROPERTY_MAP.get(name) : RecipeProperties.DO_NOTHING;
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
		this.originalTexture = location;
	}
	
	public String getOriginalTexture()
	{
		return originalTexture;
	}
	
	public void setHasBuiltInTextures(Boolean hasBuiltInTextures)
	{
		this.hasBuiltInTextures = hasBuiltInTextures;
	}
	
	public boolean getHasBuiltInTextures()
	{
		return hasBuiltInTextures;
	}
	
	public String getLocalizedName()
	{
		String text = I18n.translateToLocal("tile." + this.languageKey + ".name").replaceAll("  ", "");
		
		if (text.contains("tile.") || text.contains(".name"))
		{
			text = I18n.translateToLocal(this.languageKey).replaceAll("  ", "");
		}
		
		return text;
	}
	
	public float getHardness()
	{
		return hardness;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public boolean isDropBlock()
	{
		return isDropBlock;
	}
	
	public ResourceLocation getDropLookup()
	{
		return dropLookup;
	}
	
	public int getDropMeta()
	{
		return dropMeta;
	}
	
	public ResourceLocation getDropAltLookup()
	{
		return dropAltLookup;
	}
	
	public int getDropAltMeta()
	{
		return dropAltMeta;
	}
	
	public int getLeastDrop()
	{
		return leastDrop;
	}
	
	public int getMostDrop()
	{
		return mostDrop;
	}
	
	public int getLeastXp()
	{
		return leastXp;
	}
	
	public int getMostXp()
	{
		return mostXp;
	}
	
	public void setLightLevel(float level)
	{
		this.lightLevel = level;
	}
	
	public float getLightLevel()
	{
		return lightLevel;
	}
}
