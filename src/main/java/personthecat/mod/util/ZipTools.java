package personthecat.mod.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class ZipTools
{
	public static final File RESOURCE_PACK = new File(Loader.instance().getConfigDir().getPath() + "/ore_stone_variants_mods/resources.zip");
	public static final File TEMPLATE = new File(Loader.instance().getConfigDir().getPath() + "/ore_stone_variants_mods/template.zip");
	
    //Copies the resourcepack from the jar, if it doesn't exist already.
    public static void testForResourcePack()
    {    	
    	Map<File, String> fileMap = new HashMap<>();
    	fileMap.put(RESOURCE_PACK, "assets/ore_stone_variants/resourcepack/ore_stone_variants.zip");
    	fileMap.put(TEMPLATE, "assets/ore_stone_variants/customores/template.zip");
    	
    	for (File file : fileMap.keySet())
    	{
    		if (!file.exists())
    		{
    			try
    			{
            		file.getParentFile().mkdirs();
        			
        			InputStream copyMe = Minecraft.class.getClassLoader().getResourceAsStream(fileMap.get(file));
            		FileOutputStream output = new FileOutputStream(file.getPath());
    				
            		FileTools.copyStream(copyMe, output, 1024);
            		        		
            		copyMe.close();
            		output.close();
    			}
    			
    			catch (NullPointerException | IOException e) {e.getSuppressed();}
    		}
    	}
    }
    
    public static boolean isFileInZip(File zip, String path)
    {
    	boolean fileExists = false;
    	
    	try
		{
        	ZipFile zipFile = new ZipFile(zip);
        	
        	ZipEntry testEntry = zipFile.getEntry(path);
        	
        	fileExists = testEntry != null;
        	
    		zipFile.close();
		}
    	
		catch (IOException ignored) {}
    	
    	return fileExists;
    }
    
    public static BufferedImage getImageFromZip(File zip, String path)
    {
    	BufferedImage image = null;

    	if (isFileInZip(zip, path))
    	{
        	try
    		{
        		ZipFile zipFile = new ZipFile(zip);
    			
    			image = ImageIO.read(zipFile.getInputStream(zipFile.getEntry(path)));
    			
    			zipFile.close();
    		}
        	
    		catch (IOException e) {System.err.println("Error: unable to load " + path + " in " + zip.getName());}
    	}

    	return image;
    }
    
    public static void copyToZip(String path, File image, File zip)
    {
    	try
    	{
			if (isFileInZip(zip, path)) return; //If it already exists, don't do anything.
			
			File temp = File.createTempFile("ore_sv_resources", null);
			
			Files.move(RESOURCE_PACK.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			ZipFile tempZip = new ZipFile(temp);

			ZipOutputStream output = new ZipOutputStream(new FileOutputStream(RESOURCE_PACK));
			
			Collections.list(tempZip.entries()).forEach(entry -> 
			{
				try
				{
					moveToZip(tempZip.getInputStream(entry), output, entry);
				}
				
				catch (IOException e) {e.printStackTrace();}
			});
			
			moveToZip(new FileInputStream(image), output, new ZipEntry(path));
			
			output.close();
			tempZip.close();
			temp.delete();
		} 
    	
    	catch (IOException e) {e.printStackTrace();}
    }
    
    protected static void moveToZip(InputStream input, ZipOutputStream output, ZipEntry entry) throws IOException
    {
    	output.putNextEntry(entry);
       	FileTools.copyStream(input, output, 1024);

    	input.close();
    }
}
