package personthecat.mod.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

//cheap haxx look away pls
public class ModConfigReader
{
	public static boolean quarkJasperOn,quarkLimestoneOn,quarkMarbleOn,quarkSlateOn;
	
	//I need to read from Quark's config file directly because I refer to some of its assets before they get registered. 
	//This is necessary FOR NOW in order to avoid crashes, sadly. 
	//For the other mods, it doesn't actually matter because I'm not even making references to their blocks. 
	public static void readQuarkConfig()
	{
		if (Loader.isModLoaded("quark"))
		{
			File quarkConfigFile = new File(Loader.instance().getConfigDir(), "quark.cfg");
			Configuration quarkConfig = new Configuration(quarkConfigFile);
			quarkConfig.load();
			quarkJasperOn = quarkConfig.get("world.revamp stone gen", "Enable Jasper", true).getBoolean();
			quarkLimestoneOn = quarkConfig.get("world.revamp stone gen", "Enable Limestone", true).getBoolean();
			quarkMarbleOn = quarkConfig.get("world.revamp stone gen", "Enable Marble", true).getBoolean();
			quarkSlate = quarkConfig.get("world.revamp stone gen", "Enable Slate", true).getBoolean();
		}
	}
	
	//haxxxxxx. Should at least be using the language keys, when necessary.
	public static void disableModGeneration()
	{		
		if (Cfg.isOriginalGenerationDisabled("iceandfire"))
		{
			File iceAndFireConfigFile = new File(Loader.instance().getConfigDir(), "iceandfire.cfg");
			Configuration iceAndFireConfig = new Configuration(iceAndFireConfigFile);
			iceAndFireConfig.load();
			
			Property sapphireGen = iceAndFireConfig.get("generation", "generateSapphireOre", true);
			Property silverGen = iceAndFireConfig.get("generation", "generateSilverOre", true);
			
			sapphireGen.set(false);
			silverGen.set(false);
			
			iceAndFireConfig.save();
		}
		
		if (Cfg.isOriginalGenerationDisabled("simpleores"))
		{
			File simpleOresConfigFile = new File(Loader.instance().getConfigDir(), "AleXndr/simpleores.cfg");
			Configuration simpleOresConfig = new Configuration(simpleOresConfigFile);
			simpleOresConfig.load();

			Property copperGen = simpleOresConfig.get("Ores.Copper Ore", "enableOreGen", true);
			Property tinGen = simpleOresConfig.get("Ores.Tin Ore", "enableOreGen", true);
			Property mythrilGen = simpleOresConfig.get("Ores.Mythril Ore", "enableOreGen", true);
			Property adamantiumGen = simpleOresConfig.get("Ores.Adamantium Ore", "enableOreGen", true);
			Property onyxGen = simpleOresConfig.get("Ores.Onyx Ore", "enableOreGen", true);
			
			copperGen.set(false);
			tinGen.set(false);
			mythrilGen.set(false);
			adamantiumGen.set(false);
			onyxGen.set(false);
			
			simpleOresConfig.save();
		}
		
		if (Cfg.isOriginalGenerationDisabled("basemetals"))
		{
			File baseMetalsConfigFile = new File(Loader.instance().getConfigDir(), "BaseMetals.cfg");
			Configuration baseMetalsConfig = new Configuration(baseMetalsConfigFile);
			baseMetalsConfig.load();
			
			Property oreSpawn = baseMetalsConfig.get("general", "using_orespawn", true);
			Property fallbackOreSpawn = baseMetalsConfig.get("general", "fallback_orespawn", true);
			
			oreSpawn.set(false);
			fallbackOreSpawn.set(false);
			
			baseMetalsConfig.save();
		}
		
		if (Cfg.isOriginalGenerationDisabled("biomesoplenty"))
		{
			File bopdir = new File(Loader.instance().getConfigDir() + "/biomesoplenty/biomes/defaults/biomesoplenty/");
			bopdir.mkdirs();
			
			List<String> namesToTry = new ArrayList<>();
			namesToTry.add("emeralds");
			namesToTry.add("amber");
			namesToTry.add("malachite");
			namesToTry.add("peridot");
			namesToTry.add("ruby");
			namesToTry.add("sapphire");
			namesToTry.add("tanzanite");
			namesToTry.add("topaz");
			namesToTry.add("amethyst");
			
			for (File file : bopdir.listFiles())
			{
				JsonObject parentObj = null;
				
				JsonParser parser = new JsonParser();
				
				try { parentObj = parser.parse(new FileReader(file)).getAsJsonObject(); }
				
				catch (FileNotFoundException e) { System.err.println("Could not read file " + file.getName()); continue; }
				
				JsonObject generatorObj = parentObj.get("generators").getAsJsonObject();
				
				File newFile = new File(file.getParentFile().getParentFile().getParentFile(), file.getName());
				
				System.out.println("Creating or updating this BOP json: " + newFile.getPath());
				
				for (String oreType : namesToTry)
				{
					if (generatorObj.get(oreType) != null)
					{
						JsonObject newJson = null, newGeneratorObj = null, newChildObj = null;
						
						if (newFile.exists())
						{
							try { newJson = parser.parse(new FileReader(newFile)).getAsJsonObject(); }
							
							catch (FileNotFoundException e) { System.err.println("Could not read file " + file.getName()); continue; }
							
							if (newJson.has("generators"))
							{
								newGeneratorObj = newJson.get("generators").getAsJsonObject();
								
								if (newGeneratorObj.has(oreType))
								{
									newChildObj = newGeneratorObj.get(oreType).getAsJsonObject();
									
									newChildObj.remove("enable");
									newChildObj.addProperty("enable", false);
									
									newGeneratorObj.remove(oreType);
									newGeneratorObj.add(oreType, newChildObj);
									
									newJson.remove("generators");
									newJson.add("generators", newGeneratorObj);
								}
								else
								{
									newChildObj = new JsonObject();
									newChildObj.addProperty("enable", false);
									
									newGeneratorObj.add(oreType, newChildObj);
									
									newJson.remove("generators");
									newJson.add("generators", newGeneratorObj);
								}
							}
							
							else newJson.add("generators", getNewBOPGeneratorsObject(oreType).get("generators"));
						}
						
						else newJson = getNewBOPGeneratorsObject(oreType);
						
						try
						{
							FileWriter writer = new FileWriter(newFile);
							
							writer.write(JsonReader.formatJson(newJson.toString()));
							
							writer.close();
						}
						
						catch(IOException e1) {System.err.println("Could not write to file " + file.getName());}
						
						break;
					}
				}
			}
		}

		if (Cfg.isOriginalGenerationDisabled("glasshearts"))
		{
			File glassHeartsConfigFile = new File(Loader.instance().getConfigDir(), "glasshearts.cfg");
			Configuration glassHeartsConfig = new Configuration(glassHeartsConfigFile);
			glassHeartsConfig.load();
			
			Property agateGen = glassHeartsConfig.get("world", "generateAgateOre", true);
			Property amethystGen = glassHeartsConfig.get("world", "generateAmethystOre", true);
			Property onyxGen = glassHeartsConfig.get("world", "generateOnyxOre", true);
			Property opalGen = glassHeartsConfig.get("world", "generateOpalOre", true);
			Property rubyGen = glassHeartsConfig.get("world", "generateRubyOre", true);
			Property sapphireGen = glassHeartsConfig.get("world", "generateSapphireOre", true);
			Property topazGen = glassHeartsConfig.get("world", "generateTopazOre", true);
			
			agateGen.set(false);
			amethystGen.set(false);
			onyxGen.set(false);
			opalGen.set(false);
			rubyGen.set(false);
			sapphireGen.set(false);
			topazGen.set(false);
			
			glassHeartsConfig.save();
		}
		
		if (Cfg.isOriginalGenerationDisabled("embers"))
		{
			File embersConfigFile = new File(Loader.instance().getConfigDir(), "embers.cfg");
			Configuration embersConfig = new Configuration(embersConfigFile);
			embersConfig.load();
			
			Property aluminumGen = embersConfig.get("compat", "aluminumVeinsPerChunk", 4);
			Property copperGen = embersConfig.get("ores", "copperVeinsPerChunk", 6);
			Property leadGen = embersConfig.get("ores", "leadVeinsPerChunk", 4);
			Property nickelGen = embersConfig.get("compat", "nickelVeinsPerChunk", 4);
			Property quartzGen = embersConfig.get("ores", "quartzVeinsPerChunk", 4);
			Property silverGen = embersConfig.get("ores", "silverVeinsPerChunk", 4);
			Property tinGen = embersConfig.get("compat", "tinVeinsPerChunk", 6);
			
			aluminumGen.set(0);
			copperGen.set(0);
			leadGen.set(0);
			nickelGen.set(0);
			quartzGen.set(0);
			silverGen.set(0);
			tinGen.set(0);
			
			embersConfig.save();
		}
		
		if (Cfg.isOriginalGenerationDisabled("immersiveengineering"))
		{
			File immersiveEngineeringConfigFile = new File(Loader.instance().getConfigDir(), "immersiveengineering.cfg");
			Configuration immersiveEngineeringConfig = new Configuration(immersiveEngineeringConfigFile);
			immersiveEngineeringConfig.load();
			
			Property aluminumGen = immersiveEngineeringConfig.get("general.ores", "ore_bauxite", new int[] {4, 40, 85, 8, 100});
			Property copperGen = immersiveEngineeringConfig.get("general.ores", "ore_copper", new int[] {8, 40, 72, 8, 100});
			Property leadGen = immersiveEngineeringConfig.get("general.ores", "ore_lead", new int[] {6, 8, 36, 4, 100});
			Property nickelGen = immersiveEngineeringConfig.get("general.ores", "ore_nickel", new int[] {6, 8 , 24, 2, 100});
			Property silverGen = immersiveEngineeringConfig.get("general.ores", "ore_silver", new int[] {8, 8, 40, 4, 80});
			Property uraniumGen = immersiveEngineeringConfig.get("general.ores", "ore_uranium", new int[] {4, 8, 24, 2, 60});
			
			aluminumGen.set(new int[] {4, 40, 85, 0, 0});
			copperGen.set(new int[] {8, 40, 72, 0, 0});
			leadGen.set(new int[] {6, 8, 36, 0, 0});
			nickelGen.set(new int[] {6, 8 , 24, 0, 0});
			silverGen.set(new int[] {8, 8, 40, 0, 0});
			uraniumGen.set(new int[] {4, 8, 24, 0, 0});
			
			immersiveEngineeringConfig.save();
		}
		
		if (Cfg.isOriginalGenerationDisabled("thaumcraft"))
		{
			File thaumcraftConfigFile = new File(Loader.instance().getConfigDir(), "thaumcraft_world.cfg");
			Configuration thaumcraftConfig = new Configuration(thaumcraftConfigFile);
			thaumcraftConfig.load();
			
			Property amberGen = thaumcraftConfig.get("general", "generateAmber", true);
			Property cinnabarGen = thaumcraftConfig.get("general", "generateCinnabar", true);
			Property quartzGen = thaumcraftConfig.get("general", "generateQuartz", true);
			
			amberGen.set(false);
			cinnabarGen.set(false);
			quartzGen.set(false);
			
			thaumcraftConfig.save();
		}
	}
	
	private static JsonObject getNewBOPGeneratorsObject(String oreName)
	{
		JsonObject newJson = new JsonObject();
		JsonObject newGeneratorObj = new JsonObject();
		JsonObject newChildObj = new JsonObject();
		
		newChildObj.addProperty("enable", false);
		newGeneratorObj.add(oreName, newChildObj);
		newJson.add("generators" , newGeneratorObj);
		
		return newJson;
	}
}
