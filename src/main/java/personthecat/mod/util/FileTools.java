package personthecat.mod.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import net.minecraft.util.ResourceLocation;

import static personthecat.mod.Main.logger;

public class FileTools
{
    //redo this one
	public static String getBlendedPath(String fromNormal)
    {
    	File file = new File(fromNormal);
    	
    	String blendedPath = getNormalPath(fromNormal);
    	
    	blendedPath = blendedPath.replaceAll(file.getName(), "blended/" + file.getName());
    	
    	file = new File(fromNormal); //update the name
    	
    	if (file.getName().split(Pattern.quote(".")).length > 1)
    	{
    		String extension = getExtension(file);
    		
    		blendedPath = blendedPath.replaceAll("." + extension, "_blended." + extension);
    	}
    	
    	else blendedPath = blendedPath + "_blended";
    	
    	return blendedPath;
    }
    
    public static String getNormalPath(String fromBlended)
    {
    	if (fromBlended.split("/").length < 2) return fromBlended;
    	
    	File file = new File(fromBlended);
    	
    	String filename = file.getName().replaceAll("_blended", "").replaceAll("dense_", "");
    	
    	if (file.getParentFile().getName().contains("blended"))
    	{    		
    		file = new File(file.getParentFile().getParentFile().getPath(), filename);
    	}

    	else file = new File(file.getParentFile().getPath(), filename);

    	return file.getPath().replace("\\", "/");
    }
    
    public static String getDensePath(String fromNormal)
    {
    	String normalPath = getNormalPath(fromNormal);
    	
    	File file = new File(normalPath);
    	
    	if (!file.getName().startsWith("dense_"))
    	{
    		String fileName = "dense_" + file.getName();
    		
    		file = new File(file.getParentFile().getPath(), fileName);
    		
    		return file.getPath().replace("\\", "/");
    	}
    	
    	return normalPath;
    }
    
    /**
     * Must start with "assets/..."
     */
    public static ResourceLocation getResourceLocationFromPath(String fromPath)
    {
    	String domain = null, path = null;
    	
    	if (!fromPath.startsWith("assets/")) return null;
    	
    	String[] pathSplit = fromPath.split("/");
    	
    	domain = pathSplit[1];
    	
    	path = fromPath.replaceAll("assets/" + domain + "/", "");
    	
    	return new ResourceLocation(domain, path);
    }
    
    private static String getExtension(File file)
    {
    	if (file.getName().split(Pattern.quote(".")).length > 1) //File#isFile() for path only.
    	{
    		return file.getName().split(Pattern.quote("."))[1];
    	}
    	
    	return "";
    }
	
    //Maybe shouldn't be public. Meh.
	public static void copyStream(InputStream input, OutputStream output, int bufferSize) throws IOException
    {
		byte[] buffer = new byte[bufferSize];
		int length;

		while ((length = input.read(buffer)) > 0)
		{
			output.write(buffer, 0, length);
		}
    }
	
	public static void writeToFile(File file, String line)
	{
		try
		{
			FileWriter writer = new FileWriter(file);
			
			writer.write(line);				
			
			writer.close();
		}
		catch (IOException e) { logger.warn("Could not write new file " + file.getPath()); }
	}
}