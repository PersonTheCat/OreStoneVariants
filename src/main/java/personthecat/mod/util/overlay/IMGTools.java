package personthecat.mod.util.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static personthecat.mod.Main.logger;

public class IMGTools
{
	private static final double TEXTURE_SHARPEN_RATIO = 2.3;         //Multiplies the alpha levels for push and pull / overlay background textures.
	private static final double COLOR_RANGE_MAX_DIFFERENCE = 0.10;   //Used for separating colors into ranges.
	private static final double BACKGROUND_MATCHER_THRESHOLD = 0.25; //If a higher percent of the images is the same, the background matches.
	private static final int TRANSPARENCY_THRESHOLD = 17;            //Pixels with lower alpha levels are considered transparent.
	private static final int OPACITY_THRESHOLD = 50;                 //Pixels with higher alpha levels are considered opaque.
	private static final int SOLID_THRESHOLD = 120;                  //Pixels with higher alpha levels probably shouldn't have been removed...	
	
	/**
	 * Only width passed to this function because some images may have multiple frames.
	 */
	public static BufferedImage scaleImage(BufferedImage image, int newW, int newH)
	{
		BufferedImage scaledImage = new BufferedImage(newW, newH, image.getType());
		
		Graphics2D graphics = scaledImage.createGraphics();
		graphics.drawImage(image, 0, 0, newW, newH, null);
		graphics.dispose();
		
		return scaledImage;
	}
	
	/**
	 * Helps to avoid NullPointerExceptions.
	 */
	public static Color[][] createBlankImage(int w, int h)
	{
		Color[][] image = new Color[w][h];
		
		for (int x = 0; x < w; x++)
		{
			for (int y = 0; y < h; y++)
			{
				image[x][y] = new Color(0, 0, 0, 0);
			}
		}
		
		return image;
	}
	
	/**
	 * By pupnewfster, shifts the image in all cardinal directions
	 * per pixel, then mixes them.
	 */
    public static Color[][] shiftImage(Color[][] image)
    {
    	int w, h;
    	Color[][] shifted = new Color[w = image.length][h = image[0].length];
		int frames = h / w;
		
		//Does not divide nicely
		if (1.0 * h / w != frames) return null;

		for (int f = 0; f < frames; f++)
			for (int x = 0; x < w; x++)
				for (int y = 0; y < w; y++)
				{
					int imageY = f * w + y;
					
					shifted[x][imageY] = getAverageFromArray(image[x][imageY], fromIndex(image, x - 1, imageY, f), fromIndex(image, x + 1, imageY, f),
							fromIndex(image, x, imageY - 1, f), fromIndex(image, x, imageY + 1, f));//self, left, right, up, down
				}
		
        return shifted;
    }
    
    private static Color fromIndex(Color[][] image, int x, int y, int frame)
    {
        int w = image.length;

		return ((x < 0) || (y < frame * w) || (x >= w) || (y >= (frame + 1) * w) || (image[x][y].getAlpha() == 34)) ? new Color(0, 0, 0, 0) : image[x][y];
    }
    
	/**
	 * Replaces all pixels with the same color, (a)rgb.
	 */
	public static Color[][] fillColors(Color[][] image, Color rgb)
	{
		int w = image.length, h = image[0].length;
		
		for (int i = 0; i < w; i++)
		{
			for (int j = 0; j < h; j++)
			{
				image[i][j] = rgb;
			}
		}
		
		return image;
	}
	
	/**
	 * Repeats the background image until it is the height of the foreground.
	 */
	public static Color[][] addFramesToBackground(Color[][] background, Color[][] image)
	{
		int w = background.length, h = background[0].length, nh = image[0].length;
		int frames = nh / h;
		
		Color[][] newBackground = new Color[w][h * frames];

		for (int x = 0; x < w; x++)
		{
			for (int y = 0; y < h; y++)
			{
				for (int i = 0; i < frames; i++)
				{
					newBackground[x][(h * i) + y] = background[x][y];
				}
			}
		}
		
		return newBackground;
	}

