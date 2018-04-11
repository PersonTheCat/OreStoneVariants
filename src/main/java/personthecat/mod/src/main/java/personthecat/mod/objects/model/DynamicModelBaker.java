package personthecat.mod.objects.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import personthecat.mod.config.ConfigFile;

//The heart of all problems in the world. 
public class DynamicModelBaker
{
	private static final FaceBakery faceBakery = new FaceBakery();
	private final Map<EnumFacing, List<BakedQuad>> faceQuads = new HashMap<>();
	
	public DynamicModelBaker()
	{
		for (EnumFacing face : EnumFacing.VALUES)
		{
			faceQuads.put(face, new ArrayList<BakedQuad>());
		}
	}

	public IBakedModel bakeDynamicModel(boolean overrideShade, IBlockState targetBlockState, IBakedModel targetModel, TextureAtlasSprite overlay_sprite) throws IOException
	{	
		ModelBlock originalModel = getUnbakedModel(new ResourceLocation("ore_stone_variants:models/block/dynamic_block.json"));
		TextureAtlasSprite sprite = ModelEventHandler.failBackground;
		boolean shade = true;
        
        for (BlockPart blockPart : originalModel.getElements())
        {        	
        	for (EnumFacing enumFacing : blockPart.mapFaces.keySet())
            {                	
                String textureName = originalModel.resolveTextureName(blockPart.mapFaces.get(enumFacing).texture);
                
                if (textureName.equals("ore_stone_variants:blocks/background_finder"))
                {
                	List<BakedQuad> quads = targetModel.getQuads(targetBlockState, enumFacing, 0L);
                 	sprite = quads.isEmpty() ? targetModel.getParticleTexture() : quads.get(0).getSprite();
                   	shade = true;
                }
                   
                if (textureName.equals("ore_stone_variants:blocks/overlay_finder"))
				{
					sprite = overlay_sprite;
						
					shade = overrideShade ? ConfigFile.shade : !ConfigFile.shade;
				}
                
				faceQuads.get(enumFacing).add(faceBakery.makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, blockPart.mapFaces.get(enumFacing), sprite, enumFacing, ModelRotation.X0_Y0, blockPart.partRotation, false, shade));
                }
            }
        	//Returning an empty quads list because all sides should be cull faces.
            return new SimpleBakedModel(new ArrayList<BakedQuad>(), faceQuads, originalModel.isAmbientOcclusion(), originalModel.isGui3d(), targetModel.getParticleTexture(), targetModel.getItemCameraTransforms(), originalModel.createOverrides());  
	}
	
    public static ModelBlock getUnbakedModel(ResourceLocation location) throws IOException
    {
        Reader reader = null;
        IResource iresource = null;

        try
        {
            iresource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8);
            
            ModelBlock model = ModelBlock.deserialize(reader);
            model.name = location.toString();
            return model;
        }
        
        finally
        {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly((Closeable)iresource);
        }
    }
	
}
