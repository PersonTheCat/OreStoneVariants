package personthecat.mod.properties;

import static personthecat.mod.Main.logger;
import static personthecat.mod.Main.proxy;
import static personthecat.mod.util.CommonMethods.getBlockState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.advancements.AdvancementMap;
import personthecat.mod.config.Cfg;
import personthecat.mod.config.JsonReader;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.Reference;
import personthecat.mod.util.ShortTrans;
import personthecat.mod.util.ZipTools;

/**
 * To-do: Create a separate class for dynamic ore properties?
 */
public class OreProperties
{	
	//Some default values.
	protected boolean hasBuiltInTextures = true, blendedTexture = false, inUse = false;
	protected DropProperties[] dropProperties = new DropProperties[] {new DropProperties()};
	protected float hardness = 3.0F, lightLevel = 0F;
	protected int level = 2;
	protected String name, modName = "minecraft", languageKey, backgroundMatcher = "assets/minecraft/textures/blocks/stone.png", originalTexture;
	protected TextureAtlasSprite texture, denseTexture;

	protected static final Map<String, OreProperties> ORE_PROPERTY_MAP = new HashMap<>();
	
	public OreProperties(String name, String languageKey, float hardness, int level, DropProperties... drops)
	{
		this.name = name;
		this.languageKey = languageKey;
		this.hardness = hardness;
		this.level = level;
		this.dropProperties = drops;

		register();
	}
	
	protected OreProperties() {}
	
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

	/**
	 * @return is based on three conditions:
	 * * name matches prop.name -> this
	 * * prop.matchesFullLookup -> this
	 * * else -> 
	 *   * name matches valid block registry -> new dynamic props
	 *   * else -> null
	 */
	public static OreProperties propertiesOf(String name)
	{
		if (ORE_PROPERTY_MAP.containsKey(name))
		{
			return ORE_PROPERTY_MAP.get(name);
		}
		for (OreProperties props : ORE_PROPERTY_MAP.values())
		{
			if (props.matchesFullLookup(name)) return props;
		}
		
		IBlockState fromName = getBlockState(name);
		
		if (!fromName.equals(Blocks.AIR.getDefaultState()))
		{
			return new OrePropertiesDynamic(name);
		}
		
		throw new RuntimeException("Error: Unable to find properties or block with name \"" + name + "\"");
	}
		
	public WorldGenProperties getWorldGenProperties()
	{
		return WorldGenProperties.WORLDGEN_PROPERTY_MAP.get(name) != null ? WorldGenProperties.WORLDGEN_PROPERTY_MAP.get(name) : WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("gold_ore");
	}
	
	public RecipeProperties getRecipeProperties()
	{
		return RecipeProperties.RECIPE_PROPERTY_MAP.get(name) != null ? RecipeProperties.RECIPE_PROPERTY_MAP.get(name) : RecipeProperties.DO_NOTHING;
	}
	
	public RecipeProperties createRecipeProperties(boolean testForOverrides)
	{
		return RecipeProperties.createRecipeProperties(name, dropProperties[0].getDropSilkTouchStack(), testForOverrides);
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
			
			if ((props.getChance() / 100.0) >= worldIn.rand.nextDouble()) drops.add(props);
		}
		
