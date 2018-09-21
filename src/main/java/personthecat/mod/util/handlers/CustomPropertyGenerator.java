package personthecat.mod.util.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import personthecat.mod.config.Cfg;
import personthecat.mod.config.JsonReader;
import personthecat.mod.util.CommonMethods;
import personthecat.mod.util.ZipTools;

@EventBusSubscriber
public class CustomPropertyGenerator
{
	//Number of tries for estimated values.
	private static final int PRECISION = 15; 
	
	@SubscribeEvent
	public static void onWorldEventLoad(WorldEvent.Load event)
	{
		String[] requestedOres = Cfg.blockRegistryCat.propertyModsCat._requestedCustomOres;
		
		for (int i = 0; i < requestedOres.length; i++)
		{
			if (!StringUtils.isEmpty(requestedOres[i]))
			{
				generateBlockInfo(
						CommonMethods.getBlockState(requestedOres[i]),
						event.getWorld()
						);
			}
		}
		
		if (requestedOres.length > 0)
		{
			System.out.println("Finished writing new OSV zip files. Please verify their contents before rebooting.");
		}
	}
	
	private static void generateBlockInfo(IBlockState state, World world)
	{
		String osvName = createBlockName(state);

		File zip = new File(Loader.instance().getConfigDir() + "/ore_stone_variants_mods/" + osvName + ".zip");
		
		if (!zip.exists())
		{
			//Working info
			BlockPos dummyPos = new BlockPos(0, 0, 0);
			Block ore = state.getBlock();	
			ItemStack stack = new ItemStack(ore, 1, ore.getMetaFromState(state));
			
			ZipTools.createEmptyZipFile(zip);
			
			JsonObject oreProperties = createOreProperties(state, world, stack, dummyPos);
			JsonObject recipeProperties = createRecipeProperties(stack);
			
			writeJsonToZip("OreProperties.json", oreProperties, zip);
			writeJsonToZip("RecipeProperties.json", recipeProperties, zip);
		}
		
		System.out.println("Successfully wrote properties to " + osvName + ".zip");
	}
	
	private static String createBlockName(IBlockState state)
	{
		ResourceLocation registryName = state.getBlock().getRegistryName();
		
		if (registryName.getResourcePath().endsWith("_ore"))
		{
			return registryName.getResourceDomain() + "_" + registryName.getResourcePath();
		}
		
		else if (registryName.getResourcePath().startsWith("ore_"))
		{
			String[] split = registryName.getResourcePath().split("_");
			
			return registryName.getResourceDomain() + "_" + split[1] + "_" + split[0];
		}
		
		Block ore = state.getBlock();
		Item item = ore.getItemDropped(state, new Random(), 0);
		int meta = ore.damageDropped(state);
		
		/*
		 * Looking for better ideas...
		 * 
		 * Gets the first part of the translated name for the ore's drop, 
		 * lower cased.
		 */
		String dropDisplayName = new ItemStack(item, 1, meta).getDisplayName().toLowerCase();
		
		if (dropDisplayName.contains(" "))
		{
			dropDisplayName = dropDisplayName.substring(0, dropDisplayName.indexOf(" "));
		}

		return registryName.getResourceDomain() + "_" + dropDisplayName + "_" + "ore";
	}
	
	private static JsonObject createOreProperties(IBlockState state, World world, ItemStack stack, BlockPos pos)
	{
		JsonObject oreProperties = new JsonObject();
		
		oreProperties.addProperty("addToCustomPropertyGroup", state.getBlock().getRegistryName().getResourceDomain());
		
		oreProperties.addProperty("originalTexture", getTexturePath(state));
		
		oreProperties.addProperty("languageKey", (stack.getUnlocalizedName() + ".name").replaceAll(".name.name", ".name"));
		
		oreProperties.addProperty("hardness", state.getBlockHardness(world, pos));
		
		oreProperties.addProperty("harvestLevel", state.getBlock().getHarvestLevel(state));
		
		oreProperties.addProperty("drop", getPrimaryDrop(state, world));
		
		oreProperties.addProperty("dropSilkTouch", getSpecialRegistryName(state));
		
		oreProperties.add("dropRange", getDropRange(state, world));
		
		oreProperties.add("xpRange", getXpRange(state, world, pos));
		
		setAdditionalDrops(oreProperties, state, world, pos);
		
		return oreProperties;
	}
	
