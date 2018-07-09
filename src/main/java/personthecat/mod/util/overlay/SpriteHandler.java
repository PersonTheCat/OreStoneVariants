package personthecat.mod.util.overlay;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository.Entry;
import net.minecraft.util.ResourceLocation;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.ZipTools;

//Thanks to pupnewfster for writing the original version of this class for me!

public class SpriteHandler
{
	/**
	 * Ignoring some intermediate steps, this ultimately just runs
	 * createNormalOverlays() in the normal and blended locations,
	 * ensuring that each path is correct using FileTools.
	 * 
	 * Creates a dense sprite using the loaded or created normal
	 * overlay.
	 */
	public static void createAllOverlays(String backgroundFile, String imageFile, String inThisLocation)
	{
		String normalLocation = FileTools.getNormalPath(inThisLocation);
		String blendedLocation = FileTools.getBlendedPath(inThisLocation);
		String denseLocation = FileTools.getDensePath(inThisLocation);

		Color[][] normalOverlay = testForAndCreateOverlay(backgroundFile, imageFile, normalLocation);

		if (imageFile != null && !imageFile.isEmpty())
		{
			testForAndCreateOverlay(backgroundFile, imageFile, blendedLocation);
		}
		
		try //Created dense images are not tested.
		{
			getColorsFromImage(loadImage(denseLocation));
		}
		
		catch (NullPointerException e)
		{
			createDense(normalOverlay, denseLocation);
		}
	}
	
	private static Color[][] testForAndCreateOverlay(String originalBG, String originalImage, String overlayLocation)
	{
		return testForAndCreateOverlay(originalBG, originalImage, overlayLocation, false);
	}
	
	/**
	 * First tries to locate an existing overlay. If it doesn't exist,
	 * it attempts to create the normal overlays and subsequently tries
	 * again. On the second try, it throws a null pointer exception
	 * to avoid an infinite loop.
	 */
	private static Color[][] testForAndCreateOverlay(String originalBG, String originalImage, String overlayLocation, boolean throwException)
	{
		Color[][] overlay = null;
		
		try
		{
			overlay = getColorsFromImage(loadImage(overlayLocation));
		}
		
		catch (NullPointerException e)
		{
			if (!throwException)
			{
				createNormalOverlays(originalBG, originalImage, overlayLocation);
				
				overlay = testForAndCreateOverlay(originalBG, originalImage, overlayLocation, true);
			}
			
			else
			{
				System.err.println("Error: Could not create normal overlay.");
				
				throw e;
			}
		}
		
		return overlay;
	}
	
	/**
	 * Loads images from paths. Searches for and reuses existing .mcmeta files.
	 */
	private static void createNormalOverlays(String backgroundFile, String imageFile, String inThisLocation)
    {
		Color[][] image = getColorsFromImage(loadImage(imageFile));
		BufferedImage originalBackground = loadImage(backgroundFile);
		Color[][] background = ensureSizeParity(getColorsFromImage(originalBackground), image);

    	//Was able to load
    	if ((image != null) && (background != null) && (image.length == background.length))
    	{
    		int SD = IMGTools.getChannelAverage(IMGTools.getStandardDeviation(image));
    		
    		Color[][] overlayNormal = OverlayExtractor.extractNormalOverlay(IMGTools.getAverageColor(background), image, SD);
    		Color[][] overlayBlended = OverlayExtractor.extractBlendedOverlay(background, image, SD);
    		
    		System.out.println("overlay extraction complete. Success? " + (overlayNormal != null));
    		
    		writeImageToResourcePack(overlayNormal, FileTools.getNormalPath(inThisLocation));
    		writeImageToResourcePack(overlayBlended, FileTools.getBlendedPath(inThisLocation));
    		
    		System.out.println("testing for and copying any mcmeta files...");
    		
    		testForAndCopyMcmeta(imageFile, inThisLocation);
    	}
    }
	