	public static Color getAverageColor(Color[][] image)
	{
		return getAverageFromArray(matrixToArray(image));
	}
	
    private static Color getAverageFromArray(Color... colors)
    {
        int r = 0, g = 0, b = 0; //, a = 0;
        int pixelCount = 0;
        
        for (Color color : colors)
        {
            if (color.getAlpha() > OPACITY_THRESHOLD)
            {
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
                //a += color.getAlpha();
                
                pixelCount++;
            }
        }
        
        if (pixelCount == 0) return new Color(0, 0, 0, 0);
        
        r /= pixelCount;
        g /= pixelCount;
        b /= pixelCount;
        //a /= pixelCount;
        
        //return a < TRANSPARENCY_THRESHOLD ? new Color(0, 0, 0, 0) : new Color(r, g, b, a);
        
        return new Color(r, g, b);
    }
    
	public static double getAverageDifferenceFromColor(Color color, Color[][] image)
	{
		double averageDifference = 0.0;
		int pixelCount = 0;
		
		for (int x = 0; x < image.length; x++)
		{
			for (int y = 0; y < image[0].length; y++)
			{
				if (image[x][y].getAlpha() > TRANSPARENCY_THRESHOLD)
				{
					averageDifference += getDifference(color, image[x][y]);
					
					pixelCount++;
				}
			}
		}
		
		averageDifference /= pixelCount;
		
		return averageDifference;
	}
	
	/**
	 * For each pixel in the image, finds which color is the most different from its counterpart.
	 */
	public static Color getMostUniqueColor(Color[][] background, Color[][] image)
	{
		double greatestDifference = 0.0;
		Color mostUniqueColor = null;

		for (int x = 0; x < image.length; x++)
		{
			for (int y = 0; y < image[0].length; y++)
			{
				double difference = getDifference(image[x][y], background[x][y]);
				
				if (difference > greatestDifference)
				{
					greatestDifference = difference;
					
					mostUniqueColor = image[x][y];
				}
			}
		}
		
		return mostUniqueColor;
	}
	
	public static Color getMostCommonColor(Color[] color)
	{
		int highestCount = 0;
		
		Color mostCommonColor = color[0];
		
		for (int i = 0; i < color.length; i++)
		{
			int colorCount = 0;
			
			for (int j = 0; j < color.length; j++)
			{
				if (color[i] == color[j]) colorCount++;
			}
			
			if (colorCount > highestCount)
			{
				highestCount = colorCount;
				
				mostCommonColor = color[i];
			}
		}
		
		return mostCommonColor;
	}
	
	public static boolean doesBackgroundMatch(Color[][] background, Color[][] foreground)
	{
		int bx = background.length, by = background[0].length,
			fx = foreground.length, fy = foreground[0].length;
		
		if (!(bx == fx) && !(by == fy)) return false;
		
		double percentMatches = 0;
		
		for (int x = 0; x < bx; x++)
		{
			for (int y = 0; y < by; y++)
			{
				if (background[x][y].equals(foreground[x][y]))
				{
					percentMatches++;
				}
			}
		}
		
		percentMatches /= (bx * by);
		
		return percentMatches > BACKGROUND_MATCHER_THRESHOLD;
	}
    
	/**
	 * Used for equalizing color sums.
	 */
	public static Color[] matrixToArray(Color[][] colors)
	{
		Color[] newArray = new Color[colors.length * colors[0].length];
		int index = 0;
		
		for (int x = 0; x < colors.length; x++)
		{
			for (int y = 0; y < colors[0].length; y++)
			{				
				newArray[index] = colors[x][y];
				
				index++;
			}
		}
		
		return newArray;
	}
	
	/**
	 * Just used for getting the average of multiple colors more neatly.
	 */
	public static Color[][] arrayToMatrix(Color... colors)
	{
		Color[][] newMatrix = new Color[1][colors.length];
		
		for (int i = 0; i < colors.length; i++)
		{
			newMatrix[0][i] = colors[i];
		}
		
		return newMatrix;
	}
	
