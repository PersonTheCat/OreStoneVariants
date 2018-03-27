package personthecat.mod.util;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import personthecat.mod.world.gen.WorldGenCustomOres;

public class VariantOnly implements Predicate<IBlockState>
{
    private final IBlockState blockState;

    public VariantOnly(IBlockState blockState)
    {
        this.blockState = blockState;
    }

    public static VariantOnly forBlockState(IBlockState blockState)
    {
    	return new VariantOnly(blockState);
    }

    public boolean apply(@Nullable IBlockState yuzMi)
    {
    	return yuzMi != null && yuzMi == this.blockState;
    }
}
