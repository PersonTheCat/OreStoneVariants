package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class RedstoneOreVariant extends BaseOreVariant {
    /** Keeps track of whether this block is currently lit. */
    public static final PropertyBool LIT = PropertyBool.create("lit");
    private static final int DENSE_BIT = 0b10;
    private static final int LIT_BIT = 0b01;

    protected RedstoneOreVariant(OreProperties properties, IBlockState bgBlock) {
        super(properties, bgBlock);
        setTickRandomly(true);
    }

    @Override
    protected IBlockState createDefaultState() {
        return getDefaultState()
            .withProperty(LIT, false)
            .withProperty(DENSE, false);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DENSE, LIT);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0b00;
        if (state.getValue(DENSE)) {
            meta |= DENSE_BIT;
        }
        if (state.getValue(LIT)) {
            meta |= LIT_BIT;
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        if ((meta & DENSE_BIT) == DENSE_BIT) {
            state = state.withProperty(DENSE, true);
        }
        if ((meta & LIT_BIT) == LIT_BIT) {
            state = state.withProperty(LIT, true);
        }
        return state;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(LIT)) {
            world.setBlockState(pos, state.withProperty(LIT, false), 3);
        }
        super.updateTick(world, pos, state, rand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (state.getValue(LIT)) {
            spawnRedstoneParticles(world, pos);
        }
    }

    @Override
    public boolean getTickRandomly() {
        return true;
    }

    @Override // test me
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return getItem(world, pos, state.withProperty(LIT, false));
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(LIT) ? super.getLightValue(state) : 0;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer player) {
        activate(worldIn.getBlockState(pos), worldIn, pos);
        super.onBlockClicked(worldIn, pos, player);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        activate(worldIn.getBlockState(pos), worldIn, pos);
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ) {
        activate(worldIn.getBlockState(pos), worldIn, pos);
        return super.onBlockActivated(worldIn, pos, state, player, handIn, facing, hitX, hitY, hitZ);
    }

    /** Called whenever this block should be transformed into its lit variant. */
    private void activate(IBlockState state, World world, BlockPos pos) {
        if (!world.isRemote) {
            spawnRedstoneParticles(world, pos);
        }
        if (!state.getValue(LIT)) {
            world.setBlockState(pos, state.withProperty(LIT, true), 3);
        }
    }

    /** Imitates the redstone ore particle effect. */
    private static void spawnRedstoneParticles(World world, BlockPos pos) {
        Random random = world.rand;

        double d1 = (double)((float)pos.getX() + random.nextFloat());
        double d2 = (double)((float)pos.getY() + random.nextFloat());
        double d3 = (double)((float)pos.getZ() + random.nextFloat());

        for (int i = 0; i < 6; i++) {
            if (i == 0 && !world.getBlockState(pos.up()).isOpaqueCube()) {
                d2 = (double)pos.getY() + 0.0625D + 1.0D;
            }
            if (i == 1 && !world.getBlockState(pos.down()).isOpaqueCube()) {
                d2 = (double)pos.getY() - 0.0625D;
            }
            if (i == 2 && !world.getBlockState(pos.south()).isOpaqueCube()) {
                d3 = (double)pos.getZ() + 0.0625D + 1.0D;
            }
            if (i == 3 && !world.getBlockState(pos.north()).isOpaqueCube()) {
                d3 = (double)pos.getZ() - 0.0625D;
            }
            if (i == 4 && !world.getBlockState(pos.east()).isOpaqueCube()) {
                d1 = (double)pos.getX() + 0.0625D + 1.0D;
            }
            if (i == 5 && !world.getBlockState(pos.west()).isOpaqueCube()) {
                d1 = (double)pos.getX() - 0.0625D;
            }
            if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1)) {
                world.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}