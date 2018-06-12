package personthecat.mod.objects.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.ResourcePackRepository.Entry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.Reference;
import personthecat.mod.util.ZipTools;
import personthecat.mod.util.handlers.BlockStateGenerator.State;
import personthecat.mod.util.handlers.RegistryHandler;
import personthecat.mod.util.overlay.SpriteHandler;

//Avert your eyes from this class... Nothing to see here.

@EventBusSubscriber
public class ModelEventHandler
{
	public static TextureAtlasSprite failBackground;
	
	private static final List<String> OVERLAY_LOCATION_REGISTRY = new ArrayList<>();
	private static final Map<String, TextureAtlasSprite> OVERLAY_SPRITE_MAP = new HashMap<>();
	
	private static boolean blendedTextureOverride = false;
	
	@SideOnly(value = Side.CLIENT)
	public static void registerTextureLocations()
	{		
		ZipTools.testForResourcePack();
		
		for (OreProperties property : OreProperties.getOrePropertyRegistry())
		{
			String modName = NameReader.getMod(property.getName());
			
			if ((PropertyGroup.getPropertyGroup(modName.replaceAll("vanilla", "minecraft")).getConditions()))
			{
				String path = "assets/ore_stone_variants/textures/blocks/" + modName + "/" + "blended/" + property.getName() + "_overlay_blended";
				
				if (!ConfigFile.blendedTextures || !property.getUseBlendedTexture())
				{
					path = FileTools.getNormalPath(path + ".png").replaceAll(".png", "");
				}
				
				createAndAddSprites(property, path);
			}
		}
		
		for (String name : OreProperties.CUSTOM_PROPERTY_NAMES)
		{			
			createAndAddSprites(OreProperties.propertiesOf(name), "assets/ore_stone_variants/textures/blocks/" + name + "_overlay");
		}
		
		if (ConfigFile.denseVariants) createDenseSpritesFromList();
		
		RegistryHandler.onRegisterNewResourcesBadly();
	}
	
	private static void testForRPSettings()
	{
		blendedTextureOverride = false;		
		
		ResourceLocation rpSettingsLocation = new ResourceLocation(Reference.MODID, "osv.cfg");
		
		try
		{
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(rpSettingsLocation).getInputStream();
			
			Scanner scanner = new Scanner(is);
			
			while (scanner.hasNextLine())
			{
				String nextLine = scanner.nextLine();

				if (nextLine.trim().equals("force_single_texture_location = true"))
				{
					blendedTextureOverride = true;
				}
			}
			
			scanner.close();
			is.close();
		}
		
		catch (IOException ignored) {}
	}
	
	@SideOnly(value = Side.CLIENT)
	private static void createAndAddSprites(OreProperties property, String location)
	{
		String normalPath = FileTools.getNormalPath(location + ".png");
		
		if ((Minecraft.class.getClassLoader().getResourceAsStream(normalPath) == null))
		{
			SpriteHandler.createOverlays(property.getBackgroundMatcher(), property.getOriginalTexture(), FileTools.getNormalPath(normalPath));
		}
		
		OVERLAY_LOCATION_REGISTRY.add(location);
	}
	
