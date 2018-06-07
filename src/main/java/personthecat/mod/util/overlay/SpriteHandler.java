package personthecat.mod.util.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import personthecat.mod.util.FileTools;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.ZipTools;

//Thanks to pupnewfster for writing the original version of this class for me!
public class SpriteHandler
{
	public static void createOverlay(String backgroundFile, String imageFile, String inThisLocation)
    {		
    	System.out.println("going to create an overlay from this image: " + imageFile);
		
		Color[][] image = getColorsFromImage(getImageFromFile(imageFile));
		BufferedImage originalBackground = getImageFromFile(backgroundFile);
		originalBackground = IMGTools.scaleImage(originalBackground, image.length, image[0].length);
		Color[][] background = getColorsFromImage(originalBackground);
        
    	//Was able to load
    	if ((image != null) && (background != null) && (image.length == background.length))
    	{

    		Color[][] overlayNormal = OverlayExtractor.extractBlendedOverlay(background, image);
    		
    		Color[][] overlayBlended = OverlayExtractor.extractNormalOverlay(background, image);
    		
    		try
			{
				File tempNormal = File.createTempFile("overlay", ".png");
				File tempBlended = File.createTempFile("overlay_blended", ".png");
				tempNormal.deleteOnExit();
				tempBlended.deleteOnExit();
				
				writeImageToFile(getImageFromColors(overlayNormal), tempNormal.getPath());
				writeImageToFile(getImageFromColors(overlayBlended), tempBlended.getPath());
				
				String blendedLocation = FileTools.getBlendedPath(inThisLocation);
				
				System.out.println("the blended version will go here: " + blendedLocation);
				
				ZipTools.copyToZip(inThisLocation, tempNormal, ZipTools.RESOURCE_PACK);
				ZipTools.copyToZip(blendedLocation, tempBlended, ZipTools.RESOURCE_PACK);
				
	    		testForAndCopyMcmeta(imageFile, inThisLocation);
	    		testForAndCopyMcmeta(imageFile, blendedLocation);
			}
    		
    		catch (IOException e) {System.err.println("Error: Could not create temporary images. Can't write overlays to zip.");}
    	}
    }

