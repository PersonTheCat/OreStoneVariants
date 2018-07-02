package personthecat.mod.util.overlay;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.ZipTools;

//Thanks to pupnewfster for writing the original version of this class for me!

/*
 * To-do: This still desperately needs work. There are a lot of changes made which
 * only make a slight difference overall (aside from the "blended" texture features).
 * Overall, most changes are leftover from the 32x / Conquest texture extraction
 * program I was working on, which are stylistically much more complex than the
 * majority of 16x default textures.
 * 
 * In the following update, I will work on adding two more features:
 * 
 *  * One, for determining how closely the background texture matches portions of
 *    ore sprite, which can be used to more accurately fall back to the original
 *    algorithm (and which, again, makes more sense for most textures already
 *    used in the mod); and,
 *   
 *  * Two, for determining how sharp the contrast is from background to foreground.
 *    What this will do is allow me to keep all matching pixels and then more smoothly
 *    fade to the background, exactly relative to how distant the matching pixel is
 *    from its match, based on a threshold. Still not sure about how this will look.
 *    There may be some way to also blend the faded pixels with the matching color so
 *    as to make them look less like the background, but this is a bad idea in the
 *    event that the matched color is not accurate.
 *   
 * Once these are complete, I will reconsider removing some unnecessary pieces.
 * I am not so concerned with performance in this class as:
 * 
 *  * One, it only slightly affects the initial load time of the game; and,
 *  
 *  * Two, it only does so once, when the textures are initially generated.
 *  
 *  However, obviously there's too much going on here and it almost doesn't seem
 *  completely worth it, especially given the continued lack of support for the
 *  textures it was made for.
 */
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
			getColorsFromImage(getImageFromFile(denseLocation));
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
			overlay = getColorsFromImage(getImageFromFile(overlayLocation));
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
		Color[][] image = getColorsFromImage(getImageFromFile(imageFile));
		BufferedImage originalBackground = getImageFromFile(backgroundFile);
		Color[][] background = getColorsFromImage(IMGTools.scaleImage(originalBackground, image.length, image[0].length));
        
    	//Was able to load
    	if ((image != null) && (background != null) && (image.length == background.length))
    	{
    		int SD = IMGTools.getChannelAverage(IMGTools.getStandardDeviation(image));
    		
    		Color[][] overlayNormal = OverlayExtractor.extractNormalOverlay(IMGTools.getAverageColor(background), image, SD);
    		Color[][] overlayBlended = OverlayExtractor.extractBlendedOverlay(background, image, SD);
    		
    		writeImageToResourcePack(overlayNormal, FileTools.getNormalPath(inThisLocation));
    		writeImageToResourcePack(overlayBlended, FileTools.getBlendedPath(inThisLocation));
    		
    		testForAndCopyMcmeta(imageFile, FileTools.getNormalPath(inThisLocation));
    		testForAndCopyMcmeta(imageFile, FileTools.getBlendedPath(inThisLocation));
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
	
	public static BufferedImage getImageFromColors(Color[][] image)
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
    
    private static BufferedImage getImageFromFile(String file)
    {
    	BufferedImage image = null;
    	
		try
		{    			
			image = ImageIO.read(Minecraft.class.getClassLoader().getResourceAsStream(file));
		}

		catch (NullPointerException | IllegalArgumentException | IOException e) 
		{
			image = ZipTools.getImageFromZip(ZipTools.RESOURCE_PACK, file);
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
    
    private static void testForAndCopyMcmeta(String forImage, String inThisLocation)
    {
    	try
    	{
    		String fileName = NameReader.getEndOfPath(inThisLocation);
    		File temp = File.createTempFile("current", ".mcmeta");
    		temp.deleteOnExit();
    		
    		InputStream copyMe = Minecraft.class.getClassLoader().getResourceAsStream(forImage + ".mcmeta");
    		FileOutputStream output = new FileOutputStream(temp.getPath());
    		
    		FileTools.copyStream(copyMe, output, 1024);
    		ZipTools.copyToZip(inThisLocation + ".mcmeta", temp, ZipTools.RESOURCE_PACK);
    		
    		//Gonna go ahead and copy this for the dense overlay, as well. Not the most organized location to do that, but definitely the easiest.
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
			Color[][] texturePixels = new Color[image.length][image[0].length];
			Color[][] textureMask = getColorsFromImage(getImageFromFile("assets/ore_stone_variants/textures/mask.png"));
			Color[][] newBackground = IMGTools.addFramesToBackground(background, image); //Not the most efficient way to solve just this problem; has other uses.
			newBackground = IMGTools.fillColors(newBackground, IMGTools.getAverageColor(newBackground));

			//Ore texture changes.
			if (orePixels.length > 16)
			{
				for (int i = 0; i < 2; i++) orePixels = IMGTools.removeLonePixels(orePixels);                //Remove all of the single pixels and small lines.			
				for (int i = 0; i < 3; i++) orePixels = IMGTools.removeOffColorPixelsFromBorders(orePixels); //Remove pixels that look too different from everything else.
				for (int i = 0; i < 2; i++) orePixels = IMGTools.reAddSurroundingPixels(orePixels, image);   //The ore textures are usually too small at this point. Expand them.
			}
			
			//Texture texture changes.
			texturePixels = IMGTools.convertToPushAndPull(image, newBackground);           //Add transparency from getDifference() per-pixel using only black and white.
			texturePixels = IMGTools.removePixelsUsingMask(texturePixels, textureMask); //Use a vignette mask to lower the opacity from border pixels.
			
			return IMGTools.overlayImage(orePixels, texturePixels);
		}

		/**
		 * Currently only accepts the average color of the background. This may not
		 * necessarily be better than using per-pixel calculations, but it at the moment,
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