		return drops.toArray(new DropProperties[] {});
	}
	
	public List<ItemStack> getDrops(World world, BlockPos pos, IBlockState state, int fortune, DropProperties[] currentDrops, boolean isDense, ItemStack selfStack)
	{
		List<ItemStack> drops = new ArrayList<>();
		
		for (DropProperties drop : currentDrops)
		{
			int quantity = MathHelper.getInt(world.rand, drop.getLeastDrop(), isDense ? drop.getMostDrop() * 3 : drop.getMostDrop());
			
			if (!drop.isDropBlock()) quantity = fortune > 0 ? quantity * (MathHelper.abs(world.rand.nextInt(fortune + 2) - 1) + 1) : quantity;

			ItemStack stack = drop.getDropStack();
			
			if (drop.canDropSelf()) stack = selfStack;
			
			stack.setCount(quantity);

			drops.add(stack);
		}
		
		return drops;
	}
	
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune, DropProperties[] currentDrops)
	{
    	Random rand = world instanceof World ? ((World)world).rand : new Random();
    	
		int i = 0;
		
		for (DropProperties drop : currentDrops)
		{    			
			if (!drop.isDropBlock()) i += MathHelper.getInt(rand, drop.getLeastXp(), drop.getMostXp());
		}

		return i;
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
		proxy.testTextureLocation(originalTexture, getName());
		proxy.testTextureLocation(backgroundMatcher, getName());
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
	
	public float getHardness(World world, BlockPos pos)
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
	
	public boolean matchesFullLookup(String fullLookup)
	{
		String[] split = fullLookup.split(":");
		
		boolean endIsNum = StringUtils.isNumeric(split[split.length - 1]);
		
		int meta = endIsNum ? meta = Integer.parseInt(split[split.length - 1]) : 0;
		
		if (meta != dropProperties[0].dropSilkTouchMeta) return false;
		
		if (endIsNum) fullLookup = fullLookup.replaceAll(":" + meta, "");
		
		return dropProperties[0].dropSilkTouchLookup.toString().equals(fullLookup);
	}
	
	public IBlockState getOreState()
	{
		return dropProperties[0].ore;
	}
	
	protected void register()
	{
		ORE_PROPERTY_MAP.put(name, this);
	}
	
	public static class DropProperties
	{
		//More default values.
		protected double chance = 100.0;
		protected IBlockState ore;
		protected int dropMeta = 0, dropSilkTouchMeta = 0;
		protected int[] dropRange = new int[] {1, 1}, xpRange = new int[] {0, 0};
		protected Item drop;
		protected ResourceLocation dropLookup = new ResourceLocation(""), dropSilkTouchLookup = new ResourceLocation(""), requiredAdvancement = new ResourceLocation("");
		
		public DropProperties(String drop, String dropAlt, int[] dropRange, int[] xpRange)
		{
			setFullDropLookup(drop);
			setFullDropSilkTouchLookup(dropAlt);
			
			Arrays.sort(dropRange);
			Arrays.sort(xpRange);
			
			this.dropRange = dropRange;
			this.xpRange = xpRange;
		}
		
		protected DropProperties() {}
		
		public static void loadAllOres()
		{
			for (OreProperties props : ORE_PROPERTY_MAP.values())
			{
				if (props.inUse())
				{
					for (DropProperties drops : props.getDropProperties())
					{
						drops.ore = drops.getOreFromLookup();
					}
				}
			}
		}
		
		/**
		 * Running this separately from loadAllOres() due to
		 * {@link Item#getItemFromBlock(Block)} being finicky. 
		 */
		public static void loadAllItems()
		{
			for (OreProperties props : ORE_PROPERTY_MAP.values())
			{
				if (props.inUse())
				{
					for (DropProperties drops : props.getDropProperties())
					{
						drops.drop = drops.getDropFromLookup();
					}
				}
			}
		}
		
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
		
		public Item getDropFromLookup()
		{
			if (isDropBlock()) return Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(dropLookup));
			
			return ForgeRegistries.ITEMS.getValue(dropLookup);
		}
		
		public Item getDrop()
		{
			return drop;
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
		
		private IBlockState getOreFromLookup()
		{
			Block dropSilkTouch = ForgeRegistries.BLOCKS.getValue(dropSilkTouchLookup);
			
			return dropSilkTouch.getStateFromMeta(dropSilkTouchMeta);
		}
		
		public Item getDropSilkTouch()
		{
			return Item.getItemFromBlock(ore.getBlock());
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
				Block dropBlock = Block.getBlockFromItem(drop);
				Block dropSilkTouch = ore.getBlock();
				
				if (dropBlock.equals(dropSilkTouch)) return true;
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
		private final String fileName;
		private final String name;
		private final OreProperties properties;
		private final JsonObject parent;
		private boolean fileChanged = false;
		private JsonObject updated;
		private Map<JsonObject, DropProperties> jsons = new HashMap<>();
		
		public FromJson(JsonObject json, String fileName)
		{
			this.fileName = fileName;
			this.name = fileName.replaceAll(".zip", "");
			this.parent = json;

			if (ORE_PROPERTY_MAP.get(name) != null)
			{
				this.properties = ORE_PROPERTY_MAP.get(name);
				
				jsons.put(json, properties.getDropProperties()[0]);
			}
			else
			{
				this.properties = new OreProperties();
				
				jsons.put(json, new DropProperties());
			}

			addAdditionalDropObjects();

			properties.setName(name);

			setPrimaryValues(json);

			setAllDrops();	

			properties.testTextures();
			properties.register();
			
			updateFile();
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
			if (parent.has("createOverworldVariants") && parent.get("createOverworldVariants").getAsBoolean())
			{
				logger.info("Removing createOverworldVariants");
				
				remove("createOverworldVariants");
				
				logger.info("adding / registering block group custom");
				
				addAndRegisterBlockGroup("custom");
			}
			else if (parent.has("addToDefaultCustomPropertyGroup") && parent.get("addToDefaultCustomPropertyGroup").getAsBoolean())
			{
				remove("addToDefaultCustomPropertyGroup");
				
				addAndRegisterBlockGroup("custom");
			}
			else if (parent.has("addToCustomPropertyGroup"))
			{
				String group = parent.get("addToCustomPropertyGroup").getAsString();
				
				remove("addToCustomPropertyGroup");
				
				addAndRegisterBlockGroup(group);
			}
			
			if (parent.has("languageKey")) properties.setLanguageKey(parent.get("languageKey").getAsString());
			
			if (parent.has("hardness")) properties.setHardness(parent.get("hardness").getAsFloat());
			
			if (parent.has("level")) properties.setLevel(parent.get("level").getAsInt());
			
			if (parent.has("lightLevel")) properties.setLightLevel(parent.get("lightLevel").getAsFloat());
			
			if (parent.has("backgroundMatcher")) properties.setBackgroundMatcher(parent.get("backgroundMatcher").getAsString());
			
			if (parent.has("originalTexture")) properties.setOriginalTexture(parent.get("originalTexture").getAsString());
			
			if (parent.has("useBlendedTexture") && parent.get("useBlendedTexture").getAsBoolean()) properties.setUseBlendedTextures();
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
				
				if (obj.has("drop")) dropProps.setFullDropLookup(obj.get("drop").getAsString());
				
				if (obj.has("dropAlt")) dropProps.setFullDropSilkTouchLookup(obj.get("dropAlt").getAsString());
				
				if (obj.has("dropSilkTouch")) dropProps.setFullDropSilkTouchLookup(obj.get("dropSilkTouch").getAsString());
				
				if (obj.has("dropMeta")) dropProps.setDropMeta(obj.get("dropMeta").getAsInt());
				
				if (obj.has("dropAltMeta")) dropProps.setDropSilkTouchMeta(obj.get("dropAltMeta").getAsInt());
				
				if (obj.has("dropSilkTouchMeta")) dropProps.setDropSilkTouchMeta(obj.get("dropSilkTouchMeta").getAsInt());
				
				if (obj.has("chance")) dropProps.setChance(obj.get("chance").getAsDouble());

				dropPropsList.add(dropProps);
			}
			
			properties.setDropProperties(dropPropsList.toArray(new DropProperties[] {}));
		}
		
		private static int[] getArray(JsonObject obj, String partialKey)
		{
			return JsonReader.getArray(obj, partialKey, "least", "most");
		}
		
		/**
		 * Removes the element from a separate json to avoid 
		 * changing the hash value and breaking the map.
		 */
		private void remove(String property)
		{
			fileChanged();
			updated.remove(property);
		}
		
		private void fileChanged()
		{
			if (!fileChanged)
			{
				fileChanged = true;

				updated = JsonReader.cloneJson(parent);
			}
		}
		
		private void addAndRegisterBlockGroup(String group)
		{
			String[] existing = Cfg.blockRegistryCat.propertyGroupsCat.get(group);

			if (existing == null) existing = new String[0];

			if (!ArrayUtils.contains(existing, name))
			{
				Cfg.blockRegistryCat.propertyGroupsCat.put(group, ArrayUtils.add(existing, name));
			}
			
			Cfg.addBlockEntry(group, "default");

			Cfg.configChanged();
		}
		
		private void updateFile()
		{
			if (fileChanged)
			{
				logger.info("Preparing to update file...");
				
				if (fileName.endsWith(".zip"))
				{
					try
					{
						File temp = File.createTempFile("OreProperties", ".json");
						File zip = new File(JsonReader.directory, fileName);
						
						logger.info("file being updated: " + zip.getPath());
						
						FileTools.writeToFile(temp, JsonReader.formatJson(updated.toString()));
						
						ZipTools.copyToZip("OreProperties.json", temp, zip, true);
						
						temp.delete();
					}
					catch (IOException e) {	logger.warn("Unable to update " + fileName + ". You may want to manually remove property group entries."); }
				}
				else
				{
					File propertiesFile = new File(JsonReader.directory, "/" + fileName + "/OreProperties.json");
					
					FileTools.writeToFile(propertiesFile, JsonReader.formatJson(updated.toString()));
				}
			}
		}
	}
}