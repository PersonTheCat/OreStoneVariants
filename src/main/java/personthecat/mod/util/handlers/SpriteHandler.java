package personthecat.mod.util.handlers;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import personthecat.mod.util.NameReader;

//Thanks to pupnewfster for writing a solid 85% of this class for me!
public class SpriteHandler
{	
	public static File resourcePack = new File(Loader.instance().getConfigDir().getPath() + "/ore_stone_variants_mods/resources.zip");
	public static File template = new File(Loader.instance().getConfigDir().getPath() + "/ore_stone_variants_mods/template.zip");
	
	public static void createOverlay(String backgroundFile, String imageFile, String inThisLocation)
    {
	Color[][] background = loadPixelsFromImage(scaleBackgroundToOverlay(backgroundFile, imageFile));
    	Color[][] image = loadPixelsFromImage(loadImage(imageFile));
        
    	//Was able to load
    	if ((image != null) && (background != null) && (image.length == background.length))
    	{
    		writePixelsToImage(extractOverlay(image, background), inThisLocation);
    		
    		testForAndCopyMcmeta(imageFile, inThisLocation);
    	}
    }

    public static void createDense(String imageFile)
    {    	
    	Color[][] colors = loadPixelsFromImage(loadImage(imageFile));
        String oreName = NameReader.getOreFromPath(imageFile);
        imageFile = imageFile.replaceAll(oreName, "dense_" + oreName);
    	
    	//Was able to load
    	if (colors != null) writePixelsToImage(shiftImage(colors), imageFile);
    }

    private static Color[][] extractOverlay(Color[][] image, Color[][] background)
    {
    	int w, h, bh = background[0].length;
	Color[][] overlay = new Color[w = image.length][h = image[0].length];
	int frames = h / bh;

	//Does not divide nicely
	if (1.0 * h / bh != frames) return null;

	//Technically starts at 0.4.
	double targetAlpha = 0.8;
	double averageAlpha = 0.0;

	//Most vanilla ores should be ~36.855% alpha. 
	while (averageAlpha < 30.0)
	{
		targetAlpha /= 2;
		averageAlpha = 0.0;

		for (int f = 0; f < frames; f++)
			for (int x = 0; x < w; x++)
				for (int y = 0; y < bh; y++)
				{
					int imageY = f * bh + y;

					overlay[x][imageY] = getDifference(image[x][imageY], background[x][y], targetAlpha);

					averageAlpha += overlay[x][imageY].getAlpha();
				}			

		averageAlpha /= (frames * w * bh);
	}

	return overlay;
    }

    /* Math logic that gets used below
    target = alphaPercent*overlay + background * (1 - alphaPercent)
    alphaPercent*overlay = target - background * (1 - alphaPercent)
    overlay = (target - background * (1 - alphaPercent)) / alphaPercent
    */
   	private static Color getDifference(Color front, Color back, double alphaPercent)
   	{
   		if (front.getRGB() == back.getRGB()) return new Color(0, 0, 0, 0);
   		
   		int rOverlay = (int) ((front.getRed() - back.getRed() * (1 - alphaPercent)) / alphaPercent);
   		int gOverlay = (int) ((front.getGreen() - back.getGreen() * (1 - alphaPercent)) / alphaPercent);
   		int bOverlay = (int) ((front.getBlue() - back.getBlue() * (1 - alphaPercent)) / alphaPercent);
           
   		if (rOverlay > 255 || gOverlay > 255 || bOverlay > 255 || rOverlay < 0 || gOverlay < 0 || bOverlay < 0)
   			return front;
   		
   		//else same color scheme as background
   		return new Color(0, 0, 0, 0);
    }
   	
   	private static Color[][] loadPixelsFromImage(BufferedImage image)
   	{
		int w, h;
   		
   		Color[][] colors = new Color[w = image.getWidth()][h = image.getHeight()];
        
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				colors[x][y] = new Color(image.getRGB(x, y), true);
        
		return colors;
   	}
    
    private static BufferedImage loadImage(String file)
    {
    	BufferedImage image = null;
		
	try
	{    			
		image = ImageIO.read(Minecraft.class.getClassLoader().getResourceAsStream(file));
	}

	//needs to also search the resourcepack file to see if the image exists there, instead.
	catch (NullPointerException | IllegalArgumentException | IOException e) 
	{    			
		try
		{
		ZipFile resourcePackZip = new ZipFile(resourcePack);

		image = ImageIO.read(resourcePackZip.getInputStream(resourcePackZip.getEntry(file)));

		resourcePackZip.close();
		}

		catch (IOException e2) {return null;}

	}

	return image;
    }

    private static Color[][] shiftImage(Color[][] image)
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
					
