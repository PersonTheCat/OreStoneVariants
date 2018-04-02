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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
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
		Color[][] background = loadPixelsFromImage(backgroundFile);
    	Color[][] image = loadPixelsFromImage(imageFile);
        
    	//Was able to load
    	if ((image != null) && (background != null) && (image.length == background.length) && (image[0].length == background[0].length))
    		writePixelsToImage(extractOverlay(image, background), inThisLocation);
    }

    public static void createDense(String imageFile)
    {    	
    	Color[][] colors = loadPixelsFromImage(imageFile);
        String oreName = NameReader.getOreFromPath(imageFile);
        imageFile = imageFile.replaceAll(oreName, "dense_" + oreName);
    	
    	//Was able to load
    	if (colors != null) writePixelsToImage(shiftImage(colors), imageFile);
    }

    private static Color[][] extractOverlay(Color[][] image, Color[][] background)
    {
    	int w, h;
    	Color[][] overlay = new Color[w = image.length][h = image[0].length];
        
    	for (int x = 0; x < w; x++)
    		for (int y = 0; y < h; y++)
    			overlay[x][y] = getDifference(image[x][y], background[x][y]);
        
    	return overlay;
    }

   	private static Color getDifference(Color front, Color back)
   	{
   		if (front.getRGB() == back.getRGB())
   			return new Color(0, 0, 0, 0);
           /* Math logic that gets used below
           target = alphaPercent*overlay + background * (1 - alphaPercent)
           alphaPercent*overlay = target - background * (1 - alphaPercent)
           overlay = (target - background * (1 - alphaPercent)) / alphaPercent
           */
   		double alphaPercent = 0.4;
   		int rOverlay = (int) ((front.getRed() - back.getRed() * (1 - alphaPercent)) / alphaPercent);
   		int gOverlay = (int) ((front.getGreen() - back.getGreen() * (1 - alphaPercent)) / alphaPercent);
   		int bOverlay = (int) ((front.getBlue() - back.getBlue() * (1 - alphaPercent)) / alphaPercent);
           
   		if (rOverlay > 255 || gOverlay > 255 || bOverlay > 255 || rOverlay < 0 || gOverlay < 0 || bOverlay < 0)
   			return front;
   		//else same color scheme as background
   		return new Color(0, 0, 0, 0);
    }

    private static Color[][] loadPixelsFromImage(String file)	
    {    	    	    	    	   	
    	try
    	{
    		int w, h;
    		
    		BufferedImage image = null;
    		
    		try
    		{    			
    			image = ImageIO.read(Minecraft.class.getClassLoader().getResourceAsStream(file));
    		}
    		
    		//needs to also search the resourcepack file to see if the image exists there, instead.
    		catch (NullPointerException | IllegalArgumentException e) 
    		{    			
    			ZipFile resourcePackZip = new ZipFile(resourcePack);
    			
    			image = ImageIO.read(resourcePackZip.getInputStream(resourcePackZip.getEntry(file)));
    			
    			resourcePackZip.close();
    		}
    		
    		Color[][] colors = new Color[w = image.getWidth()][h = image.getHeight()];
            
    		for (int x = 0; x < w; x++)
    			for (int y = 0; y < h; y++)
    				colors[x][y] = new Color(image.getRGB(x, y), true);
            
    		return colors;
    	} 
    	
    	catch (IOException e) {return null;}
    }

    private static Color[][] shiftImage(Color[][] image)
    {
    	int w, h;
    	Color[][] shifted = new Color[w = image.length][h = image[0].length];
        
    	for (int x = 0; x < w; x++)
    		for (int y = 0; y < h; y++)
    			shifted[x][y] = getAverage(image[x][y], fromIndex(image, x - 1, y), fromIndex(image, x + 1, y), //self, left, right, 
    							fromIndex(image, x, y - 1), fromIndex(image, x, y + 1)); 						//up, down
    	
        return shifted;
    }

    private static Color fromIndex(Color[][] image, int x, int y)
    {
        //Can remove "|| image[x][y].getAlpha() == 34" if it is only making it from auto generated images
        return ((x < 0) || (y < 0) || (x >= image.length) || (y >= image[0].length) || (image[x][y].getAlpha() == 34)) ? new Color(0, 0, 0, 0) : image[x][y];
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

    private static void writePixelsToImage(Color[][] colors, String file)
    {
        BufferedImage bufferedImage = new BufferedImage(colors.length, colors[0].length, BufferedImage.TYPE_INT_ARGB);
        int w = colors.length, h = colors[0].length;
        String fileName = NameReader.getOreFromPath(file);
        File temp = null;
		        
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                bufferedImage.setRGB(x, y, colors[x][y].getRGB());
        
        try 
        {
        	File path = new File(Loader.instance().getConfigDir().getPath() + "/ore_stone_variants_mods/temp/");
        	path.mkdirs();
        	
        	temp = File.createTempFile(Loader.instance().getConfigDir().getPath() + "/ore_stone_variants_mods/temp/" + fileName, "");
        	temp.deleteOnExit();
        	
        	ImageIO.write(bufferedImage, "png", temp);
		} 
        
        catch (IOException e) {e.getSuppressed();}
        
        copyToResourcePack(file, temp);
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
            		FileOutputStream outputStream = new FileOutputStream(file.getPath());
    				
            		copyStream(copyMe, outputStream, 1024);
            		        		
            		copyMe.close();
            		outputStream.close();
    			}
    			
    			catch (IOException e) {e.getSuppressed();}
    		}
    	}
    }
    
    //Creates the new image, copies it to the resourcepack. 
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
			
			File temp = File.createTempFile(resourcePackZip.getName(), null);
			temp.delete();
			
			resourcePack.renameTo(temp);
			
			ZipInputStream tempIn = new ZipInputStream(new FileInputStream(temp));
			ZipOutputStream tempOut = new ZipOutputStream(new FileOutputStream(resourcePack));
			
			ZipEntry currentEntry = tempIn.getNextEntry();
			
			while (currentEntry != null)
			{
				tempOut.putNextEntry(currentEntry);
				copyStream(tempIn, tempOut, 1024);
				
				currentEntry = tempIn.getNextEntry();
			}
			
			tempIn.close();
			
			InputStream imageIn = new FileInputStream(image);
			
			tempOut.putNextEntry(new ZipEntry(path));
			copyStream(imageIn, tempOut, 1024);
			
			imageIn.close();
			tempOut.close();
			temp.delete();
			
		} 
    	
    	catch (IOException e) {e.printStackTrace();}
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