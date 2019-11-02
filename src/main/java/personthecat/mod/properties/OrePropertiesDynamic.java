package personthecat.mod.properties;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class OrePropertiesDynamic extends OreProperties
{
    public OrePropertiesDynamic(String registryName)
    {
        this.dropProperties = new DropProperties[] { new DropProperties() };
        this.dropProperties[0].setFullDropSilkTouchLookup(registryName);
        this.name = registryName.replaceAll(":", "_");
        this.modName = dropProperties[0].dropSilkTouchLookup.getNamespace();
        this.inUse = true;

        register();
    }

    @Override
    public List<ItemStack> getDrops(World world, BlockPos pos, IBlockState state, int fortune, DropProperties[] currentDrops, boolean isDense, ItemStack selfStack)
    {
        NonNullList<ItemStack> drops = NonNullList.create();

        IBlockState ore = getOreState();

        ore.getBlock().getDrops(drops, world, pos, ore, fortune);

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
        return getOreState().getBlock().getTranslationKey();
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
        return new ResourceLocation(fromLocation.getNamespace() + ":/models/block/" + fromLocation.getPath() + ".json");
    }
}