	/**
	 * For readability. Just writes the shifted image using IMGTools.
	 */
    private static void createDense(Color[][] originalOverlay, String densePath)
    {
    	writeImageToResourcePack(IMGTools.shiftImage(originalOverlay), densePath);
    }
	
	private static void writeImageToResourcePack(Color[][] overlay, String inThisLocation)
	{
		try
		{
			File tmp = File.createTempFile("overlay", ".png");
			tmp.deleteOnExit();
			
			writeImageToFile(getImageFromColors(overlay), tmp.getPath());

			ZipTools.copyToZip(inThisLocation, tmp, ZipTools.RESOURCE_PACK);
		}
		
		catch (IOException e) {System.err.println("Error: Could not create temporary images. Can't write overlays to zip.");}
	}
   	
	private static Color[][] getColorsFromImage(BufferedImage image)
	{
		int w = image.getWidth(), h = image.getHeight();
		
		Color[][] colors = new Color[w][h];
		
		for (int i = 0; i < w; i++)
		{
			for (int j = 0; j < h; j++)
			{
				colors[i][j] = new Color(image.getRGB(i, j), true);
			}
		}
		
		return colors;
	}
	
	private static BufferedImage getImageFromColors(Color[][] image)
	{
		int w = image.length, h = image[0].length;
		
		BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		for (int i = 0; i < w; i++)
		{
			for (int j = 0; j < h; j++)
			{
				bufferedImage.setRGB(i, j, image[i][j].getRGB());
			}
		}
		
		return bufferedImage;
	}
	
	/**
	 * Scales bg to width, repeats it for additional frames.
	 */
	private static Color[][] ensureSizeParity(Color[][] background, Color[][] image)
	{
		background = getColorsFromImage(IMGTools.scaleImage(getImageFromColors(background), image.length, image.length));
		background = IMGTools.addFramesToBackground(background, image);
		
		return background;
	}
    
	/**
	 * Order: Resource pack repository > jar files > resources.zip > crash
	 */
    private static BufferedImage loadImage(String file)
    {
    	BufferedImage image = null;
    	
    	if (ConfigFile.overlaysFromRP)
    	{
        	for (Entry resourcePack : Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries())
        	{
        		try
    			{
    				image = ImageIO.read(resourcePack.getResourcePack().getInputStream(FileTools.getResourceLocationFromPath(file)));
    			}
        		
    			catch (NullPointerException | IllegalArgumentException | IOException ignored) {continue;}
        	}
    	}

    	if (image == null)
    	{
    		try
    		{			
    			image = ImageIO.read(Minecraft.class.getClassLoader().getResourceAsStream(file));
    		}

    		catch (NullPointerException | IllegalArgumentException | IOException e) 
    		{
    			image = ZipTools.getImageFromZip(ZipTools.RESOURCE_PACK, file);
    		}
    	}

		return image;
    }
    
	private static void writeImageToFile(BufferedImage image, String location)
	{
		try
		{
			File png = new File(location);
			
			ImageIO.write(image, "png", png);
		}
		
		catch (IOException e) {System.err.println("Error: Could not create image file.");}
	}
    
	/*
	 * To-do: Clean this up.
	 */
    private static void testForAndCopyMcmeta(String forImage, String inThisLocation)
    {
    	try
    	{
    		File temp = File.createTempFile("current", ".mcmeta");
    		temp.deleteOnExit();
    		
    		InputStream copyMe = Minecraft.class.getClassLoader().getResourceAsStream(forImage + ".mcmeta");

    		if (ConfigFile.overlaysFromRP)
    		{
        		for (Entry resourcePack : Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries())
        		{
        			ResourceLocation mcmeta = FileTools.getResourceLocationFromPath(forImage + ".mcmeta");

        			if (resourcePack.getResourcePack().resourceExists(mcmeta))
        			{
        				copyMe = resourcePack.getResourcePack().getInputStream(mcmeta);
        				
        				System.out.println("using mcmeta file for " + forImage + " from " + resourcePack.getResourcePackName());
        				
        				break;
        			}
        		}
    		}
    		
    		FileOutputStream output = new FileOutputStream(temp.getPath());
    		FileTools.copyStream(copyMe, output, 1024);
    		
    		ZipTools.copyToZip(FileTools.getNormalPath(inThisLocation) + ".mcmeta", temp, ZipTools.RESOURCE_PACK);
    		ZipTools.copyToZip(FileTools.getBlendedPath(inThisLocation) + ".mcmeta", temp, ZipTools.RESOURCE_PACK);
    		ZipTools.copyToZip(FileTools.getDensePath(inThisLocation) + ".mcmeta", temp, ZipTools.RESOURCE_PACK);
    		
    		copyMe.close();
    		output.close();
    	}
    	
    	catch (NullPointerException | IOException ignored) {}
    }
    
