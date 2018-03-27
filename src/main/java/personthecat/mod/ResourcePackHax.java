package personthecat.mod;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import personthecat.mod.util.Reference;

public class ResourcePackHax implements IResourcePack
{
	File parentFile, image;
	InputStream inputStream;
	String name;
	
	public ResourcePackHax(File file)
	{
		this.name = file.getName().replaceAll(".zip", "");
		this.parentFile = file;
		
		System.out.println("Answer: someone tried to create a new resourcepack for this file: " + file.getPath());
		
		try
		{
			if (file.isDirectory())
			{
				this.image = new File(file.getPath() + "/" + name + ".png");
				
				this.inputStream = new BufferedInputStream(new FileInputStream(image));
			}
			else if (file.getName().endsWith(".zip"))
			{
				System.out.println("Answer: this should be the name of the overlay: " + file.getPath() + "/" + name + ".png");
				
				ZipFile zipFile = new ZipFile(file.getPath());
				
				this.inputStream = zipFile.getInputStream(zipFile.getEntry(file.getPath() + "/" + name + ".png"));
				
				FileUtils.copyInputStreamToFile(inputStream, this.image);
				
				zipFile.close();
				
				BufferedImage actualImage = ImageIO.read(image);
				DynamicTexture test = new DynamicTexture(actualImage);

			}
		}
		catch (NullPointerException e) {e.getSuppressed();}
		catch (FileNotFoundException e) {e.printStackTrace();}
		catch (IOException e) {e.getSuppressed();}
	}
	
	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException
	{
		return inputStream;
	}

	@Override
	public boolean resourceExists(ResourceLocation location)
	{
		return image.exists();
	}

	@Override
	public Set<String> getResourceDomains()
	{
		Set<String> modName = new HashSet<String>();
		modName.add(Reference.MODID);
		
		return modName;
	}

	@Override
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException
	{
		String metadataHax = 
			  "{\n"
			+ "\"pack\":{\n"
			+ "\"description\": \"Custom texture overlay\",\n"
			+ "\"pack_format\": 1\n"
			+ "}\n"
			+ "}";
		
		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(metadataHax).getAsJsonObject();
		
		return metadataSerializer.parseMetadataSection(metadataSectionName, obj);
	}

	@Override
	public BufferedImage getPackImage() throws IOException
	{
		return null;
	}

	@Override
	public String getPackName()
	{
		return name;
	}
	
	public static class Listener implements IResourceManagerReloadListener
	{
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager)
		{
			
		}
	}
}