	public static Color getOrePixel(Color foreground, Color background, double threshold)
	{
		if (IMGTools.getDifference(foreground, background) > threshold) return foreground;
		
		return new Color(0, 0, 0, 0);
	}
	
	/**
	 * In 8 directions, if > 5 are alpha = 0, remove pixel. Does not include border pixels.
	 * Can be repeated to remove textures that are unusually narrow. Probably not wise for <32x textures. 
	 */
	public static Color[][] removeLonePixels(Color[][] image)
	{
		for (int x = 1; x < image.length - 1; x++)
		{
			for (int y = 1; y < image[0].length - 1; y++)
			{
				int alphaCount = getSurroundingAlphaCount(image, x, y);
				
				if (alphaCount > 5) image[x][y] = new Color(0, 0, 0, 0);
			}
		}
		
		return image;
	}
	
	/**
	 * Goes around the edge of the textures (on the borders between the pixels with full transparency and everything else)
	 * and removes them if they are too different from the average color. If the pixel is brown but most of the ore is blue, 
	 * it gets removed. If the background and the ore are pretty similar, nothing happens anyway. 
	 */
	public static Color[][] removeOffColorPixelsFromBorders(Color[][] image)
	{
		Color averageColor = getAverageColor(image);
		
		for (int x = 0; x < image.length; x++)
		{
			for (int y = 0; y < image[0].length; y++)
			{
				double difference = getDifference(image[x][y], averageColor);
				
				try
				{
					if ((getSurroundingAlphaCount(image, x, y) > 2) && difference > 0.25) //Make this threshold a variable? Use algorithm2?
					{
						image[x][y] = new Color(0, 0, 0, 0);
					}
				}
				
				//This means border pixels are removed.
				catch (IndexOutOfBoundsException ignored) {image[x][y] = new Color(0, 0, 0, 0);} 
			}
		}
		
		return image;
	}
	
	/**
	 * Using an image with clusters of non-alpha pixels, re-add the surrounding pixels at 50% opacity.
	 * Makes sure the entire ore textures are both included and blended to the background.
	 */
	public static Color[][] reAddSurroundingPixels(Color[][] image, Color[][] original)
	{
		for (int x = 1; x < image.length - 1; x++)
		{
			for (int y = 1; y < image[0].length - 1; y++)
			{
				if ((image[x][y].getAlpha() == 255) && (getSurroundingAlphaCount(image, x, y) < 4))
				{
					//Neighbor pixel's alpha < half ? original pixel w/ 2/3 opacity.
					image[x + 1][y] = testAlphaAndReplaceColor(image[x + 1][y], original[x + 1][y], 127, 200);
					image[x - 1][y] = testAlphaAndReplaceColor(image[x - 1][y], original[x - 1][y], 127, 200);
					image[x][y + 1] = testAlphaAndReplaceColor(image[x][y + 1], original[x][y + 1], 127, 200);
					image[x][y - 1] = testAlphaAndReplaceColor(image[x][y - 1], original[x][y - 1], 127, 200);
					
					//Diagonal pixel's alpha < half ? original pixel w/ quarter opacity.
					image[x + 1][y + 1] = testAlphaAndReplaceColor(image[x + 1][y + 1], original[x + 1][y + 1], 127, 63);
					image[x - 1][y - 1] = testAlphaAndReplaceColor(image[x - 1][y - 1], original[x - 1][y - 1], 127, 63);
					image[x + 1][y - 1] = testAlphaAndReplaceColor(image[x + 1][y - 1], original[x + 1][y - 1], 127, 63);
					image[x - 1][y + 1] = testAlphaAndReplaceColor(image[x - 1][y + 1], original[x - 1][y + 1], 127, 63);
				}
			}
		}
		
		return image;
	}
	
