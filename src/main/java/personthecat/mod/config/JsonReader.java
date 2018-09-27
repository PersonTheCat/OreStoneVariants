package personthecat.mod.config;

import static personthecat.mod.Main.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.fml.common.Loader;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.RecipeProperties;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.Reference;

public class JsonReader
{
	public static File directory = new File(Loader.instance().getConfigDir() + "/" + Reference.MODID + "_mods/");
	
	static { directory.mkdirs(); }
	
	public static void loadNewProperties()
	{
		logger.info("Registering custom ore properties.");

		for (File file : directory.listFiles())
		{
			String name = file.getName();
			
			if (!(name.endsWith("_ore") || name.endsWith("_ore.zip"))) continue;

			JsonObject orePropObj = getProperties(name, "OreProperties.json");
			if (orePropObj != null)
			{
				new OreProperties.FromJson(orePropObj, name);
			}

			JsonObject worldGenPropObj = getProperties(name, "WorldGenProperties.json");
			if (worldGenPropObj != null)
			{
				new WorldGenProperties.FromJson(worldGenPropObj, name);
			}
			
			JsonObject recipePropObj = getProperties(name, "RecipeProperties.json");
			if (recipePropObj != null)
			{
				RecipeProperties newRecipeProperty = new Gson().fromJson(recipePropObj, RecipeProperties.class);
				newRecipeProperty.register();
			}
		}
		
		Cfg.conditionalSync();
	}
	
	public static JsonObject getProperties(String fileName, String type)
	{
		try
		{
			JsonParser parser = new JsonParser();
			
			if (fileName.endsWith(".zip"))
			{
				ZipFile zip = new ZipFile(Loader.instance().getConfigDir() + "/" + Reference.MODID + "_mods/" + fileName);
				InputStream is = zip.getInputStream(zip.getEntry(type));
				
				if (is != null)
				{
					JsonObject obj = parser.parse(new BufferedReader(new InputStreamReader(is, "UTF-8"))).getAsJsonObject();
					
					is.close();
					zip.close();
					
					return obj;
				}
				
				zip.close();
			}
			else
			{
				File json = new File(Loader.instance().getConfigDir() + "/" + Reference.MODID + "_mods/" + fileName + "/" + type);
				
				if (json.exists()) return parser.parse(new FileReader(json)).getAsJsonObject(); 
			}
		}
		catch (NullPointerException | IOException ignored) {}
		
		return null;
	}
	
	public static int[] getArray(JsonObject obj, String partialKey, String minKey, String maxKey)
	{			
		JsonElement rangeElement = obj.get(partialKey.toLowerCase() + "Range");

		int[] ints = rangeElement != null ? new int[rangeElement.getAsJsonArray().size()] : new int[2];
		
		if (rangeElement != null)
		{
			for (int i = 0; i < ints.length; i++)
			{
				ints[i] = rangeElement.getAsJsonArray().get(i).getAsInt();
			}
		}
		else if (obj.get(minKey + partialKey) != null || obj.get(maxKey + partialKey) != null)
		{
			ints[0] = obj.get(minKey + partialKey) != null ? obj.get(minKey + partialKey).getAsInt() : null;
			
			ints[ints.length - 1] = obj.get(maxKey + partialKey) != null ? obj.get(maxKey + partialKey).getAsInt() : null;
		}
		else return null;
		
		Arrays.sort(ints);
		
		if (obj.get(minKey + partialKey) == null | obj.get(maxKey + partialKey) == null)
		{
			ints[0] = ints[ints.length - 1];
		}
		
		return ints;
	}
	
	public static String formatJson(String json)
	{
		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(json).getAsJsonObject();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(obj).replace("\n", System.getProperty("line.separator"));
	}
	
	public static JsonObject cloneJson(JsonObject obj)
	{
		JsonObject clone = new JsonObject();
		
		for (Map.Entry<String, JsonElement> entry : obj.entrySet())
		{
			clone.add(entry.getKey(), entry.getValue());
		}
		
		return clone;
	}
}
