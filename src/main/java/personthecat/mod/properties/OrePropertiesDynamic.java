package personthecat.mod.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import personthecat.mod.objects.model.DynamicModelBaker;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.CommonMethods;
import personthecat.mod.util.handlers.CustomPropertyGenerator;

import static personthecat.mod.Main.logger;

public class OrePropertiesDynamic extends OreProperties
{
	public OrePropertiesDynamic(String registryName)
	{
		this.dropProperties = new DropProperties[] { new DropProperties() };
		this.dropProperties[0].setFullDropSilkTouchLookup(registryName);
		this.name = registryName.replaceAll(":", "_");
		this.modName = dropProperties[0].dropSilkTouchLookup.getResourceDomain();
		this.inUse = true;
		
		register();
	}
	
	/**
	 * Make sure the result exists at this point.
	 */
	public void createRecipeProperties()
	{
		ItemStack result = FurnaceRecipes.instance().getSmeltingResult(dropProperties[0].getDropSilkTouchStack());
		
		String resultName = result.getItem().getRegistryName().toString();
		int resultMeta = result.getItemDamage();
		int quantity = result.getCount();
		float xp = FurnaceRecipes.instance().getSmeltingExperience(result);
		
		new RecipeProperties(name, resultName, resultMeta, quantity, xp);
	}
	
	@Override
	public List<ItemStack> getDrops(World world, BlockPos pos, IBlockState state, int fortune, DropProperties[] currentDrops, boolean isDense, ItemStack selfStack)
	{
		List<ItemStack> drops = NonNullList.create();
		
		IBlockState ore = getOreState();
		
		ore.getBlock().getDrops((NonNullList) drops, world, pos, ore, fortune);
		
		for (ItemStack stack : drops)
		{
			if (!(stack.getItem() instanceof ItemBlock))
			{
				int quantity = stack.getCount();
				
				if (isDense) quantity = MathHelper.getInt(world.rand, quantity, quantity * 3);
				
				if (fortune > 0) quantity *= (MathHelper.abs(world.rand.nextInt(fortune + 2) - 1) + 1);
				
				stack.setCount(quantity);
			}
		}
		
		return drops;
	}
	
	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune, DropProperties[] currentDrops)
	{
		IBlockState ore = getOreState();
		
		return ore.getBlock().getExpDrop(ore, world, pos, fortune);
	}
	
	@Override
	public String getLanguageKey()
	{
		return getOreState().getBlock().getUnlocalizedName();
	}
	
	@Override
	public String getLocalizedName()
	{
		return dropProperties[0].getDropSilkTouchStack().getDisplayName();
	}
	
	@Override
	public float getHardness(World world, BlockPos pos)
	{
		return getOreState().getBlockHardness(world, pos);
	}
	
	@Override
	public int getLevel()
	{
		IBlockState ore = getOreState();
		
		return ore.getBlock().getHarvestLevel(ore);
	}
	
	@Override
	public float getLightLevel()
	{
		return getOreState().getLightValue();
	}
	
	@Override
	public String getOriginalTexture()
	{
		return "assets/minecraft/textures/items/string.png";
	}
	
	private static ResourceLocation guessModelJsonLocation(ModelResourceLocation fromLocation)
	{
		return new ResourceLocation(fromLocation.getResourceDomain() + ":/models/block/" + fromLocation.getResourcePath() + ".json");
	}
}