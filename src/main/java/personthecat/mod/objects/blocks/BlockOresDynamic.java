package personthecat.mod.objects.blocks;

import java.io.IOException;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.init.BlockInit;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.ShortTrans;

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
	
	@Override
    public String getLocalizedName() //Only for the stats page. The rest is handled in ItemBlockVariants.
    {
    	String nameText = ShortTrans.unformatted(this.getUnlocalizedName() + ".name");
		try 
		{
			IBlockState backgroundBlock = ConfigInterpreter.getBackgroundBlockState(enumerate);
			String bgText = ShortTrans.unformatted(backgroundBlock.getBlock().getUnlocalizedName() + ".name");
	    	String oreText = ShortTrans.unformatted(this.getUnlocalizedName() + ".name");
	    	nameText = oreText + " (" + bgText + ")";
		}
		
		catch (IOException e) {e.printStackTrace();}

		return nameText;
    }
	
	public String getModelName()
	{
		String modelName = ConfigInterpreter.getFullEnumeratedName(enumerate);
		
		if (!modelName.contains("_ore"))
		{
			String[] nameTester = modelName.split("_");
			
			modelName = modelName.replaceAll(nameTester[0], name);
		}
		
		if (NameReader.isDense(this) && !modelName.contains("dense_"))
		{
			modelName = "dense_" + modelName;
		}
		
		return modelName;
	}
	
	//Ultra slop. Redo this.
	@Override
	public void registerModels()
	{
		Main.proxy.registerVariantRenderer(Item.getItemFromBlock(this), 0, getModelName());
	}
		
}
