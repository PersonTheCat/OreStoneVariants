package personthecat.mod.objects.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.config.JsonReader;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.DefaultProperties;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.handlers.BlockStateGenerator.State;
import personthecat.mod.util.handlers.RegistryHandler;
import personthecat.mod.util.handlers.SpriteHandler;

//Avert your eyes from this class... Nothing to see here.
public class ModelEventHandler
{
	public static TextureAtlasSprite failBackground;
	private static String blendedTexturePath = ConfigFile.blendedTextures ? "blend/" : "noblend/";
	
	private static final List<String> OVERLAY_LOCATION_REGISTRY = new ArrayList<String>();
	private static final Map<String, TextureAtlasSprite> OVERLAY_SPRITE_MAP = new HashMap<String, TextureAtlasSprite>();
	
	@SideOnly(value = Side.CLIENT)
	public static void registerTextureLocations()
	{		
		for (OreProperties property : OreProperties.ORE_PROPERTY_REGISTRY)
		{
			String modName = NameReader.getMod(property.getName());
			
			if ((PropertyGroup.PROPERTY_GROUP_MAP.get(modName.replaceAll("vanilla", "minecraft")).getConditions()))
			{
				if (property.getHasBuiltInTextures()) createAndAddSprites(property, "assets/ore_stone_variants/textures/blocks/" + modName + "/" + blendedTexturePath + property.getName() + "_overlay");
				
				else createAndAddSprites(property, "assets/ore_stone_variants/textures/blocks/" + modName + "/" + property.getName() + "_overlay");
			}
		}
		
		for (String name : JsonReader.NEW_PROPERTY_NAMES)
		{			
			createAndAddSprites(OreProperties.propertiesOf(name), "assets/ore_stone_variants/textures/blocks/" + name + "_overlay");
		}
		
		if (ConfigFile.denseVariants) createDenseSpritesFromList();
		
		RegistryHandler.onRegisterNewResourcesBadly();
	}
	
	private static void createAndAddSprites(OreProperties property, String location)
	{				
		//This somehow writes the shaded/blended style texture to the /noblend/ directory, resulting in some ugly textures. Not sure why. 
		//Doesn't matter anyway, though. If we write one variant to resources.zip, that should theoretically supersede the texture in the jar. 
		//So, we always have to keep both variants when variants are available..
		if ((Minecraft.class.getClassLoader().getResourceAsStream(location.replaceAll("/blend/", "/noblend/") + ".png") == null))
		{			
			SpriteHandler.createOverlay(property.getBackgroundMatcher(), property.getOriginalTexture(), location.replaceAll("/blend/", "/noblend/") + ".png");
		}
		
		OVERLAY_LOCATION_REGISTRY.add(location);
	}
	
	@SideOnly(value = Side.CLIENT)
	private static void createDenseSpritesFromList()
	{
		List<String> tempLocationClone = new ArrayList<String>();
		tempLocationClone.addAll(OVERLAY_LOCATION_REGISTRY);
		
		for (String location : tempLocationClone)
		{
			if (!location.contains("lit_"))
			{
				location = location.replaceAll("/blend/", "/noblend/");
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
		for (String location : OVERLAY_LOCATION_REGISTRY)
		{
			String blockName = NameReader.getOreFromPath(location);
			
			TextureAtlasSprite sprite = event.getMap().registerSprite(new ResourceLocation(Reference.MODID, location.replaceAll("assets/ore_stone_variants/textures/", "")));
			OVERLAY_SPRITE_MAP.put(blockName.replaceAll("_overlay", ""), sprite);
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
			//Name stuff
			String registryName = state.getBlock().getRegistryName().getResourcePath();
			String oreType = BlockInit.BLOCK_PROPERTY_MAP.get(state.getBlock()).getName();
			oreType = NameReader.isDense(state.getBlock()) ? "dense_" + oreType : oreType;
			
			//Target block
			IBlockState targetBlockState;
			ModelResourceLocation backgroundModelLocation;
			
			//New block
			ModelResourceLocation newModelLocationVariant, newModelLocationInventory;
			
			if (!NameReader.isDynamic(state.getBlock()))
			{
				//Name stuff
				State variant = BlockInit.BLOCKSTATE_STATE_MAP.get(state);
				
				//Target block
				targetBlockState = variant.getBackgroundBlockState();
				backgroundModelLocation = variant.getBackgroundModelLocation();
				
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
			TextureAtlasSprite overlay = OVERLAY_SPRITE_MAP.get(oreType) == null ? failBackground : OVERLAY_SPRITE_MAP.get(oreType);
			boolean overrideShade = ConfigFile.shadeOverrides.contains(newModelLocationInventory.getResourcePath());
			
			//Bake new model
			DynamicModelBaker baker = new DynamicModelBaker();
			IBakedModel newModel = baker.bakeDynamicModel(overrideShade, targetBlockState, targetModel, overlay);
			
			//Place new model
			event.getModelRegistry().putObject(newModelLocationVariant, newModel);
			event.getModelRegistry().putObject(newModelLocationInventory, newModel);
			
			if (NameReader.isDynamic(state.getBlock()) && NameReader.isLit(state.getBlock()))
			{
				event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName), "normal"), newModel);
				event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, registryName), "inventory"), newModel);
			}
		}
	}
	
	//Damn. I need to try using BlockRenderDispatcher instead. I thought I tried that...
	private static IBakedModel modelGuesser(ModelBakeEvent event, ModelResourceLocation tryMe)
	{
		IBakedModel model = event.getModelManager().getModel(tryMe);
		String[] multiVariantSeparator = tryMe.getVariant().split(",");
		
		List<String> locationsToTry = new ArrayList<String>();
		
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