    public static void createDense(String imageFile)
    {    	
    	Color[][] colors = getColorsFromImage(getImageFromFile(imageFile));
        String oreName = NameReader.getOreFromPath(imageFile);
        imageFile = imageFile.replaceAll(oreName, "dense_" + oreName);
        imageFile = FileTools.getNormalPath(imageFile);
    	
    	//Was able to load
    	if (colors != null)
    	{
			try
			{
				File temp = File.createTempFile("overlay_dense", ".png");
				temp.deleteOnExit();
				
				BufferedImage denseImage = getImageFromColors(IMGTools.shiftImage(colors));
				
				ZipTools.copyToZip(imageFile, temp, ZipTools.RESOURCE_PACK);
			}
			
			catch (IOException e) {System.err.println("Error: could not create temp file. Cannot write dense image.");}
    	}
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
			try
			{
				image = ImageIO.read(ZipTools.getInputStreamFromZip(ZipTools.RESOURCE_PACK, file));
			}
			
			catch (IOException e2) {/*Error would already be reported by ZipTools--return null.*/}
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
    		String fileName = NameReader.getOreFromPath(inThisLocation);
    		File temp = File.createTempFile("current", ".mcmeta");
    		temp.deleteOnExit();
    		
    		InputStream copyMe = Minecraft.class.getClassLoader().getResourceAsStream(forImage + ".mcmeta");
    		FileOutputStream output = new FileOutputStream(temp.getPath());
    		
    		FileTools.copyStream(copyMe, output, 1024);
    		ZipTools.copyToZip(inThisLocation + ".mcmeta", temp, ZipTools.RESOURCE_PACK);
    		
    		//Gonna go ahead and copy this for the dense overlay, as well. Not the most organized location to do that, but definitely the easiest.
    		ZipTools.copyToZip(inThisLocation.replaceAll(fileName, "dense_" + fileName) + ".mcmeta", temp, ZipTools.RESOURCE_PACK);
    		
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
		 * Decides which algorithm to use. If one misses good pixels found in the other,
		 * adds those pixels. If any exclusive pixels are clearly bad, removes them.
		 * 
		 * Note: for all overlays included in the mod where normal stone is the background, 
		 * this only fixes two images. I'm not sure if it's actually a good solution, but
		 * it does fix those two overlays.
		 */
		private static Color[][] extractNormalOverlay(Color[][] background, Color[][] image)
		{			
			int w = image.length, h = image[0].length;
			
			//Getting a single color to avoid issues with frames and ores with no equivalent background.
			Color bgColor = IMGTools.getAverageColor(background);
			
			Color[][] algorithm1 = algorithm1FromSD(bgColor, image, IMGTools.getChannelAverage(IMGTools.getStandardDeviation(image)));			
			Color[][] algorithm2 = algorithm2(bgColor, image, getComparisonColors(background, image));
			
			/*
			 * The exclusives from algorithm1's output are the most useful;
			 * they tend to either have pixels that algorithm2's output is missing
			 * (as 2's problems are usually the result of not including enough pixels)
			 * or they just have too many pixels (i.e. 1's problems are usually the
			 * result of including too many pixels. The opposite is not true. We can
			 * use this information to decide when to add extra pixels to algorithm2's
			 * output or when to remove pixels from algorithm1's output.
			 */
			 
			Color[][] alg1Exclusives = IMGTools.createBlankImage(w, h);
			
			for (int x = 0; x < w; x++)
			{
				for (int y = 0; y < h; y++)
				{
					if (algorithm1[x][y].getAlpha() > 127 && algorithm2[x][y].getAlpha() < 127)
					{
						alg1Exclusives[x][y] = algorithm1[x][y];
					}
				}
			}
			
			Double alg1ExclusivesDifferenceFromBG = new Double(IMGTools.getAverageDifferenceFromColor(bgColor, alg1Exclusives));
			Double noDifference = new Double(0.4901960295198231);

			if (!alg1ExclusivesDifferenceFromBG.equals(noDifference))
			{
				if (alg1ExclusivesDifferenceFromBG < 0.1) //These shouldn't be here.
				{
					algorithm1 = IMGTools.removePixelsUsingMask(algorithm1, alg1Exclusives);
				}
				
				//This value is too picky. Thus, this entire function is not a good long-term solution.
				if (alg1ExclusivesDifferenceFromBG > 0.27) //These probably should have been kept.
				{
					algorithm2 = IMGTools.overlayImage(algorithm2, alg1Exclusives);
				}
			}
			
			//Pixels may sometimes be similar at this point, but this if statement 
			//still helps decide the best algorithm when they aren't.
			
			if (IMGTools.getGreatestDifference(image) > 0.45)
			{
				return algorithm2;
			}

			return algorithm1;
		}
		
		/**
		 * Retrieves normal algorithm. Applies effects in some cases.
		 */
		private static Color[][] extractBlendedOverlay(Color[][] background, Color[][] image)
		{
			Color[][] orePixels = extractNormalOverlay(background, image);
			Color[][] texturePixels = new Color[image.length][image[0].length];
			
			Color[][] textureMask = getColorsFromImage(getImageFromFile("assets/ore_stone_variants/textures/mask.png"));

			//Ore texture changes.
			if (orePixels.length > 16)
			{
				for (int i = 0; i < 2; i++) orePixels = IMGTools.removeLonePixels(orePixels);                //Remove all of the single pixels and small lines.			
				for (int i = 0; i < 3; i++) orePixels = IMGTools.removeOffColorPixelsFromBorders(orePixels); //Remove pixels that look too different from everything else.
				for (int i = 0; i < 2; i++) orePixels = IMGTools.reAddSurroundingPixels(orePixels, image);   //The ore textures are usually too small at this point. Expand them.
			}
			
			//Texture texture changes.
			texturePixels = IMGTools.convertToPushAndPull(image, background);           //Add transparency from getDifference() per-pixel using only black and white.
			texturePixels = IMGTools.removePixelsUsingMask(texturePixels, textureMask); //Use a vignette mask to lower the opacity from border pixels.
			
			return IMGTools.overlayImage(orePixels, texturePixels);
		}
		
		/**
		 * Determines which colors to forward into the extractor for comparison.
		 */
		private static Color[] getComparisonColors(Color[][] background, Color[][] image)
		{
			Color mostUniqueColor = IMGTools.getMostUniqueColor(background, image);		
			Color guessedColor = IMGTools.guessOreColor(background, image);
			
			if (guessedColor == null) return new Color[] {mostUniqueColor};
			
			Color blendedColor = IMGTools.getAverageColor(IMGTools.arrayToMatrix(mostUniqueColor,guessedColor));

			return new Color[] {mostUniqueColor, guessedColor, blendedColor};	
		}

		private static Color[][] algorithm1FromSD(Color background, Color[][] image, int SD)
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
		
		//The biggest problem with this algorithm is that the colors passed into it are often not geniune.
		//It uses the closest match, and if that match is closer than the background, it is accepted.
		//I have yet to realize a way to verify these matches any further.
		private static Color[][] algorithm2(Color background, Color[][] image, Color... colors)
		{
			Color[][] overlay = new Color[image.length][image[0].length];
			
			for (int x = 0; x < image.length; x++)
			{
				for (int y = 0; y < image[0].length; y++)
				{
					for (Color color : colors)
					{
						double differenceFromBackground = IMGTools.getDifference(image[x][y], background);
						double differenceFromColor = IMGTools.getDifference(image[x][y], color);
						
						if (differenceFromColor < differenceFromBackground)
						{
							overlay[x][y] = image[x][y];
							
							break;
						}
					}
					
					if (overlay[x][y] == null) overlay[x][y] = new Color(0, 0, 0, 0);
				}
			}
			
			return overlay;
		}
	}
}