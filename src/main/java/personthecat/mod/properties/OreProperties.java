package personthecat.mod.properties;

import static personthecat.mod.Main.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.Main;
import personthecat.mod.advancements.AdvancementMap;
import personthecat.mod.config.Cfg;
import personthecat.mod.config.JsonReader;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.Reference;
import personthecat.mod.util.ShortTrans;

public class OreProperties
{	
	//Some default values.
	private boolean hasBuiltInTextures = true, blendedTexture = false, inUse = false;
	private DropProperties[] dropProperties = new DropProperties[] {new DropProperties()};
	private float hardness = 3.0F, lightLevel = 0F;
	private int level = 2;
	private String name, modName = "minecraft", languageKey, backgroundMatcher = "assets/minecraft/textures/blocks/stone.png", originalTexture;
	private TextureAtlasSprite texture, denseTexture;

	private static final Map<String, OreProperties> ORE_PROPERTY_MAP = new HashMap<>();
	
	public OreProperties(String name, String languageKey, float hardness, int level, DropProperties... drops)
	{
		this.name = name;
		this.languageKey = languageKey;
		this.hardness = hardness;
		this.level = level;
		this.dropProperties = drops;

		register();
	}
	
	private OreProperties() {}
	
	/**
	 * Variants can now be created with these properties;
	 */
	public void setInUse()
	{
		this.inUse = true;
	}
	
	public boolean inUse()
	{
		return inUse;
	}
	
	public void setModName(String name)
	{
		this.modName = name;
	}
	
	public String getModName()
	{
		return modName;
	}
	
	public static Collection<OreProperties> getOrePropertyRegistry()
	{
		return ORE_PROPERTY_MAP.values();
	}

	public static OreProperties propertiesOf(String name)
	{
		if (name.contains("lit_redstone_ore")) return ORE_PROPERTY_MAP.get("lit_redstone_ore");
		
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
			
				if (!AdvancementMap.playerHasAdvancement(AdvancementMap.getAdvancement(props.getRequiredAdvancement(), worldIn), player)) continue;
			}
			
			if ((props.getChance() / 100.0) >= worldIn.rand.nextDouble()) { drops.add(props);}
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
	
	/**
	 * A NullPointerException would otherwise be thrown later. Explains why.
	 */
	public void testTextures()
	{
		Main.proxy.testTextureLocation(originalTexture, getName());
		Main.proxy.testTextureLocation(backgroundMatcher, getName());
	}
	
	public boolean getHasBuiltInTextures()
	{
		return hasBuiltInTextures;
	}
	
	public void setUseBlendedTextures()
	{
		this.blendedTexture = true;
	}
	
	public boolean getUseBlendedTexture()
	{
		return blendedTexture;
	}
	
	/**
	 * Returns the normal or blended overlay path, without ".png"
	 * Use FileTools to derive the dense path.
	 */
	public String getOverlayPath()
	{
		return "assets/" + Reference.MODID + "/textures/blocks/" + getFileName();
	}
	
	public ResourceLocation getOverlayResourceLocation()
	{
		return new ResourceLocation(Reference.MODID, "blocks/" + getFileName());
	}
	
	private String getFileName()
	{
		String fileName = getModName().replaceAll("minecraft", "vanilla") + "/" + name + "_overlay";
		
		if (Cfg.BlocksCat.miscCat.blendedTextures && blendedTexture)
		{
			return FileTools.getBlendedPath(fileName);
		}
		
		return fileName;
	}
	
	public void setTexture(TextureAtlasSprite texture)
	{
		this.texture = texture;
	}
	
	public TextureAtlasSprite getTexture()
	{
		if (texture == null) return ModelEventHandler.failBackground;
		
		return texture;
	}
	
	public void setDenseTexture(TextureAtlasSprite texture)
	{
		this.denseTexture = texture;
	}
	