					shifted[x][imageY] = getAverage(image[x][imageY], fromIndex(image, x - 1, imageY, f), fromIndex(image, x + 1, imageY, f),
							fromIndex(image, x, imageY - 1, f), fromIndex(image, x, imageY + 1, f));//self, left, right, up, down
				}
		
        return shifted;
    }

    private static Color fromIndex(Color[][] image, int x, int y, int frame)
    {
        int w = image.length;

		return ((x < 0) || (y < frame * w) || (x >= w) || (y >= (frame + 1) * w) || (image[x][y].getAlpha() == 34)) ? new Color(0, 0, 0, 0) : image[x][y];
    }

    private static Color getAverage(Color... colors)
    {
        int count = 0, red = 0, green = 0, blue = 0, alpha = 0;
        
        for (Color color : colors)
        {
            if (color.getAlpha() != 0)
            {
                count++;
                red += color.getRed();
                green += color.getGreen();
                blue += color.getBlue();
                alpha += color.getAlpha();
            }
        }
        
        if (count == 0) return new Color(0, 0, 0, 0);
        
        alpha /= count;
        
        return alpha == 34 ? new Color(0, 0, 0, 0) : new Color(red / count, green / count, blue / count, alpha);
    }
    
    //We're only using the width in case the overlay is animated. We don't necessarily want to scale it the whole way down because of functions used later.
    private static BufferedImage scaleBackgroundToOverlay(String background, String overlay)
    {
    	BufferedImage backgroundImage = loadImage(background);
    	BufferedImage overlayImage = loadImage(overlay);
    	
    	BufferedImage scaledImage = new BufferedImage(overlayImage.getWidth(), overlayImage.getWidth(), overlayImage.getType());
    	
    	Graphics2D graphics = scaledImage.createGraphics();
    	graphics.drawImage(backgroundImage, 0, 0, overlayImage.getWidth(), overlayImage.getWidth(), null);
    	graphics.dispose();
    	
    	return scaledImage;
    }
    
    
    //I see a lot of repetition below this point. I will hopefully write one method to handle all file copying relatively soon.
    
    private static void writePixelsToImage(Color[][] colors, String file)
    {
        if (colors == null) return;
    		
    	int w, h;
        BufferedImage bufferedImage = new BufferedImage(w = colors.length, h = colors[0].length, BufferedImage.TYPE_INT_ARGB);
        File temp = null;
		        
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                bufferedImage.setRGB(x, y, colors[x][y].getRGB());
        
        try 
        {
        	new File(Loader.instance().getConfigDir().getPath() + "/ore_stone_variants_mods/").mkdirs();
        	
        	temp = File.createTempFile("new_overlay", ".png");
        	temp.deleteOnExit();
        	
        	ImageIO.write(bufferedImage, "png", temp);
		} 
        
        catch (IOException e) {e.getSuppressed();}
        
        copyToResourcePack(file, temp);
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
    		
    		copyStream(copyMe, output, 1024);
    		copyToResourcePack(inThisLocation + ".mcmeta", temp);
    		
    		//Gonna go ahead and copy this for the dense overlay, as well. Not the most organized location to do that, but definitely the easiest.
    		copyToResourcePack(inThisLocation.replaceAll(fileName, "dense_" + fileName) + ".mcmeta", temp);
    		
    		copyMe.close();
    		output.close();
    	}
    	
    	catch (NullPointerException | IOException ignored) {}
    }
    
    //Copies the resourcepack from the jar, if it doesn't exist already.
    public static void testForResourcePack()
    {    	
    	Map<File, String> fileMap = new HashMap<File, String>();
    	fileMap.put(resourcePack, "assets/ore_stone_variants/resourcepack/ore_stone_variants.zip");
    	fileMap.put(template, "assets/ore_stone_variants/customores/template.zip");
    	
    	for (File file : fileMap.keySet())
    	{
    		if (!file.exists())
    		{
    			try
    			{
            		file.getParentFile().mkdirs();
        			
        		InputStream copyMe = Minecraft.class.getClassLoader().getResourceAsStream(fileMap.get(file));
            		FileOutputStream output = new FileOutputStream(file.getPath());
    				
            		copyStream(copyMe, output, 1024);
            		        		
            		copyMe.close();
            		output.close();
    			}
    			
    			catch (NullPointerException | IOException e) {e.getSuppressed();}
    		}
    	}
    }

    private static void copyToResourcePack(String path, File image)
    {
    	try
    	{
		ZipFile resourcePackZip = new ZipFile(resourcePack);

		//If it already exists, don't do anything.
		if (resourcePackZip.getEntry(path) != null)
		{
			resourcePackZip.close();
			return;
		}

		resourcePackZip.close();

		File temp = File.createTempFile("ore_sv_resources", null);

		Files.move(resourcePack.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);

		ZipFile tempZip = new ZipFile(temp);

		ZipOutputStream output = new ZipOutputStream(new FileOutputStream(resourcePack));

		Enumeration<? extends ZipEntry> entries = tempZip.entries();

		while (entries.hasMoreElements())
		{
			ZipEntry currentEntry = entries.nextElement();

			moveToZip(tempZip.getInputStream(currentEntry), output, currentEntry);
		}			

		moveToZip(new FileInputStream(image), output, new ZipEntry(path));

		output.close();
		tempZip.close();
		temp.delete();
	} 
    	
    	catch (IOException e) {e.printStackTrace();}
    }
    
    private static void moveToZip(InputStream input, ZipOutputStream output, ZipEntry entry) throws IOException
    {
    	output.putNextEntry(entry);
       	copyStream(input, output, 1024);

    	input.close();
    }
    
    private static void copyStream(InputStream input, OutputStream output, int bufferSize) throws IOException
    {
	byte[] buffer = new byte[bufferSize];
	int length;

	while ((length = input.read(buffer)) > 0)
	{
		output.write(buffer, 0, length);
	}
    }
}
