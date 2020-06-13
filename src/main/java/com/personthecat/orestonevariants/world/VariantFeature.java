package com.personthecat.orestonevariants.world;

import com.google.common.collect.ImmutableMap;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Map;
import java.util.Random;

/**
 * Based on the original WorldGenMinableMod by Pupnewfster.
 */
public class VariantFeature extends WorldGenerator {
//    /** A map containing all of the ores that should spawn given a background and properties. */
//    private static final Lazy<DualMap<IBlockState, OreProperties, IBlockState>> FEATURE_MAP =
//        new Lazy<>(VariantFeature::createFeatureMap);
    /** The chance for dense variants to spawn in the place of regular variants. */
    private static final double DENSE_CHANCE = Cfg.DenseCat.chance;

    private final ImmutableMap<IBlockState, IBlockState> genMap;
    private final float count;
    private final double count16;
    private final float count8;

    public VariantFeature(Map<IBlockState, IBlockState> genMap, int count) {
        this.genMap = ImmutableMap.copyOf(genMap);
        this.count = count;
        this.count16 = count / 16.0;
        this.count8 = count / 8.0F;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        return count < 4 ? smallFeature(world, rand, pos)
            : genFeature(world, rand, pos);
    }

    private boolean smallFeature(World world, Random rand, BlockPos pos) {
        final BlockPos offset = pos.add(8, 0, 8);
        for (int i = 1; i < count; i++) {
            final int x = rand.nextInt(2);
            final int y = rand.nextInt(2);
            final int z = rand.nextInt(2);
            final BlockPos newPos = offset.add(x, y, z);
            trySet(newPos, rand, world);
        }
        return true;
    }

    public boolean genFeature(World world, Random rand, BlockPos pos) {
        final float f = rand.nextFloat() * (float) Math.PI;
        final float sinFCount = MathHelper.sin(f) * count8;
        final float cosFCount = MathHelper.cos(f) * count8;
        final double d0 = (double) (pos.getX() + 8 + sinFCount);
        final double d2 = (double) (pos.getZ() + 8 + cosFCount);

        final double d4Rand = rand.nextInt(3);
        final double d4 = pos.getY() - 2 + d4Rand;

        final double d1Md0Dc = -2 * (double) sinFCount / count;
        final double d5Md4DC = (rand.nextInt(3) - d4Rand) / count;
        final double d3Md2Dc = -2 * (double) cosFCount / count;

        for (int i = 0; i < count; ++i) {
            final double d6 = d0 + d1Md0Dc * i;
            final double d7 = d4 + d5Md4DC * i;
            final double d8 = d2 + d3Md2Dc * i;

            final double d10 = ((double) (MathHelper.sin((float) Math.PI * i / count) + 1.0F) * (rand.nextDouble() * count16) + 1.0) / 2.0;
            // d11 is not the same value?
            final int j = MathHelper.floor(d6 - d10);
            final int k = MathHelper.floor(d7 - d10);
            final int l = MathHelper.floor(d8 - d10);
            final int i1 = MathHelper.floor(d6 + d10);
            final int j1 = MathHelper.floor(d7 + d10);
            final int k1 = MathHelper.floor(d8 + d10);

            final double d6Shift = 0.5 - d6;
            final double d7Shift = 0.5 - d7;
            final double d8Shift = 0.5 - d8;

            for (int l1 = j; l1 <= i1; ++l1) {
                final double d12 = ((double) l1 + d6Shift) / d10;
                final double d12Square = d12 * d12;

                if (d12Square < 1.0) {
                    final double d13Max = 1.0 - d12Square;

                    for (int i2 = k; i2 <= j1; ++i2) {
                        final double d13 = ((double) i2 + d7Shift) / d10;
                        final double d13Square = d13 * d13;

                        if (d13Square < d13Max) {
                            final double d14Max = d13Max - d13Square;

                            for (int j2 = l; j2 <= k1; ++j2) {
                                final double d14 = ((double) j2 + d8Shift) / d10;
                                final double d14Square = d14 * d14;

                                if (d14Square < d14Max) {
                                    final BlockPos newPos = new BlockPos(l1, i2, j2);
                                    trySet(newPos, rand, world);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /** Attempt to retrieve and spawn an appropriate variant. */
    private void trySet(BlockPos pos, Random rand, World world) {
        final IBlockState found = world.getBlockState(pos);
        IBlockState candidate = genMap.get(found);
        if (candidate != null) {
            if (rand.nextDouble() <= DENSE_CHANCE) {
                candidate = candidate.withProperty(BaseOreVariant.DENSE, true);
            }
            world.setBlockState(pos, candidate, 2);
        }
    }

//    private static DualMap<IBlockState, OreProperties, IBlockState> createFeatureMap() {
//        final DualMap.Builder<IBlockState, OreProperties, IBlockState> builder = new DualMap.Builder<>();
//        for (BaseOreVariant block : Main.BLOCKS) {
//            builder.put(block.bgBlock, block.properties, block.getDefaultState());
//        }
//        return builder.build();
//    }
}