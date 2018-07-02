package personthecat.mod.objects.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.Reference;
import personthecat.mod.util.ZipTools;
import personthecat.mod.util.handlers.BlockStateGenerator.State;
import personthecat.mod.util.handlers.RegistryHandler;
import personthecat.mod.util.overlay.SpriteHandler;

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
			if (properties.isDependencyMet())
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
			if (properties.isDependencyMet())
			{
				ResourceLocation location = properties.getOverlayResourceLocation();
				
				if (blendedTextureOverride)
				{
					location = new ResourceLocation(Reference.MODID, FileTools.getNormalPath(location.getResourcePath()));
				}

				properties.setTexture(event.getMap().registerSprite(location));
				
				if (ConfigFile.denseVariants)
				{
					ResourceLocation denseLocation = new ResourceLocation(Reference.MODID, FileTools.getDensePath(location.getResourcePath()));

					properties.setDenseTexture(event.getMap().registerSprite(denseLocation));
				}
			}
		}

		failBackground = event.getMap().registerSprite(new ResourceLocation(Reference.MODID, "blocks/background_finder"));
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
				BlockOresBase asBOB = (BlockOresBase) state.getBlock();
				int meta = asBOB.getMetaFromState(state);
				OreProperties properties = asBOB.ensureNotLit().getProperties();

				IBakedModel newModel = new DynamicModelBaker().bakeDynamicModel(
					ConfigFile.isShadeOverridden(asBOB.getOriginalName()),							//overrideShade
					asBOB.getBackgroundBlockState(meta),											//targetBlockState
					modelGuesser(event, asBOB.getBackgroundModelLocation(meta)),					//targetModel
					asBOB.isDenseVariant() ? properties.getDenseTexture() : properties.getTexture(),//overlay
					null																			//forceTexture
					);
				
				placeModels(event, asBOB, state, asBOB.getOriginalName(), newModel);
			}
			
			else System.err.println("Error: Could not cast to BlockOresBase. Model not placed correctly.");
		}
	}
	
	private static void placeModels(ModelBakeEvent event, BlockOresBase ore, IBlockState state, String originalName, IBakedModel model)
	{
		if (ore.hasEnumBlockStates())
		{
			State variant = BlockInit.BLOCKSTATE_STATE_MAP.get(state);
			
			event.getModelRegistry().putObject(modelLocationShort(originalName, "variant=" + variant.getName()), model);
			event.getModelRegistry().putObject(modelLocationShort(originalName + "_" + variant.getName(), "inventory"), model);
		}
		
		else
		{
			event.getModelRegistry().putObject(modelLocationShort(originalName, "normal"), model);
			event.getModelRegistry().putObject(modelLocationShort(originalName, "inventory"), model);
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