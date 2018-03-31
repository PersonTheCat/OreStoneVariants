package personthecat.mod.objects.blocks;

import java.io.IOException;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.CreativeTab;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.init.BlockInit;
import personthecat.mod.init.ItemInit;
import personthecat.mod.objects.blocks.item.ItemBlockVariants;
import personthecat.mod.properties.DefaultProperties;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.Reference;

public class BlockOresDynamic extends BlockOresBase implements IHasModel
{
private int enumerate;
private String name;

	public BlockOresDynamic(int enumerate, String oreToImitate) 
	{
		super(oreToImitate, true, false, enumerate);
		
		this.enumerate = enumerate;
		this.name = oreToImitate;
		
		BlockInit.BLOCKSTATES.add(this.getDefaultState());
		BlockInit.DYNAMIC_BLOCKSTATES_NUMBER_MAP.put(this.getDefaultState(), enumerate);
	}
	
	//Only for the stats page. The rest is handled in ItemBlockVariants.
	@Override
    public String getLocalizedName()
    {
    	String nameText = I18n.translateToLocal(this.getUnlocalizedName() + ".name");
		try 
		{
			IBlockState backgroundBlock = ConfigInterpreter.getBackgroundBlockState(enumerate);
			String bgText = I18n.translateToLocal(backgroundBlock.getBlock().getUnlocalizedName() + ".name");
	    	String oreText = I18n.translateToLocal(this.getUnlocalizedName() + ".name");
	    	nameText = oreText + " (" + bgText + ")";
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return nameText;
    }
	
	@Override
	public void registerModels()
	{
		String fullName = ConfigInterpreter.getFullEnumeratedName(enumerate);
		String[] nameTester = fullName.split("_");
		String realName = null;
		
		if (fullName.contains("_ore"))					
			realName = name.startsWith("lit_") ? "lit_" + fullName : fullName;
		else
			realName = fullName.replaceAll(nameTester[0], name);
		
		Main.proxy.registerVariantRenderer(Item.getItemFromBlock(this), 0, realName);
	}
		
}
