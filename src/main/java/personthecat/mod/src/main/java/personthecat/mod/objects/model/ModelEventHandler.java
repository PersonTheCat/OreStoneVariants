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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.config.JsonReader;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.Reference;
import personthecat.mod.util.handlers.BlockStateGenerator;
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
		for (OreProperties property : PropertyGroup.PROPERTY_GROUP_MAP.get("minecraft").getProperties())
		{
			if (property.getName().equals("emerald_ore") || property.getName().equals("quartz_ore"))
			{
				createAndAddSprites(property, "assets/ore_stone_variants/textures/blocks/vanilla/" + blendedTexturePath + property.getName() + "_overlay");
			}
			
			else createAndAddSprites(property, "assets/ore_stone_variants/textures/blocks/vanilla/" + property.getName() + "_overlay");
		}

		List<String> modsWithGoodOverlays = new ArrayList<String>();
		if (Main.isBiomesOPlentyLoaded) modsWithGoodOverlays.add("biomesoplenty");
		if (Main.isGlassHeartsLoaded) modsWithGoodOverlays.add("glasshearts");
		
		for (String modName : modsWithGoodOverlays)
		{
			for (OreProperties property : PropertyGroup.PROPERTY_GROUP_MAP.get(modName).getProperties())
			{
				createAndAddSprites(property, "assets/ore_stone_variants/textures/blocks/" + modName + "/" + blendedTexturePath + property.getName() + "_overlay");
			}
		}
		
		List<String> modsWithoutGoodOverlays = new ArrayList<String>();
		if (Main.isIceAndFireLoaded) modsWithoutGoodOverlays.add("iceandfire");
		if (Main.isSimpleOresLoaded) modsWithoutGoodOverlays.add("simpleores");
		if (Main.isBaseMetalsLoaded) modsWithoutGoodOverlays.add("basemetals");
		
		for (String modName : modsWithoutGoodOverlays)
		{
			for (OreProperties property : PropertyGroup.PROPERTY_GROUP_MAP.get(modName).getProperties())
			{
				createAndAddSprites(property, "assets/ore_stone_variants/textures/blocks/" + modName + "/" + property.getName() + "_overlay");
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
		//We need to make sure the noblend variant gets created if it doesn't exist either, because we'll reuse it to create the dense sprites later.
		//This somehow writes the shaded/blended style texture to the /noblend/ directory, resulting in some ugly textures. Not sure why. 
		//Doesn't matter anyway, though. If we write one variant to resources.zip, that should theoretically supersede the texture in the jar. So, we always have to keep both variants.
		if ((Minecraft.class.getClassLoader().getResourceAsStream(location.replaceAll("/blend/", "/noblend/")) == null) && !location.contains("blend/"))
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
				String[] nameFinder = location.split("/");
				String fileName = nameFinder[nameFinder.length - 1];
				
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
			String[] nameFinder = location.split("/");
			String blockName = nameFinder[nameFinder.length - 1];
			
			TextureAtlasSprite sprite = event.getMap().registerSprite(new ResourceLocation(Reference.MODID, location.replaceAll("assets/ore_stone_variants/textures/", "")));
			OVERLAY_SPRITE_MAP.put(blockName.replaceAll("_overlay", ""), sprite);
		}
		
		failBackground = Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(new ResourceLocation(Reference.MODID, "blocks/background_finder"));
	}
	
	//This is where it gets gross. Turn back while you still can. 
	
	//Reusing SimpleModelBuilder instead of creating a new IModel implementation. Sorry. I may do that later, if I have the time.
	@SubscribeEvent
	@SideOnly(value = Side.CLIENT)
	public static void onModelBakeEvent(final ModelBakeEvent event) throws IOException
	{				
		for (IBlockState state : BlockInit.BLOCKSTATES)
		{
			BlockStateGenerator.State variant = BlockInit.BLOCKSTATE_MAP.get(state);
			IBlockState targetBlockState = variant.getBackgroundBlockState();
			ModelResourceLocation backgroundModelLocation = variant.getBackgroundModelLocation();
			IBakedModel oldModel = modelGuesser(event, backgroundModelLocation);
			
			//Background
			TextureAtlasSprite overlay = OVERLAY_SPRITE_MAP.get(state.getBlock().getRegistryName().getResourcePath().replaceAll("lit_", "").replaceAll("_quark", ""));
			overlay = overlay == null ? failBackground : overlay;
			
			boolean overrideShade = ConfigFile.shadeOverrides.contains(state.getBlock().getRegistryName().getResourcePath() + "_" + variant.getName());
			DynamicModelBaker baker = new DynamicModelBaker();
			IBakedModel newModel = baker.bakeDynamicModel(false, overrideShade, targetBlockState, oldModel, overlay);
			
			//New block
			event.getModelRegistry().putObject(new ModelResourceLocation(state.getBlock().getRegistryName(), "variant=" + variant.getName()), newModel);
			event.getModelRegistry().putObject(new ModelResourceLocation(state.getBlock().getRegistryName() + "_" + variant.getName(), "inventory"), newModel);
		}
		
		for (IBlockState state : BlockInit.DYNAMIC_BLOCKSTATES)	
		{
			String oreType = BlockInit.DYNAMIC_BLOCKSTATES_PROPERTY_MAP.get(state).getName();
			
			int i = BlockInit.DYNAMIC_BLOCKSTATES_NUMBER_MAP.get(state);
			String fullName = ConfigInterpreter.getFullEnumeratedName(i);
			String[] nameTester = fullName.split("_");
			String realName = fullName.contains("_ore") ? fullName : fullName.replaceAll(nameTester[0], oreType);
			
			//Background
			IBlockState targetBlockState = ConfigInterpreter.getBackgroundBlockState(i);
			ModelResourceLocation backgroundModelLocation = ConfigInterpreter.getBackgroundModelLocation(i);
			DynamicModelBaker baker = new DynamicModelBaker();
			IBakedModel oldModel = modelGuesser(event, backgroundModelLocation);
			
			//New block
			TextureAtlasSprite overlay = OVERLAY_SPRITE_MAP.get(oreType.replaceAll("lit_", ""));
			overlay = overlay == null ? failBackground : overlay;			
			boolean overrideShade = ConfigFile.shadeOverrides.contains(realName);
			IBakedModel newModel = baker.bakeDynamicModel(false, overrideShade, targetBlockState, oldModel, overlay);
			
			event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, realName), "normal"), newModel);
			event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, realName), "inventory"), newModel);
			
			//Lit redstone ore is not added to the list.
			if (fullName.contains("redstone"))
			{
				event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, "lit_" + realName), "normal"), newModel);
				event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(Reference.MODID, "lit_" + realName), "inventory"), newModel);
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
