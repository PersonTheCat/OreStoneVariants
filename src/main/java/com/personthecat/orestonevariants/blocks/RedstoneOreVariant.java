package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.properties.BlockPropertiesHelper;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.CommonMethods;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.function.ToIntFunction;

// Todo: we can probably work on just copy the `lit` property from regular redstone.
//  The original ore's class will be able to do the animation, then.
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RedstoneOreVariant extends BaseOreVariant {

    /** Keeps track of whether this block is currently lit. */
    private static final BooleanProperty LIT = BooleanProperty.create("lit");

    RedstoneOreVariant(OreProperties properties, BlockState bgBlock) {
        super(updateLight(properties), bgBlock);
    }

    private static OreProperties updateLight(OreProperties properties) {
        final BlockPropertiesHelper helper = new BlockPropertiesHelper(properties.block);
        helper.setLightValue(wrapLight(helper.getLightValue()));
        return properties;
    }

    @Override
    protected BlockState createDefaultState() {
        return getDefaultState()
            .with(LIT, false)
            .with(DENSE, false);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DENSE, LIT);
    }

    @Override
    @Deprecated
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (state.get(LIT)) {
            world.setBlockState(pos, state.with(LIT, false), 3);
        } else {
            super.randomTick(state, world, pos, rand);
        }
    }

    @Override
    public boolean ticksRandomly(@NotNull BlockState state) {
        return state.get(LIT);
    }

    @Override
    @Deprecated
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getItem(world, pos, state.with(LIT, false));
    }

    private static ToIntFunction<BlockState> wrapLight(ToIntFunction<BlockState> lightGetter) {
        return s -> s.get(LIT) ? lightGetter.applyAsInt(s) : 0;
    }

    @Override
    @Deprecated
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        activate(state, worldIn, pos);
        super.onBlockClicked(state, worldIn, pos, player);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        activate(worldIn.getBlockState(pos), worldIn, pos);
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @NotNull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            spawnRedstoneParticles(worldIn, pos);
        } else {
            activate(state, worldIn, pos);
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    /** Called whenever this block should be transformed into its lit variant. */
    private static void activate(BlockState state, World world, BlockPos pos) {
        spawnRedstoneParticles(world, pos);
        if (!state.get(LIT)) {
            world.setBlockState(pos, state.with(LIT, true), 3);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (state.get(LIT)) {
            spawnRedstoneParticles(world, pos);
        }
        super.animateTick(state, world, pos, rand);
    }

    /** Imitates the redstone ore particle effect. */
    private static void spawnRedstoneParticles(World world, BlockPos pos) {
        for (Direction d : Direction.values()) {
            final BlockPos offset = pos.offset(d);
            if (!world.getBlockState(offset).isOpaqueCube(world, offset)) {
                final Direction.Axis axis = d.getAxis();
                final double x = axis == Direction.Axis.X ? rsOffset(d.getXOffset()) : world.rand.nextFloat();
                final double y = axis == Direction.Axis.Y ? rsOffset(d.getYOffset()) : world.rand.nextFloat();
                final double z = axis == Direction.Axis.Z ? rsOffset(d.getZOffset()) : world.rand.nextFloat();
                world.addParticle(RedstoneParticleData.REDSTONE_DUST, (double) pos.getX() + x, (double) pos.getY() + y, (double) pos.getZ() + z, 0, 0, 0);
            }
        }
    }

    private static double rsOffset(int i) {
        return (double) i * 0.5625 + 0.5;
    }
}