	private static class OverlayExtractor
	{		
		//A texture's SD and optimal threshold are highly correlated (r = 0.9174)
		//Average difference is slightly higher (r = 0.9230). 
		//Try correlating with the highest channel from SD instead. 
		private static final double SD_THRESHOLD_RATIO = 0.0055; 
		
		/**
		 * Retrieves normal algorithm. Applies effects in some cases.
		 */
		private static Color[][] extractBlendedOverlay(Color[][] background, Color[][] image, int SD)
		{
			Color[][] orePixels = extractNormalOverlay(IMGTools.getAverageColor(background), image, SD);
			Color[][] textureMask = getColorsFromImage(loadImage("assets/ore_stone_variants/textures/mask.png"));			
			textureMask = ensureSizeParity(textureMask, image);
			
			background = ensureSizeParity(background, image);
			background = IMGTools.fillColors(background, IMGTools.getAverageColor(background));

			//Ore texture changes.
			if (orePixels.length > 16) applyOreEffects(orePixels, image);
			
			return applyTextureEffects(orePixels, background, image, textureMask);
		}
		
		private static Color[][] applyOreEffects(Color[][] oreOverlay, Color[][] originalImage)
		{
			for (int i = 0; i < 2; i++) oreOverlay = IMGTools.removeLonePixels(oreOverlay);                      //Remove all of the single pixels and small lines.			
			for (int i = 0; i < 3; i++) oreOverlay = IMGTools.removeOffColorPixelsFromBorders(oreOverlay);       //Remove pixels that look too different from everything else.
			for (int i = 0; i < 2; i++) oreOverlay = IMGTools.reAddSurroundingPixels(oreOverlay, originalImage); //The ore textures are usually too small at this point. Expand them.
			
			return oreOverlay;
		}
		
		private static Color[][] applyTextureEffects(Color[][] overlay, Color[][] background, Color[][] image, Color[][] mask)
		{
			Color[][] texturePixels = new Color[image.length][image[0].length];
			
			texturePixels = IMGTools.convertToPushAndPull(image, background);    //Add transparency from getDifference() per-pixel using only black and white.
			texturePixels = IMGTools.removePixelsUsingMask(texturePixels, mask); //Use a vignette mask to lower the opacity from border pixels.
			
			return IMGTools.overlayImage(overlay, texturePixels);
		}

		/**
		 * Currently only accepts the average color of the background. This may not
		 * necessarily be better than using per-pixel calculations, but at the moment,
		 * it does produce better results, which I suppose could be because of the 
		 * SD_THRESHOLD_RATIO being tailored to this setup. Not sure.
		 */
		private static Color[][] extractNormalOverlay(Color background, Color[][] image, int SD)
		{
			int w = image.length, h = image[0].length;

			Color[][] overlay = new Color[w][h];

			double threshold = SD * SD_THRESHOLD_RATIO; //Threshold used for separating ore from background.

			for (int x = 0; x < w; x++)
			{
				for (int y = 0; y < h; y++)
				{
					overlay[x][y] = IMGTools.getOrePixel(image[x][y], background, threshold);	
				}
			}
			
			return overlay;
		}
	}
}