	private static JsonObject createRecipeProperties(ItemStack stack)
	{
		JsonObject recipeProperties = new JsonObject();

		ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);

		recipeProperties.addProperty("result", result.getItem().getRegistryName().toString());
		
		recipeProperties.addProperty("resultMeta", result.getItemDamage());
		
		recipeProperties.addProperty("quantity", result.getCount());
		
		recipeProperties.addProperty("xp", FurnaceRecipes.instance().getSmeltingExperience(result));
		
		return recipeProperties;
	}
	
	private static String getTexturePath(IBlockState state)
	{
		String location = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state).getIconName();
		
		String[] split = location.split(":");
		
		return "assets/" + split[0] + "/textures/" + split[1] + ".png";
	}
	
	private static String getPrimaryDrop(IBlockState state, World world)
	{
		String drop = state.getBlock().getItemDropped(state, world.rand, 0).getRegistryName().toString();
		
		drop += ":" + state.getBlock().damageDropped(state);
		
		return drop;
	}
	
	private static void setAdditionalDrops(JsonObject json, IBlockState state, World world, BlockPos pos)
	{
		ItemStack[] drops = new ItemStack[200];

		for (int i = 0; i < drops.length;)
		{
			List<ItemStack> current = state.getBlock().getDrops(world, pos, state, 0);
			
			for (ItemStack stack : current)
			{
				if (i < 200)
				{
					drops[i] = stack;
					
					i++;
				}
			}
		}
		
		Map<String, Integer> uniqueDrops = new HashMap<>();
		
		for (ItemStack stack : drops)
		{
			String registryName = stack.getItem().getRegistryName().toString() + ":" + stack.getItemDamage();
			
			if (!uniqueDrops.keySet().contains(registryName))
			{
				uniqueDrops.put(registryName, 1);
			}
			
			else uniqueDrops.put(registryName, uniqueDrops.get(registryName) + 1);
		}
		
		JsonObject additionalDrops = new JsonObject();
		
		JsonArray keys = new JsonArray();
		
		for (String name : uniqueDrops.keySet())
		{
			if (!json.get("drop").getAsString().equals(name))
			{
				String keyName = name.split(":")[1];
				
				keys.add(keyName);
				
				JsonObject newDrop = new JsonObject();
				
				newDrop.addProperty("//", 
						"Cannot yet calculate drop/xp range for additional drops. "
					  + "These default to [1] and [0], respectively.");
				
				newDrop.addProperty("drop", name);
				
				newDrop.addProperty("chance", uniqueDrops.get(name) / 2);
				
				json.add(keyName, newDrop);
			}
		}
		
		json.add("additionalDropKeys", keys);		
	}
	
	private static JsonArray getDropRange(IBlockState state, World world)
	{
		int[] dropRange = new int[PRECISION];
		
		for (int i = 0; i < PRECISION; i++) dropRange[i] = state.getBlock().quantityDropped(world.rand);
		
		return getJsonArray(dropRange);
	}
	
	private static JsonArray getXpRange(IBlockState state, World world, BlockPos pos)
	{		
		int[] xpRange = new int[PRECISION];
		
		for (int i = 0; i < PRECISION; i++) xpRange[i] = state.getBlock().getExpDrop(state, world, pos, 0);
		
		return getJsonArray(xpRange);
	}
	
	private static JsonArray getJsonArray(int[] ints)
	{
		JsonArray array = new JsonArray();
		
		Arrays.sort(ints);
		
		int lowValue = ints[0], highValue = ints[ints.length - 1];
		
		array.add(lowValue);
		
		if (lowValue != highValue)
			array.add(highValue);
		
		return array;
	}

	private static String getSpecialRegistryName(IBlockState state)
	{
		Block ore = state.getBlock();
		
		String name = ore.getRegistryName().toString();
		
		name += ":" + ore.getMetaFromState(state);
		
		return name;
	}
	
	private static void writeJsonToZip(String fileName, JsonObject json, File zip)
	{
		try
		{
			File temp = File.createTempFile("properties", ".json");
			temp.deleteOnExit();
			
			FileWriter writer = new FileWriter(temp);
			
			writer.write(JsonReader.formatJson(json.toString()));
			
			writer.close();
			
			ZipTools.copyToZip(fileName, temp, zip);
		}
		
		catch (IOException e) {System.err.println("Error: Could not write " + fileName + " to " + zip.getName());}
	}
}