	@SideOnly(value = Side.CLIENT)
	private static void createDenseSpritesFromList()
	{
		List<String> tempLocationClone = new ArrayList<>();
		tempLocationClone.addAll(OVERLAY_LOCATION_REGISTRY);
		
		for (String location : tempLocationClone)
		{
			if (!location.contains("lit_"))
			{
				location = location.replaceAll("blended/", "").replaceAll("_blended", "");
				String fileName = NameReader.getOreFromPath(location);

				SpriteHandler.createDense(location + ".png");
				OVERLAY_LOCATION_REGISTRY.add(location.replaceAll(fileName, "dense_" + fileName));
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(value = Side.CLIENT)
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		testForRPSettings();
		
		for (String location : OVERLAY_LOCATION_REGISTRY)
		{
			String blockName = NameReader.getOreFromPath(location);
			blockName = NameReader.getOre(blockName);
			
			if (blendedTextureOverride) location = FileTools.getNormalPath(location);
			
			TextureAtlasSprite sprite = event.getMap().registerSprite(new ResourceLocation(Reference.MODID, location.replaceAll("assets/ore_stone_variants/textures/", "")));
			OVERLAY_SPRITE_MAP.put(blockName, sprite);
		}
		
		failBackground = Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(new ResourceLocation(Reference.MODID, "blocks/background_finder"));
	}
	
	//Split this all up or something. It's hideous.
	//Reusing SimpleModelBuilder and placing these on ModelBakeEvent instead of creating a new IModel implementation. Sorry. I may do that later, if I have the time.
	@SubscribeEvent
	@SideOnly(value = Side.CLIENT)
	public static void onModelBakeEvent(final ModelBakeEvent event) throws IOException
	{
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			//Name stuff
			String registryName = state.getBlock().getRegistryName().getResourcePath();
			String oreType = NameReader.getOre(registryName);
			
			//Target block
			IBlockState targetBlockState;
			ModelResourceLocation backgroundModelLocation;
			
			//New block
			ModelResourceLocation newModelLocationVariant, newModelLocationInventory;
			TextureAtlasSprite bgOverride = null;
			
			if (!NameReader.isDynamic(state.getBlock()))
			{				
				//Name stuff
				State variant = BlockInit.BLOCKSTATE_STATE_MAP.get(state);
				
				//Target block
				targetBlockState = variant.getBackgroundBlockState();
				backgroundModelLocation = variant.getBackgroundModelLocation();
				
				if (variant.hasForcibleTexture())
				{
					bgOverride = event.getModelManager().getTextureMap().getAtlasSprite(variant.getForceTextureLocation());
				}
				
				//New block
				newModelLocationVariant = new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName), "variant=" + variant.getName());
				newModelLocationInventory = new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName + "_" + variant.getName()) , "inventory");
			}
			
			else
			{				
				//Name stuff
				int i = BlockInit.DYNAMIC_BLOCKSTATES_NUMBER_MAP.get(state);
				String fullName = ConfigInterpreter.getFullEnumeratedName(i);
				String[] nameTester = fullName.split("_");
				registryName = fullName.contains("_ore") ? fullName : fullName.replaceAll(nameTester[0], oreType);
				
				//Target block
				targetBlockState = ConfigInterpreter.getBackgroundBlockState(i);
				backgroundModelLocation = ConfigInterpreter.getBackgroundModelLocation(i);

				//New block
				newModelLocationVariant = new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName), "normal");
				newModelLocationInventory = new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName), "inventory");
			}
			
			//Target model information
			IBakedModel targetModel = modelGuesser(event, backgroundModelLocation);
			
			//New model information
			TextureAtlasSprite overlay = OVERLAY_SPRITE_MAP.get(oreType.replaceAll("lit_", "")) == null ? failBackground : OVERLAY_SPRITE_MAP.get(oreType.replaceAll("lit_", ""));
			
			boolean overrideShade = Arrays.asList(ConfigFile.shadeOverrides).contains(newModelLocationInventory.getResourcePath());
			
			for (String entry : ConfigFile.shadeOverrides) //So that specific models do not need to (but still can) be registered. 
			{
				if (entry.equals(oreType)) overrideShade = true;
			}
			
			//Bake new model
			DynamicModelBaker baker = new DynamicModelBaker();
			IBakedModel newModel = baker.bakeDynamicModel(overrideShade, targetBlockState, targetModel, overlay, bgOverride);
			
			//Place new model
			event.getModelRegistry().putObject(newModelLocationVariant, newModel);
			event.getModelRegistry().putObject(newModelLocationInventory, newModel);
			
			if (NameReader.isDynamic(state.getBlock()) && NameReader.isLit(state.getBlock()))
			{
				event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, "lit_" + registryName), "normal"), newModel);
				event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, "lit_" + registryName), "inventory"), newModel);
			}
		}
	}

	private static IBakedModel modelGuesser(ModelBakeEvent event, ModelResourceLocation tryMe)
	{
		IBakedModel model = event.getModelManager().getModel(tryMe);
		String[] multiVariantSeparator = tryMe.getVariant().split(",");
		
		List<String> locationsToTry = new ArrayList<>();
		
		for (String variantString : multiVariantSeparator)
		{
			String[] variantRemover = variantString.split("=");
			
			if (variantRemover.length > 1)
			{
				locationsToTry.add(variantRemover[1]);
				locationsToTry.add(tryMe.getResourcePath() + "_" + variantRemover[1]);
				locationsToTry.add(variantRemover[1] + "_" + tryMe.getResourcePath());
			}
		}

		for (String testLocation : locationsToTry)
		{
			if (model.getParticleTexture().toString().contains("missingno"))
			{			
				ModelResourceLocation tryMeInstead = new ModelResourceLocation(new ResourceLocation(tryMe.getResourceDomain(), testLocation), "inventory");
				model = event.getModelManager().getModel(tryMeInstead);
			}
		}
		
		return model;
	}
	
}