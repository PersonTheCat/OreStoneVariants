package personthecat.mod.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import personthecat.mod.Main;
import personthecat.mod.objects.model.ModelEventHandler;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.RecipeProperties;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.util.Reference;

public class JsonReader
{
	public static void loadNewProperties()
	{
		File directory = new File(Loader.instance().getConfigDir() + "/" + Reference.MODID + "_mods/");
		directory.mkdirs();
		
		for (File file : directory.listFiles())
		{
			String name = file.getName().replaceAll(".zip", "");

			if (!name.endsWith("_ore")) continue;
			
			Gson gson = new Gson();		
			
			JsonObject orePropObj = getProperties(name, "OreProperties.json");
			if (orePropObj != null)
			{
				new OreProperties.FromJson(orePropObj, name).getProperties();
			}

			JsonObject worldGenPropObj = getProperties(name, "WorldGenProperties.json");
			if (worldGenPropObj != null)
			{
				new WorldGenProperties.FromJson(worldGenPropObj, name);
			}
			
			JsonObject recipePropObj = getProperties(name, "RecipeProperties.json");
			if (recipePropObj != null)
			{
				RecipeProperties newRecipeProperty = gson.fromJson(recipePropObj, RecipeProperties.class);
			}
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
		
		if (obj.get(minKey + partialKey) == null | obj.get(maxKey + partialKey) == null) ints[0] = ints[ints.length - 1];
		
		return ints;
	}
	
	public static String formatJson(String json)
	{
		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(json).getAsJsonObject();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(obj).replace("\n", System.getProperty("line.separator"));
	}
}
