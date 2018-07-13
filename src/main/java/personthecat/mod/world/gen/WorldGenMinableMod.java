package personthecat.mod.world.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

/**
 * Mod of the vanilla WorldGenMinable class by
 * @author pupnewfster
 */
public class WorldGenMinableMod extends WorldGenerator
{
    private final Map<IBlockState, IBlockState> genStateMap, denseStateMap;
    private final double denseChance, count16;
    private final float count8;
    private final int count;

    public WorldGenMinableMod(Map<IBlockState, IBlockState> genStateMap, int count)
    {
        this(genStateMap, count, new HashMap<>(), 0);
    }
    
    public WorldGenMinableMod(Map<IBlockState, IBlockState> genStateMap, int count, Map<IBlockState, IBlockState> denseStateMap, double denseChance)
    {
    	this.genStateMap = genStateMap;
    	this.count = count;
    	this.count8 = this.count / 8.0F;
        this.count16 = this.count / 16.0D;
        this.denseChance = denseChance;
        this.denseStateMap = denseStateMap;
    }

    @Override //Optimized and edited version of WorldGenMinable's generate method
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        if (count < 4) {
            //Slightly different calculations that increase performance for very small vein sizes
            BlockPos blockpos = position.add(8, 0, 8);
            for (int j = 1; j < count; j++) {
                trySet(blockpos.add(rand.nextInt(2), rand.nextInt(2), rand.nextInt(2)), rand, worldIn);
            }
            return true;
        }
        float f = rand.nextFloat() * (float) Math.PI;
        float sinFCount = MathHelper.sin(f) * count8, cosFCount = MathHelper.cos(f) * count8;
        double d0 = (double) (position.getX() + 8 + sinFCount);
        double d2 = (double) (position.getZ() + 8 + cosFCount);

        double d4rand = rand.nextInt(3);

        double d4 = position.getY() - 2 + d4rand;

        //potentially long complex doubles so precalculate them instead of recalculating each loop
        double d1Md0Dc = -2 * (double) sinFCount / (float) this.count;
        double d5Md4Dc = (rand.nextInt(3) - d4rand) / (float) this.count;
        double d3Md2Dc = -2 * (double) cosFCount / (float) this.count;

        for (int i = 0; i < this.count; ++i)
        {
            double d6 = d0 + d1Md0Dc * i;
            double d7 = d4 + d5Md4Dc * i;
            double d8 = d2 + d3Md2Dc * i;
            //precalculate d10/2.0D as that is the only time it ever was used
            double d10Precalc = ((double) (MathHelper.sin((float) Math.PI * i / (float) this.count) + 1.0F) * (rand.nextDouble() * count16) + 1.0D) / 2.0D;
            //d11 is the same value as d10 so just replace d11 references and only calculate it once
            int j = MathHelper.floor(d6 - d10Precalc);
            int k = MathHelper.floor(d7 - d10Precalc);
            int l = MathHelper.floor(d8 - d10Precalc);
            int i1 = MathHelper.floor(d6 + d10Precalc);
            int j1 = MathHelper.floor(d7 + d10Precalc);
            int k1 = MathHelper.floor(d8 + d10Precalc);

            double d6shift = 0.5D - d6;
            double d7shift = 0.5D - d7;
            double d8shift = 0.5D - d8;

            for (int l1 = j; l1 <= i1; ++l1)
            {
                double d12 = ((double) l1 + d6shift) / d10Precalc;
                double d12square = d12 * d12;
                if (d12square < 1.0D)
                {
                    double d13max = 1.0D - d12square;
                    for (int i2 = k; i2 <= j1; ++i2)
                    {
                        double d13 = ((double) i2 + d7shift) / d10Precalc;
                        double d13square = d13 * d13;
                        if (d13square < d13max)
                        {
                            double d14max = d13max - d13square;
                            for (int j2 = l; j2 <= k1; ++j2)
                            {
                                double d14 = ((double) j2 + d8shift) / d10Precalc;
                                if (d14 * d14 < d14max)
                                {
                                    trySet(new BlockPos(l1, i2, j2), rand, worldIn);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void trySet(BlockPos blockpos, Random rand, World worldIn) {
        IBlockState foundState = worldIn.getBlockState(blockpos);
        if (this.genStateMap.containsKey(foundState))
        {
            IBlockState newState;
            if (rand.nextDouble() < denseChance) {
                newState = this.denseStateMap.getOrDefault(foundState, this.genStateMap.get(foundState));
            } else {
                newState = this.genStateMap.get(foundState);
            }
            worldIn.setBlockState(blockpos, newState, 2);
        }
    }
}