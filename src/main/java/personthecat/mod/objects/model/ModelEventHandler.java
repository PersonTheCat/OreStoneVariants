package personthecat.mod.objects.model;

import static personthecat.mod.Main.logger;
import static personthecat.mod.util.CommonMethods.formatStateName;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.config.Cfg;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.Reference;
import personthecat.mod.util.ZipTools;
import personthecat.mod.util.handlers.RegistryHandler;
import personthecat.mod.util.overlay.SpriteHandler;

/**
 * Here's a project for the future:
 * 
 * If we can get all models to load correctly on postInit
 * *without* refreshing all resources, we can start retrieving
 * block models directly from BlockRenderDispatcher. This
 * means we don't have to worry about whether whether our
 * models are valid. It also means we can dynamically setup
 * all OreProperties with full support for texture generation.
 * I've gotten this to work somewhat, but I have no idea how 
 * to do with without calling Minecraft#refreshResources and 
 * really don't want to slow down the startup process as
 * much as it takes. :c
 */
@EventBusSubscriber
public class ModelEventHandler
{
	public static TextureAtlasSprite failBackground;
	
	private static boolean blendedTextureOverride = false;
	
	@SideOnly(value = Side.CLIENT)
	public static void createAndRegisterResourcePack()
	{
		ZipTools.testForResourcePack();
		
		createAndAddSprites();
		
		RegistryHandler.onRegisterNewResourcesBadly();
	}
	
	@SideOnly(value = Side.CLIENT)
	private static void createAndAddSprites()
	{
		for (OreProperties properties : OreProperties.getOrePropertyRegistry())
		{
			if (properties.inUse())
			{
				SpriteHandler.createAllOverlays(properties.getBackgroundMatcher(), properties.getOriginalTexture(), properties.getOverlayPath() + ".png");
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(value = Side.CLIENT)
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		testForRPSettings();

		for (OreProperties properties : OreProperties.getOrePropertyRegistry())
		{
			if (properties.inUse())
			{
				ResourceLocation location = properties.getOverlayResourceLocation();
				
				if (blendedTextureOverride)
				{
					location = new ResourceLocation(Reference.MODID, FileTools.getNormalPath(location.getResourcePath()));
				}

				properties.setTexture(event.getMap().registerSprite(location));
				
				if (Cfg.denseCat.generalDenseCat.denseVariants)
				{
					ResourceLocation denseLocation = new ResourceLocation(Reference.MODID, FileTools.getDensePath(location.getResourcePath()));

					properties.setDenseTexture(event.getMap().registerSprite(denseLocation));
				}
			}
		}

		failBackground = event.getMap().registerSprite(new ResourceLocation(Reference.MODID, "blocks/background_finder"));
	}
	
	private static ModelBakeEvent event;
	
	@SubscribeEvent()
	@SideOnly(value = Side.CLIENT)
	public static void onModelBakeEvent(final ModelBakeEvent event) throws IOException
	{
		ModelEventHandler.event = event;

		//test for BlockRendererDispatcher() != null if late setup is figured out.
		placeAllModels(false);
	}
	
	@SideOnly(value = Side.CLIENT)
	public static void placeAllModels(boolean post)
	{
		logger.info("Placing all models.");

		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			if (state.getBlock() instanceof BlockOresBase)
			{
				BlockOresBase asBOB = (BlockOresBase) state.getBlock();
				int meta = asBOB.getMetaFromState(state);
				OreProperties properties = asBOB.ensureNotLit().getProperties();
				
				try
				{
					IBakedModel newModel = new DynamicModelBaker().bakeDynamicModel(
						Cfg.isShadeOverridden(asBOB.getOriginalName()),									//overrideShade
						asBOB.getBackgroundBlockState(meta),											//targetBlockState
						modelGuesser(asBOB.getBackgroundModelLocation(meta)),							//targetModel
						asBOB.isDenseVariant() ? properties.getDenseTexture() : properties.getTexture(),//overlay
						null																			//forcedTexture
					);
					
					placeModels(asBOB, state, newModel, meta);
				}
				catch (IOException e) { logger.warn("Unable to generate new model. Model not placed correctly."); }
			}
			else logger.warn("Error: Could not cast to BlockOresBase. Model not placed correctly.");
		}
		
		if (post) event.getModelManager().getBlockModelShapes().reloadModels(); //Does not load item models?
	}
	
	private static ModelResourceLocation modelLocationShort(String registryName, String id)
	{
		return new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName), id);
	}
	
	private static void placeModels(BlockOresBase ore, IBlockState state, IBakedModel model, int meta)
	{		
		ModelResourceLocation mrl = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(ore).get(state);
		
		event.getModelRegistry().putObject(mrl, model);
		
		String modelName = ore.getModelName(meta);
		
		event.getModelRegistry().putObject(modelLocationShort(modelName, "inventory"), model);
	}
	
	/*
	 * This is the method I'd like to completely get rid of.
	 */
	private static IBakedModel modelGuesser(ModelResourceLocation tryMe)
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

		//Try all combinations of registry name + variant tag.
		for (String testLocation : locationsToTry)
		{
			if (model.getParticleTexture().toString().contains("missingno"))
			{
				ModelResourceLocation tryMeInstead = new ModelResourceLocation(new ResourceLocation(tryMe.getResourceDomain(), testLocation), "inventory");
				model = event.getModelManager().getModel(tryMeInstead);
			}
		}
		
		//Try just the registry name. Do the least specific option last.
		if (model.getParticleTexture().toString().contains("missingno"))
		{
			ModelResourceLocation tryMeFinally = new ModelResourceLocation(new ResourceLocation(tryMe.getResourceDomain(), tryMe.getResourcePath()), "inventory");
			model = event.getModelManager().getModel(tryMeFinally);
		}
		
		return model;
	}
	
	/**
	 * This will be updated to use regex if more options are added.
	 */
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
}