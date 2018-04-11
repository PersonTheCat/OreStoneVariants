package personthecat.mod.util;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.state.IBlockState;

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
