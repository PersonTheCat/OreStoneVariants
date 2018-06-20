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

	public BlockOresDynamic(int enumerate, String oreToImitate) 
	{
		super(ConfigInterpreter.getFullCorrectedEnumeratedName(enumerate, oreToImitate));
		
		setBackgroundInformation(enumerate);
	}
	
	//Mild hax
	public BlockOresDynamic(int enumerate, String fullName, boolean isVariant)
	{
		super(fullName);
		
		setBackgroundInformation(enumerate);
	}
	
	private void setBackgroundInformation(int enumerate)
	{
		this.enumerate = enumerate;
		
		setBackgroundBlockState(ConfigInterpreter.getBackgroundBlockState(enumerate));
		setBackgroundModelLocation(ConfigInterpreter.getBackgroundModelLocation(enumerate));
	}
	
	@Override
    public String getLocalizedName() //Only for the stats page. The rest is handled in ItemBlockVariants.
    {
    	String nameText = ShortTrans.unformatted(this.getUnlocalizedName() + ".name");

		IBlockState backgroundBlock = getBackgroundBlockState();
		String bgText = ShortTrans.unformatted(backgroundBlock.getBlock().getUnlocalizedName() + ".name");
    	String oreText = ShortTrans.unformatted(this.getUnlocalizedName() + ".name");
    	nameText = oreText + " (" + bgText + ")";

		return nameText;
    }
	
	public int getOriginalEnumeration()
	{
		return enumerate;
	}

	@Override
	public void registerModels()
	{
		Main.proxy.registerVariantRenderer(Item.getItemFromBlock(this), 0, getOriginalName());
	}
		
}