	public TextureAtlasSprite getDenseTexture()
	{
		if (denseTexture == null) return ModelEventHandler.failBackground;
		
		return denseTexture;
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
		String translated = null;
		
		if (!ShortTrans.canTranslate(languageKey.replaceAll("  ", "")))
		{
			translated = ShortTrans.unformatted("tile." + languageKey.replaceAll("  ", "") + ".name");
		}
		
		else translated = ShortTrans.unformatted(languageKey);
		
		while (translated.endsWith(" ") || translated.endsWith("\t") || translated.endsWith("\n"))
		{
			translated = translated.substring(0, translated.length() - 1);
		}
		
		return translated;
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
	
	public PropertyGroup getPropertyGroup()
	{
		return PropertyGroup.getGroupByProperties(this);
	}

	@Override
	public String toString()
	{
		return getName();
	}
	
	private void register()
	{
		ORE_PROPERTY_MAP.put(name, this);
	}
	
	public static class DropProperties
	{
		//More default values.
		private double chance = 100.0;
		private int dropMeta = 0, dropSilkTouchMeta = 0;
		private int[] dropRange = new int[] {1, 1}, xpRange = new int[] {0, 0};
		private ResourceLocation dropLookup = new ResourceLocation(""), dropSilkTouchLookup = new ResourceLocation(""), requiredAdvancement = new ResourceLocation("");
		
		public DropProperties(String drop, String dropAlt, int[] dropRange, int[] xpRange)
		{
			setFullDropLookup(drop);
			setFullDropSilkTouchLookup(dropAlt);
			
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
		
		public Item getDrop()
		{
			if (isDropBlock()) return Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(dropLookup));
			
			return ForgeRegistries.ITEMS.getValue(dropLookup);
		}
		
		public ItemStack getDropStack()
		{
			return new ItemStack(getDrop(), 1, getDropMeta());
		}
		
		public void setDropMeta(int meta)
		{
			this.dropMeta = meta;
		}
		
		public int getDropMeta()
		{
			return dropMeta;
		}
		
		public void setFullDropSilkTouchLookup(String fullDrop)
		{
			String[] dropComponents = fullDrop.split(":");
			
			String dropLastComponent = dropComponents[dropComponents.length - 1];
			
			if (StringUtils.isNumeric(dropLastComponent))
			{
				fullDrop = fullDrop.replaceAll(":" + dropLastComponent, "");
				
				this.dropSilkTouchMeta = Integer.parseInt(dropLastComponent);
			}
			
			this.dropSilkTouchLookup = new ResourceLocation(fullDrop);
		}
		
		public Item getDropSilkTouch()
		{
			return Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(dropSilkTouchLookup));
		}
		
		public ItemStack getDropSilkTouchStack()
		{
			return new ItemStack(getDropSilkTouch(), 1, getDropSilkTouchMeta());
		}
		
		public void setDropSilkTouchMeta(int meta)
		{
			this.dropSilkTouchMeta = meta;
		}
		
		public int getDropSilkTouchMeta()
		{
			return dropSilkTouchMeta;
		}
		
		public boolean canDropSelf()
		{
			if (isDropBlock() && Cfg.blocksCat.oreDropCat.variantsDrop)
			{
				Block drop = ForgeRegistries.BLOCKS.getValue(dropLookup);
				Block dropAlt = ForgeRegistries.BLOCKS.getValue(dropSilkTouchLookup);
				
				if (drop.equals(dropAlt)) return true;
			}
			
			return false;
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
			return !StringUtils.isEmpty(requiredAdvancement.toString().replaceAll("minecraft:", ""));
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
			}

			this.parent = json;
			
			addAdditionalDropObjects();
			
			properties.setName(filename);
			setPrimaryValues(json);
			setAllDrops();	
			
			properties.testTextures();
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
			/*
			 * createOverworldVariants == addToDefaultCustomPropertyGroup.
			 * It is a legacy / compatibility key.
			 */			
			if (parent.get("languageKey") != null) properties.setLanguageKey(parent.get("languageKey").getAsString());
			
			if (parent.get("hardness") != null) properties.setHardness(parent.get("hardness").getAsFloat());
			
			if (parent.get("level") != null) properties.setLevel(parent.get("level").getAsInt());
			
			if (parent.get("lightLevel") != null) properties.setLightLevel(parent.get("lightLevel").getAsFloat());
			
			if (parent.get("backgroundMatcher") != null) properties.setBackgroundMatcher(parent.get("backgroundMatcher").getAsString());
			
			if (parent.get("originalTexture") != null) properties.setOriginalTexture(parent.get("originalTexture").getAsString());
			
			if (parent.get("useBlendedTexture") != null && parent.get("useBlendedTexture").getAsBoolean()) properties.setUseBlendedTextures();
		}
		
		private void setAllDrops()
		{			
			List<DropProperties> dropPropsList = new ArrayList<>();
			
			/*
			 * dropAlt(Meta) == dropSilkTouch(Meta).
			 * It is a legacy / compatibility key.
			 */
			for (JsonObject obj : jsons.keySet())
			{
				DropProperties dropProps = jsons.get(obj);
				
				if (getArray(obj, "Drop") != null) dropProps.setDropRange(getArray(obj, "Drop"));
				
				if (getArray(obj, "Xp") != null) dropProps.setXpRange(getArray(obj, "Xp"));
				
				if (obj.get("drop") != null) dropProps.setFullDropLookup(obj.get("drop").getAsString());
				
				if (obj.get("dropAlt") != null) dropProps.setFullDropSilkTouchLookup(obj.get("dropAlt").getAsString());
				
				if (obj.get("dropSilkTouch") != null) dropProps.setFullDropSilkTouchLookup(obj.get("dropSilkTouch").getAsString());
				
				if (obj.get("dropMeta") != null) dropProps.setDropMeta(obj.get("dropMeta").getAsInt());
				
				if (obj.get("dropAltMeta") != null) dropProps.setDropSilkTouchMeta(obj.get("dropAltMeta").getAsInt());
				
				if (obj.get("dropSilkTouchMeta") != null) dropProps.setDropSilkTouchMeta(obj.get("dropSilkTouchMeta").getAsInt());
				
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