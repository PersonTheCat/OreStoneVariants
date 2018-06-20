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
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.objects.blocks.BlockOresDynamic;
import personthecat.mod.objects.blocks.BlockOresEnumerated;
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
			
			if (Loader.isModLoaded(modName.replaceAll("vanilla", "minecraft")))
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
	
	//Reusing SimpleModelBuilder and placing these on ModelBakeEvent instead of creating a new IModel implementation. Sorry. I may do that later, if I have the time.
	@SubscribeEvent
	@SideOnly(value = Side.CLIENT)
	public static void onModelBakeEvent(final ModelBakeEvent event) throws IOException
	{
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			if (state.getBlock() instanceof BlockOresBase)
			{
				//Block info
				BlockOresBase asBOB = (BlockOresBase) state.getBlock();
				int meta = asBOB.getMetaFromState(state);
				
				//Name stuff
				String registryName = state.getBlock().getRegistryName().getResourcePath();
				String oreType = NameReader.getOre(registryName).replaceAll("lit_redstone_ore", "redstone_ore");
				
				//Target block
				IBlockState targetBlockState = asBOB.getBackgroundBlockState(meta);
				IBakedModel targetModel = modelGuesser(event, asBOB.getBackgroundModelLocation(meta));
				
				//New block
				ModelResourceLocation newModelLocationVariant = modelLocationShort(registryName, "normal");
				ModelResourceLocation newModelLocationInventory = modelLocationShort(registryName, "inventory");
				
				if (asBOB instanceof BlockOresEnumerated)
				{
					State variant = BlockInit.BLOCKSTATE_STATE_MAP.get(state);

					newModelLocationVariant = modelLocationShort(registryName, "variant=" + variant.getName());
					newModelLocationInventory = modelLocationShort(registryName + "_" + variant.getName(), "inventory");
				}
				
				//New model information
				TextureAtlasSprite overlay = OVERLAY_SPRITE_MAP.get(oreType) == null ? failBackground : OVERLAY_SPRITE_MAP.get(oreType);
				boolean overrideShade = ConfigFile.isShadeOverridden(registryName);
				
				//Bake new model
				IBakedModel newModel = new DynamicModelBaker().bakeDynamicModel(overrideShade, targetBlockState, targetModel, overlay, null);
				
				//Place new model
				event.getModelRegistry().putObject(newModelLocationVariant, newModel);
				event.getModelRegistry().putObject(newModelLocationInventory, newModel);
			}
			
			else System.err.println("Error: Could not cast to BlockOresBase. Model not placed correctly.");
		}
	}

	private static ModelResourceLocation modelLocationShort(String registryName, String id)
	{
		return new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName), id);
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