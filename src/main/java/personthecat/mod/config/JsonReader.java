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
import java.util.List;
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
import personthecat.mod.properties.OreProperties.DropProperties;
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
			
			JsonObject orePropObj = getProperties(name, "OreProperties.json");
			if (orePropObj != null)
			{				
				NEW_PROPERTY_NAMES.add(name);
				
				OreProperties.FromJson jsonProperties = new OreProperties.FromJson(orePropObj, name);

				if (jsonProperties.getCanCreateOverworldVariants()) CUSTOM_PROPERTY_GROUP.addProperties(jsonProperties.getProperties());
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
}
