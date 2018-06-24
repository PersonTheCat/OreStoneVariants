package personthecat.mod.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class FileTools
{
    //redo this one
	public static String getBlendedPath(String fromNormal)
    {
    	File file = new File(fromNormal);
    	
    	String blendedPath = fromNormal;
    	
    	blendedPath = blendedPath.replaceAll(file.getName(), "blended/" + file.getName());
    	
    	file = new File(fromNormal); //update the name
    	
    	if (file.getName().split(Pattern.quote(".")).length > 1)
    	{
    		String extension = getExtension(file);
    		
    		blendedPath = blendedPath.replaceAll("." + extension, "_blended." + extension);
    	}
    	
    	return blendedPath;
    }
    
    public static String getNormalPath(String fromBlended)
    {
    	File file = new File(fromBlended);
    	
    	String filename = file.getName().replaceAll("_blended", "");
    	
    	if (file.getParentFile().getName().contains("blended"))
    	{    		
    		file = new File(file.getParentFile().getParentFile().getPath(), filename);
    	}

    	else file = new File(file.getParentFile().getPath(), filename);

    	return file.getPath().replace("\\", "/");
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
}