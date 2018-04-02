package personthecat.mod.config;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.properties.DefaultProperties.DefaultOreProperties;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.properties.RecipeProperties;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.Reference;

public class JsonReader
{
	public static final PropertyGroup CUSTOM_PROPERTY_GROUP = new PropertyGroup("thisvaluedoesntmatter");
	public static final List<String> NEW_PROPERTY_NAMES = new ArrayList<String>();
	
	public static void loadNewProperties()
	{		
		JsonParser parser = new JsonParser();
		File directory = new File(Loader.instance().getConfigDir() + "/" + Reference.MODID + "_mods/");		
		
		for (File file : directory.listFiles())
		{		
			String name = file.getName().replaceAll(".zip", "");
			
			if (!name.endsWith("_ore")) continue;
			
			for (DefaultOreProperties existingProperty : DefaultOreProperties.values())
			{
				if (name.equals(existingProperty.toString().toLowerCase())) continue;
			}
			
			Gson gson = new Gson();		
			
			//Getting these properties manually to account for field name differences. May create setters later and use those for such values, instead. 
			JsonObject orePropObj = getProperties(name, "OreProperties.json");
			if (orePropObj != null)
			{				
				NEW_PROPERTY_NAMES.add(name);
				
				boolean canCreateVariants = orePropObj.get("createOverworldVariants").getAsBoolean();
				String languageKey = orePropObj.get("languageKey").getAsString();
				float hardness = orePropObj.get("hardness").getAsFloat();
				int level = orePropObj.get("harvestLevel").getAsInt();
				boolean isDropBlock = orePropObj.get("isDropBlock").getAsBoolean();
				String drop = orePropObj.get("drop").getAsString();
				int dropMeta = orePropObj.get("dropMeta").getAsInt();
				String dropAlt = orePropObj.get("dropAlt").getAsString();
				int dropAltMeta = orePropObj.get("dropAltMeta").getAsInt();
				int leastDrop = orePropObj.get("leastDrop").getAsInt();
				int mostDrop = orePropObj.get("mostDrop").getAsInt();
				int leastXp = orePropObj.get("leastXp").getAsInt();
				int mostXp = orePropObj.get("mostXp").getAsInt();
				String backgroundMatcher = orePropObj.get("backgroundMatcher").getAsString(); 
				String originalTexture = orePropObj.get("originalTexture").getAsString();
				float lightLevel = orePropObj.get("lightLevel") != null ? orePropObj.get("lightLevel").getAsFloat() : 0F; 
			
				OreProperties newOreProperty = new OreProperties(name, languageKey, hardness, level, isDropBlock, drop, dropMeta, dropAlt, dropAltMeta, leastDrop, mostDrop, leastXp, mostXp);
				
				newOreProperty.setBackgroundMatcher(backgroundMatcher);
				newOreProperty.setOriginalTexture(originalTexture);
				newOreProperty.setLightLevel(lightLevel);

				if (canCreateVariants) CUSTOM_PROPERTY_GROUP.addProperties(newOreProperty);
			}
			
			//Same. Needs a few setters. 
			JsonObject worldGenPropObj = getProperties(name, "WorldGenProperties.json");
			if (worldGenPropObj != null)
			{
				int blockCount = worldGenPropObj.get("blockCount").getAsInt();
				int chance = worldGenPropObj.get("chance").getAsInt();
				int minHeight = worldGenPropObj.get("minHeight").getAsInt();
				int maxHeight = worldGenPropObj.get("maxHeight").getAsInt();
				
				List<String> biomeName = new ArrayList<String>();
				List<Type> biomeType = new ArrayList<Type>();
				
				JsonArray biomeNames = worldGenPropObj.get("biomeNameList").getAsJsonArray();
				for (JsonElement element : biomeNames)
				{
					biomeName.add(element.getAsString());
				}
				
				JsonArray biomeTypes = worldGenPropObj.get("biomeTypeList").getAsJsonArray();
				for (JsonElement element: biomeTypes)
				{
					Type type = Type.getType(element.getAsString());
					biomeType.add(type);
				}
		
				WorldGenProperties genProp = new WorldGenProperties(name, blockCount, chance, minHeight, maxHeight, biomeType, biomeName);
				
				genProp.register();
			}
			
			JsonObject recipePropObj = getProperties(name, "RecipeProperties.json");
			if (recipePropObj != null)
			{
				RecipeProperties newRecipeProperty = gson.fromJson(recipePropObj, RecipeProperties.class);
			}
		}

		CUSTOM_PROPERTY_GROUP.setConditions(true);
		CUSTOM_PROPERTY_GROUP.register();
		
		//This is a strange location. But it's only because I need this to happen at exactly this time, no sooner or later.
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			ModelEventHandler.registerTextureLocations();
		}	
	}
	
	public static JsonObject getProperties(String oreName, String fileName)
	{
		JsonObject obj = null;
		
		try
		{			
			File jsonFromFolder = new File(Loader.instance().getConfigDir() + "/"  + Reference.MODID + "_mods/" + oreName + "/" + fileName);
			JsonParser parser = new JsonParser();
			
			if (jsonFromFolder.exists())
			{
				obj = parser.parse(new FileReader(jsonFromFolder)).getAsJsonObject();
			}
			
			ZipFile zipFile = new ZipFile(Loader.instance().getConfigDir() + "/"  + Reference.MODID + "_mods/" + oreName + ".zip");
			InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(fileName));
		
			if (zipFile.getEntry(fileName) != null)
			{
				obj = parser.parse(new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))).getAsJsonObject();
			}
			
			inputStream.close();
			zipFile.close();
		}
		
		catch (NullPointerException | IOException e) {e.getSuppressed();}
		
		return obj;
	}
	
	//Currently unused. 
	public static DynamicTexture getDynamicTexture(File file)
	{
		String name = file.getName().replaceAll(".zip", "");
		InputStream inputStream = null;
		BufferedImage bufferedImage = null;
		
		try
		{
			if (file.isDirectory())
			{
				File image = new File(file.getPath() + "/" + name + ".png");
				
				inputStream = new BufferedInputStream(new FileInputStream(image));
				bufferedImage = ImageIO.read(inputStream);
			}
			
			else if (file.getName().endsWith(".zip"))
			{				
				ZipFile zipFile = new ZipFile(file.getPath());
					
				inputStream = zipFile.getInputStream(zipFile.getEntry(name + ".png"));
				bufferedImage = ImageIO.read(inputStream);
				
				zipFile.close();
			}
			
			inputStream.close();
		}
		
		catch (NullPointerException | IOException e) {e.getSuppressed();}		
		
		return new DynamicTexture(bufferedImage);
	}
}