	/**
	 * Does not handle OutOfBounds exceptions. Must be factored into the loop.
	 */
	public static int getSurroundingAlphaCount(Color[][] image, int x, int y)
	{
		int alphaCount = 0;
		
		if (image[x + 1][y].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		if (image[x - 1][y].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		if (image[x][y + 1].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		if (image[x][y - 1].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		if (image[x + 1][y + 1].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		if (image[x - 1][y - 1].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		if (image[x + 1][y - 1].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		if (image[x - 1][y + 1].getAlpha() < TRANSPARENCY_THRESHOLD) alphaCount++;
		
		return alphaCount;
	}
	
	/**
	 * Uses getDifference() to determine the alpha level for each pixel.
	 * Uses isPixelDarker() to determine whether each pixel should be black or white (push or pull)
	 */
	public static Color[][] convertToPushAndPull(Color[][] originalImage, Color[][] background)
	{
		Color[][] newImage = new Color[originalImage.length][originalImage[0].length];
		
		for (int x = 0; x < originalImage.length; x++)
		{
			for (int y = 0; y < originalImage[0].length; y++)
			{
				int alpha = (int) (255 * getDifference(originalImage[x][y], background[x][y]));
				
				alpha = (int) ((double) alpha);
				
				if (alpha > 200) alpha = 200;
				if (alpha < 0) alpha = 0;
				
				if (isPixelDarker(originalImage[x][y], background[x][y]))
				{					
					newImage[x][y] = new Color(0, 0, 0, alpha);
				}
				
				else newImage[x][y] = new Color(255, 255, 255, alpha);
			}
		}
		
		return newImage;
	}
	
	public static Color testAlphaAndReplaceColor(Color targetColor, Color originalColor, int targetAlpha, int newAlpha)
	{
		if (targetColor.getAlpha() < targetAlpha) return new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), newAlpha);
		
		return targetColor;
	}
	
	/**
	 * Lowers the alpha value of all pixels based on those of their counterparts.
	 * 
	 * Make sure to scale the mask to the image before use.
	 */
	public static Color[][] removePixelsUsingMask(Color[][] image, Color[][] mask)
	{		
		for (int x = 0; x < image.length; x++)
		{
			for (int y = 0; y < image[0].length; y++)
			{
				int r = image[x][y].getRed();
				int g = image[x][y].getGreen();
				int b = image[x][y].getBlue();
				int a = (int) ((double) image[x][y].getAlpha() * (1.0 - ((double) mask[x][y].getAlpha() / 255)));

				if (a < 0) a = 0;
				if (a > 255) a = 255;
				
				image[x][y] = new Color(r, g, b, a);
			}
		}
		
		return image;
	}
	
	public static Color[][] overlayImage(Color[][] foreground, Color[][] background)
	{
		for (int x = 0; x < foreground.length; x++)
		{
			for (int y = 0; y < foreground[0].length; y++)
			{
				foreground[x][y] = blendPixels(foreground[x][y], background[x][y]);
			}
		}
		
		return foreground;
	}
	
	/**
	 * @greatestDifference represents the highest level of difference
	 * between any two pixels from the original image to the background.
	 * 
	 * If the difference between the current pixels is at least 80% of
	 * that, keep the pixel.
	 * 
	 * Else, if the difference is at least 55% of that, find whichever
	 * would need to be blended with the background in order to produce
	 * it; set the opacity of the pixel to the current difference out of
	 * the greatestDifference (or difference / greatestDifference).
	 * 
	 * Else, return an empty pixel.
	 */
	public static Color filterColor(Color pixel, Color toRemove, double greatestDifference)
	{
		double difference = getDifference(pixel, toRemove);
		
		if (difference > (0.8 * greatestDifference)) return pixel;
		
		else if (difference > (0.55 * greatestDifference))
		{
			Color fromBlended = getColorFromBlended(pixel, toRemove);
			
			int alpha = (int) ((difference / greatestDifference) * 255);
			
			return new Color(fromBlended.getRed(), fromBlended.getGreen(), fromBlended.getBlue(), alpha);
		}
		
		return new Color(0, 0, 0, 0);
	}
	
	/**
	 * Assuming @blended is blended (or averaged) with another color,
	 * find that color.
	 * 
	 * Math: (color1 + return) / 2 = blended ->
	 *       return = (blended * 2) - color1
	 */
	public static Color getColorFromBlended(Color blended, Color color1)
	{
		int r = (blended.getRed() * 2) - color1.getRed();
		int g = (blended.getGreen() * 2) - color1.getGreen();
		int b = (blended.getBlue() * 2) - color1.getBlue();
		
		r = r < 0 ? 0 : r > 255 ? 255 : r;
		g = g < 0 ? 0 : g > 255 ? 255 : g;
		b = b < 0 ? 0 : b > 255 ? 255 : b;
		
		return new Color(r, g, b);
	}
	
	/**
	 * Basically just getting a weighted average of each color relative to the foreground's alpha level. 
	 * Foreground gets alpha level * its color, background gets the rest * its color. Final alpha = sum of both.
	 */
	public static Color blendPixels(Color foreground, Color background)
	{			
		int r = ((foreground.getRed() * foreground.getAlpha()) + (background.getRed() * (255 - foreground.getAlpha()))) / 255;
		int g = ((foreground.getGreen() * foreground.getAlpha()) + (background.getGreen() * (255 - foreground.getAlpha()))) / 255;
		int b = ((foreground.getBlue() * foreground.getAlpha()) + (background.getBlue() * (255 - foreground.getAlpha()))) / 255;
		
		if (foreground.getAlpha() > OPACITY_THRESHOLD) //I threw this in here to see if it would remove some of the harsh outlines. Wasn't originally here.
		{                                              //Basically, just keep the overlay pixel's colors if it's strong enough. That's why its existence doesn't make sense.
			r = foreground.getRed(); g = foreground.getGreen(); b = foreground.getBlue();
		}
		
		int a = foreground.getAlpha() + background.getAlpha();
		
		if (a < TRANSPARENCY_THRESHOLD && (r == 255 && g == 255 && b == 255)) return new Color(0, 0, 0, 0); //Don't keep white pixels that are hardly there.
		
		a = (int) ((double) a * TEXTURE_SHARPEN_RATIO); //Sharpen the ones that aren't.
		
		if (a > 255) a = 255;                           //Don't pass the limit.
		
		return new Color(r, g, b, a);
	}
	
	public static int getChannelAverage(Color color)
	{
		return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
	}
	
	/**
	 * The specific distance used is to include diagonal pixels. 
	 * That is intentional for >16x images.
	 */
	public static boolean arePixelsAdjacent(int x1, int y1, int x2, int y2)
	{
		if ((x1 == x2) && (y1 == y2)) return false;
		
		double distance = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
		
		return distance < 1.5;
	}
	
	public static boolean isPixelDarker(Color foreground, Color background)
	{
		int fgTotal = (Math.abs(foreground.getRed() + foreground.getGreen() + foreground.getBlue()));
		int bgTotal = (Math.abs(background.getRed() + background.getGreen() + background.getBlue()));
		
		return fgTotal < bgTotal;
	}
	
	/** 
	 * Using the distance formula to determine the 0 - 1 "distance" between each color. 
	 * Imagine r, g, and b as x, y, and z on a coordinate plane; they are dimensions, not additive values.
	 * e.g. Color(255, 255, 0) and Color(255, 0, 255) are totally different colors, so getting the average doesn't work.
	 */
	public static double getDifference(Color foreground, Color background)
	{
		int r = foreground.getRed() - background.getRed();
		int g = foreground.getGreen() - background.getGreen();
		int b = foreground.getBlue() - background.getBlue();
		
		int r2 = (r * r), g2 = (g * g), b2 = (b * b);
		
		return Math.sqrt(r2 + g2 + b2) / 441.673; //Roughly the maximum distance.
	}
	
	/**
	 * Finds the greatest difference for any two pixels in an image. Differs from
	 * getMostUniqueColor() in that it is not based on the background. This should
	 * provide an estimation of how strong a threshold is needed to extract an image.
	 */
	public static double getGreatestDifference(Color[][] image)
	{
		double greatestDifference = 0.0;
		
		for (int x = 0; x < image.length; x++)
		{
			for (int y = 0; y < image[0].length; y++)
			{
				for (int x2 = 0; x2 < image.length; x2++)
				{
					for (int y2 = 0; y2 < image[0].length; y2++)
					{
						double difference = getDifference(image[x][y], image[x2][y2]);
						
						if (difference > greatestDifference)
						{
							greatestDifference = difference;
						}
					}
				}
			}
		}
		
		return greatestDifference;
	}
	
	/**
	 * Basically just calculates the SD for r, g, and b of each color,
	 * then returns that as a color.
	 */
	public static Color getStandardDeviation(Color[][] image)
	{
		LimitlessColor average = LimitlessColor.fromColor(getAverageColor(image));
		
		List<LimitlessColor> devianceValues = new ArrayList<>();

		for (int x = 0; x < image.length; x++)
		{
			for (int y = 0; y < image[0].length; y++)
			{
				LimitlessColor noLimits = LimitlessColor.fromColor(image[x][y]);
				
				LimitlessColor deviance = LimitlessColor.subtractColors(noLimits, average);
				
				deviance.squareColor();
				
				devianceValues.add(deviance);
			}
		}
		
		LimitlessColor sum = devianceValues.get(0);
		
		for (int i = 1; i < devianceValues.size(); i++)
		{
			sum = LimitlessColor.addColors(sum, devianceValues.get(i));
		}
		
		sum.divideBy(devianceValues.size());
		
		sum.toSquareRoot();
		
		return sum.getColor();
	}
	
	private static class LimitlessColor
	{
		private int r, g, b;
		
		LimitlessColor(int r, int g, int b)
		{
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public static LimitlessColor fromColor(Color color)
		{
			return new LimitlessColor(color.getRed(), color.getGreen(), color.getBlue());
		}
		
		public static LimitlessColor addColors(LimitlessColor... colors)
		{
			int r = colors[0].r, g = colors[0].g, b = colors[0].b;
			
			for (int i = 1; i < colors.length; i++)
			{
				r += colors[i].getRed();
				g += colors[i].getGreen();
				b += colors[i].getBlue();
			}
			
			return new LimitlessColor(r, g, b);
		}
		
		public static LimitlessColor subtractColors(LimitlessColor... colors)
		{
			int r = colors[0].r, g = colors[0].g, b = colors[0].b;
			
			for (int i = 1; i < colors.length; i++)
			{
				r -= colors[i].getRed();
				g -= colors[i].getGreen();
				b -= colors[i].getBlue();
			}
			
			return new LimitlessColor(r, g, b);
		}
		
		public void divideBy(int number)
		{
			this.r /= number;
			this.g /= number;
			this.b /= number;
		}
		
		public void squareColor()
		{
			this.r = (int) Math.pow(r, 2);
			this.g = (int) Math.pow(g, 2);
			this.b = (int) Math.pow(b, 2);
		}
		
		public void toSquareRoot()
		{
			this.r = (int) Math.sqrt(r);
			this.g = (int) Math.sqrt(g);
			this.b = (int) Math.sqrt(b);
		}
		
		public int getRed() {return r;}
		
		public int getGreen() {return g;}
		
		public int getBlue() {return b;}
		
		public Color getColor()
		{
			if (r < 0 || g < 0 || b < 0 || r > 255 || g > 255 || b > 255)
			{
				logger.warn("Error: Your color is still limitless. Cannot convert from invalid color ranges.");
				
				logger.warn("r = " + r + ", g = " + g + ", b = " + b);
				
				return null;
			}
			
			return new Color(r, g, b);
		}
		
		public static LimitlessColor getLimitlessAverage(LimitlessColor... colors)
		{
			int r = 0, g = 0, b = 0;
			
			for (LimitlessColor color : colors)
			{
				r += color.getRed();
				g += color.getGreen();
				b += color.getBlue();
			}
			
			r /= colors.length;
			g /= colors.length;
			b /= colors.length;
			
			return new LimitlessColor(r, g, b);
		}
	}
}