package personthecat.mod.objects.blocks;

import java.util.Random;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.CreativeTab;
import personthecat.mod.Main;
import personthecat.mod.util.IChooseEnums;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.IMetaName;
import personthecat.mod.util.handlers.BlockStateGenerator;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class BlockOresEnumeratedQuark extends BlockOresEnumerated implements IChooseEnums
{
	public BlockOresEnumeratedQuark(String name)
	{
		super(name + "_quark");			
	}
	
	@Override
	public PropertyEnum<BlockStateGenerator.State> getEnum()
	{
		return QUARK;
	}
	
	@Override
	public String getDependency()
	{
		return "quark";
	}
}