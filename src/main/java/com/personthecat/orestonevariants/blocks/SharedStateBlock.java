package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.world.WorldInterceptor;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;

/**
 * This class is designed to circumvent the requirement that block state properties be defined at
 * compile time. Creating these properties dynamically, without changing the source code in
 * {@link Block}, requires that some fields be initialized before calling super. We work around
 * this by keeping pre-init data inside of thread-local storage. This guarantees that our data can
 * function safely in multiple threads at a time, if necessary. Once the pre-init phase has completed,
 * we delete any references out of the storage and initialize fields as normal.
 *
 * Todo: provide settings for controlling multiple separate blocks being wrapped. (fg and bg)
 *
 * This is definitely a hack and I don't like it. If you know or can think of a better way to do this,
 * please create an issue on <a href="https://github.com/PersonTheCat/ore_stone_variants/issues">GitHub</a>.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SharedStateBlock extends OreBlock {

    /** Stores all of the data needed before calling <code>super</code>. */
    private static final ThreadLocal<Block[]> cache = new ThreadLocal<>();

    /** The block being emulated by this one. */
    private final Block wrapped;

    /** Creates a new block which copies states from <em>at least one</em> other block. */
    SharedStateBlock(Properties properties, Block base, Block... others) {
        super(preInit(properties, base, others));
        this.wrapped = base;
        setDefaultState(imitateOther(base.getDefaultState()));
        // We are done with these data and can clear them from the cache.
        cache.remove();
    }

    /**
     * Here, we move any data that we need into thread local storage so that it can be use
     * during the init phase for this block only.
     */
    private static Properties preInit(Properties properties, Block b, Block... others) {
        cache.set(ArrayUtils.addAll(others, b));
        return properties;
    }

    /**
     * This function copies block state properties from an array of blocks in the cache. It is
     * safe to call this, as duplicate properties happen to be ignored by the underlying map
     * used by this builder.
     */
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        for (Block b : cache.get()) {
            b.getStateContainer().getProperties().forEach(builder::add);
        }
    }

    /**
     * Updates this block's registry name to match that of the original, plus <code>imitation_</code>.
     *
     * Todo: reimplement format states to fix namespace issues.
     * @return this, for method chaining
     */
    public SharedStateBlock usingImitationRegistry() {
        Objects.requireNonNull(wrapped.getRegistryName(), "Can't imitate registry of unregistered block.");
        return (SharedStateBlock) setRegistryName(osvLocation("imitation_" + wrapped.getRegistryName().getPath()));
    }

    /**
     * Here, we define a couple of helper methods for imitating block states in two directions.
     *
     * This method enables us to set the state of <em>this block</em> to that of another block.
     *
     * @param other The current state of the wrapped block.
     * @return A state of this block which copies the wrapped block's current state.
     */
    private BlockState imitateOther(BlockState other) {
        return imitate(getDefaultState(), other);
    }

    /**
     * In contrast, this method enables us to set the state of <em>another block</em> to emulate
     * <code>this</code>.
     *
     * @param myself the current state of this block.
     * @return A state of the wrapped block copying <code>this</code>.
     */
    private BlockState imitateThis(BlockState myself) {
        return imitate(wrapped.getDefaultState(), myself);
    }

    /**
     * This operation needs to be unchecked because each property wraps a different type. It is
     * still type safe because we're copying properties from another block, where types were
     * already accounted for.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState imitate(BlockState base, BlockState copy) {
        final Collection<Property<?>> validProperties = base.getProperties();
        for (Property prop : copy.getValues().keySet()) {
            if (validProperties.contains(prop)) {
                base = base.with(prop, copy.get(prop));
            }
        }
        return base;
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return wrapped.isLadder(imitateThis(state), world, pos, entity);
    }

    @Override
    public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos) {
        return wrapped.isBurning(imitateThis(state), world, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return wrapped.canHarvestBlock(imitateThis(state), world, pos, player);
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos,
            EntitySpawnPlacementRegistry.PlacementType placement, @Nullable EntityType<?> type) {
        return wrapped.canCreatureSpawn(imitateThis(state), world, pos, placement, type);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return wrapped.canConnectRedstone(imitateThis(state), world, pos, side);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld server, BlockPos pos,
             BlockState state2, LivingEntity entity, int particles) {
        return wrapped.addLandingEffects(imitateThis(state1), server, pos, imitateThis(state2), entity, particles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        return wrapped.addRunningEffects(imitateThis(state), world, pos, entity);
    }

    @Override
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        return wrapped.addHitEffects(imitateThis(state), world, target, manager);
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return wrapped.addDestroyEffects(imitateThis(state), world, pos, manager);
    }

    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing,
            IPlantable plant) {
        return wrapped.canSustainPlant(imitateThis(state), world, pos, facing, plant);
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return wrapped == Blocks.SLIME_BLOCK || wrapped == Blocks.HONEY_BLOCK;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return wrapped.getFlammability(imitateThis(state), world, pos, side);
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return wrapped.isFlammable(imitateThis(state), world, pos, side);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return wrapped.getFireSpreadSpeed(imitateThis(state), world, pos, side);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public PushReaction getPushReaction(BlockState state) {
        return wrapped.getPushReaction(imitateThis(state));
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
        return wrapped.getOpacity(imitateThis(state), world, pos);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean canProvidePower(BlockState state) {
        return wrapped.canProvidePower(imitateThis(state));
    }

    @Override
    public String getTranslationKey() {
        return wrapped.getTranslationKey();
    }

    @Override
    @Deprecated
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
              PlayerEntity player) {
        return wrapped.getPickBlock(imitateThis(state), target, world, pos, player);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moving) {
        final WorldInterceptor interceptor = WorldInterceptor.getInstance();
        try {
            world = interceptor.inWorld(world).intercepting(wrapped, this, pos);
            wrapped.onBlockAdded(imitateThis(state), world, pos, imitateThis(oldState), moving);
        } finally {
            interceptor.clear();
        }
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        wrapped.onBlockClicked(imitateThis(state), world, pos, player);
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        wrapped.onEntityWalk(world, pos, entity);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState state, Direction dir, BlockState facingState, IWorld world,
              BlockPos pos, BlockPos facingPos) {
        final WorldInterceptor interceptor = WorldInterceptor.getInstance();
        try {
            if (world instanceof World) {
                world = interceptor.inWorld((World) world).intercepting(wrapped, this, pos);
            }
            return imitateOther(wrapped.updatePostPlacement(imitateThis(state), dir, facingState, world, pos, facingPos));
        } finally {
            interceptor.clear();
        }
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
             Hand hand, BlockRayTraceResult hit) {
        return wrapped.onBlockActivated(imitateThis(state), world, pos, player, hand, hit);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        wrapped.randomTick(imitateThis(state), world, pos, rand);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        wrapped.tick(imitateThis(state), world, pos, rand);
    }
}