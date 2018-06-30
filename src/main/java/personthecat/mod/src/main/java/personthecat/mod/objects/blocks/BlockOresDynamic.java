package personthecat.mod.objects.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.ShortTrans;

public class BlockOresDynamic extends BlockOresBase implements IHasModel
{
	private int enumerate;

	public BlockOresDynamic(int enumerate, String oreToImitate) 
	{
		super(ConfigInterpreter.getFullCorrectedEnumeratedName(enumerate, oreToImitate));

		setBackgroundInformation(enumerate);
	}
	
	private void setBackgroundInformation(int enumerate)
	{
		this.enumerate = enumerate;
		
		this.bgBlockStates = new IBlockState[1];
		this.bgModelLocations = new ModelResourceLocation[1];
		
		setBackgroundBlockState(ConfigInterpreter.getBackgroundBlockState(enumerate));
		
		Main.proxy.setBackgroundModelLocation(this, enumerate);
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
	public BlockOresBase chooseConstructor(String newName)
	{
		return new BlockOresDynamic(getOriginalEnumeration(), newName);
	}

	@Override
	public void registerModels()
	{
		Main.proxy.registerVariantRenderer(getItem(), 0, getOriginalName());
	}
		
}
