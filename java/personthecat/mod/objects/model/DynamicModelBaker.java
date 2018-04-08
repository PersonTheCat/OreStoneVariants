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
	private final List<BakedQuad> generalQuads = new ArrayList<BakedQuad>();
	private final List<BakedQuad> generalQuadsEmpty = new ArrayList<BakedQuad>();
	private final Map<EnumFacing, List<BakedQuad>> faceQuads = new HashMap<EnumFacing, List<BakedQuad>>();
	
	public DynamicModelBaker() {}

	public IBakedModel bakeDynamicModel(boolean overrideShade, IBlockState targetBlockState, IBakedModel targetModel, TextureAtlasSprite overlay_sprite) throws IOException
	{	
		ModelBlock originalModel = getUnbakedModel(new ResourceLocation("ore_stone_variants:models/block/dynamic_block.json"));
		TextureAtlasSprite sprite = ModelEventHandler.failBackground;
		boolean shade = true;
        
        for (BlockPart blockPart : originalModel.getElements())
        {
            for (EnumFacing enumFacing : blockPart.mapFaces.keySet())
            {                	
                BlockPartFace blockPartFace = blockPart.mapFaces.get(enumFacing);
                
                if (originalModel.resolveTextureName(blockPartFace.texture).equals("ore_stone_variants:blocks/background_finder"))
                {
                	List<BakedQuad> quads = targetModel.getQuads(targetBlockState, enumFacing, 0L);
                 	sprite = quads.isEmpty() ? targetModel.getParticleTexture() : quads.get(0).getSprite();
                   	shade = true;
                }
                   
                if (originalModel.resolveTextureName(blockPartFace.texture).equals("ore_stone_variants:blocks/overlay_finder"))
				{
					sprite = overlay_sprite;
						
					if (overrideShade) shade = ConfigFile.shade;
					
					else shade = !ConfigFile.shade;
				}
				
				generalQuads.add(faceBakery.makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, blockPartFace, sprite, enumFacing, ModelRotation.X0_Y0, blockPart.partRotation, false, shade));
				faceQuads.put(enumFacing, generalQuads);
                }
            }
        	//Returning an empty quads list because all sides should be cull faces. --this is why faces sometimes render when they shouldn't, though. 
            return new SimpleBakedModel(generalQuadsEmpty, faceQuads, originalModel.isAmbientOcclusion(), originalModel.isGui3d(), targetModel.getParticleTexture(), targetModel.getItemCameraTransforms(), originalModel.createOverrides());  
	}
	
    public static ModelBlock getUnbakedModel(ResourceLocation location) throws IOException
    {
        Reader reader = null;
        IResource iresource = null;
        ModelBlock model;

        try
        {
            iresource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8);
            
            model = ModelBlock.deserialize(reader);
            model.name = location.toString();
            ModelBlock modelblock1 = model;
            return modelblock1;
        }
        
        finally
        {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly((Closeable)iresource);
        }
    }
	
}
