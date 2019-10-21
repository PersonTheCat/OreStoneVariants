package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.BlockPropertiesHelper;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.List;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BaseOreVariant extends OreBlock implements IForgeBlock {
    /** Contains the standard block properties and any additional values, if necessary. */
    public final OreProperties properties;
    /** A reference to the background block represented by this variant. */
    public final BlockState bgBlock;
    /** A reference to bgBlock that only exists if bgImitation is enabled. */
    private final Block imitationHandler;
    /** Reports whether this block should fall like sand. */
    private final boolean hasGravity;
    /** Reports whether this block should tick randomly. */
    private final boolean variantTicksRandomly;
    /** Determines this block's tick rate. */
    private final int tickRate;
    /** The item representing the normal state of this block. */
    private final Lazy<Item> normalItem = new Lazy<>(this::initNormalItem);
    /** The item representing the dense state of this block. */
    private final Lazy<Item> denseItem = new Lazy<>(this::initDenseItem);

    /** The render layer used by variant overlays. */
    private static final BlockRenderLayer LAYER = Cfg.translucentTextures.get()
        ? BlockRenderLayer.TRANSLUCENT
        : BlockRenderLayer.CUTOUT_MIPPED;

    /** BlockState properties used by all ore variants. */
    public static final BooleanProperty DENSE = BooleanProperty.create("dense");

    /** Primary constructor. */
    protected BaseOreVariant(OreProperties properties, BlockState bgBlock) {
        super(createProperties(properties.block, bgBlock));
        this.properties = properties;
        this.bgBlock = bgBlock;
        this.imitationHandler = initImitationBlock();
        this.hasGravity = initGravity();
        this.variantTicksRandomly = initTickRandomly();
        this.tickRate = initTickRate();
        setDefaultState(createDefaultState());
        setRegistryName(createName());
    }

    /** Determines the most appropriate child class to spawn for this configuration. */
    public static BaseOreVariant of(OreProperties properties, BlockState bgBlock) {
        return properties.location.getPath().equals("redstone_ore")
            ? new RedstoneOreVariant(properties, bgBlock)
            : new BaseOreVariant(properties, bgBlock);
    }

    /* --- Immediate block setup --- */

    /** Decides whether to merge block properties for this ore. */
    private static Block.Properties createProperties(Block.Properties ore, BlockState bgBlock) {
        return Cfg.bgImitation.get() ? BlockPropertiesHelper.merge(ore, bgBlock) : ore;
    }

    /** Conditionally generates the default state for this ore. */
    protected BlockState createDefaultState() {
        return getDefaultState().with(DENSE, false);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DENSE);
    }

    /* --- Registry name && functions --- */

    /** Generates the full registry name for this ore variant. */
    private ResourceLocation createName() {
        return osvLocation(f("{}{}", properties.location.getPath(), createAffix()));
    }

    /** Generates the second half of this ore's registry name, representing its background block. */
    private String createAffix() {
        if (bgBlock.getBlock().equals(Blocks.STONE)) {
            return "";
        }
        final ResourceLocation bgLocation = bgBlock.getBlock().getRegistryName();
        if (bgLocation.getNamespace().equals("minecraft")) {
            final String path = bgLocation.getPath();
            return path.equals("stone") ? "" : f("_{}", path);
        }
        return f("{}_{}", bgLocation.getNamespace(), bgLocation.getPath());
    }

    /* --- Initialize lazy values --- */

    /** Determines whether this block should fall like sand. */
    private boolean initGravity() {
        return Cfg.bgImitation.get() && bgBlock.getBlock() instanceof FallingBlock;
    }

    /** Determines whether this block should tick randomly. */
    private boolean initTickRandomly() {
        return ticksRandomly || bgBlock.ticksRandomly() || hasGravity;
    }

    /** Determines the tick rate for this block. */
    private int initTickRate() {
        final Block bg = bgBlock.getBlock();
        final Block ore = properties.ore.get().getBlock();
        return getMin(bg.tickRate(null), ore.tickRate(null));
    }

    /** Returns the background block, if bgImitation is enabled. */
    private Block initImitationBlock() {
        // if bgImitation -> use the background block : use a standard block, equivalent to super.
        return Cfg.bgImitation.get() ? bgBlock.getBlock() : new Block(properties.block);
    }

    /** Locates the item representing the normal variant of this block. */
    private Item initNormalItem() {
        return find(Main.ITEMS, i -> i.getRegistryName().equals(getRegistryName()))
            .orElseThrow(() -> runExF("Item for {} was not registered correctly.", this));
    }

    /** Locates the item representing the dense variant of this block.  */
    private Item initDenseItem() {
        return find(Main.ITEMS, i -> i.getRegistryName().getPath().equals("dense_" + getRegistryName().getPath()))
            .orElseThrow(() -> runExF("Dense item for {} was not registered correctly.", this));
    }

    /* --- Helpful BOV functions --- */

    /** Returns a stack containing this block. */
    private ItemStack getStack() {
        return new ItemStack(this);
    }

    /** Returns a stack containing the background ore block represented by this block. */
    private ItemStack getBackgroundStack() {
        return new ItemStack(properties.ore.get().getBlock());
    }

    /* --- Background block imitation --- */

    @Override
    public Block getBlock() {
        // In most cases, returning the background block for IForgeBlock#getBlock
        // will allow IForgeBlock's methods to piggyback off of it, thus requiring
        // fewer manual method overrides. Any methods that depend on the current
        // BlockState will still require manual overrides.
        return Cfg.bgImitation.get() ? bgBlock.getBlock() : this;
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return imitationHandler.isLadder(state, world, pos, entity);
    }

    @Override
    public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos) {
        return imitationHandler.isBurning(state, world, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return imitationHandler.canHarvestBlock(state, world, pos, player);
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType placement, EntityType<?> type) {
        return imitationHandler.canCreatureSpawn(state, world, pos, placement, type);
    }

    @Override
    public void beginLeaveDecay(BlockState state, IWorldReader world, BlockPos pos) {
        imitationHandler.beginLeaveDecay(state, world, pos);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.canConnectRedstone(state, world, pos, side);
    }

    @Override
    public boolean isFoliage(BlockState state, IWorldReader world, BlockPos pos) {
        return imitationHandler.isFoliage(state, world, pos);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld server, BlockPos pos, BlockState state2, LivingEntity entity, int particles) {
        return imitationHandler.addLandingEffects(state1, server, pos, state2, entity, particles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        return imitationHandler.addRunningEffects(state, world, pos, entity);
    }

    @Override
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        return imitationHandler.addHitEffects(state, world, target, manager);
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return imitationHandler.addDestroyEffects(state, world, pos, manager);
    }

    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plant) {
        return imitationHandler.canSustainPlant(state, world, pos, facing, plant);
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return getBlock() == Blocks.SLIME_BLOCK;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.getFlammability(state, world, pos, side);
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.isFlammable(state, world, pos, side);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.getFireSpreadSpeed(state, world, pos, side);
    }

    @Override
    public int getExpDrop(BlockState state, IWorldReader reader, BlockPos pos, int fortune, int silktouch) {
        final int xp = properties.xp.map(range -> range.rand(reader.getDimension().getWorld().rand))
            .orElseGet(() -> properties.ore.get().getExpDrop(reader, pos, fortune, silktouch));
        return state.get(DENSE) ? xp * 2 : xp;
    }

    /* --- Don't imitate these --- */

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(state.get(DENSE) ? denseItem.get() : normalItem.get());
    }

    @Override
    public Item asItem() {
        return normalItem.get();
    }

    /* --- Rendering --- */

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == LAYER;
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return bgBlock.getShape(worldIn, pos);
    }

    @Override
    public boolean isSolid(BlockState state) {
        return bgBlock.isSolid();
    }

    /* --- Handle block drops --- */

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        final List<ItemStack> items = getBaseDrops(state, builder);
        return handleSilkTouch(handleDense(items, state, builder));
    }

    /** Substitutes drops from the lookup loot table with those of a raw table, if applicable. */
    private List<ItemStack> getBaseDrops(BlockState state, LootContext.Builder builder) {
        final LootContext ctx = builder
            .withParameter(LootParameters.BLOCK_STATE, state)
            .build(LootParameterSets.BLOCK);
        return properties.drops
            .map(loot -> loot.generate(ctx))
            .orElseGet(() -> super.getDrops(state, builder));
    }

    /** Generates additional loot, if applicable */
    private List<ItemStack> handleDense(List<ItemStack> items, BlockState state, LootContext.Builder builder) {
        if (state.get(DENSE) && !dropsBgBlock(items)) {
            for (int i = 0; i < builder.getWorld().rand.nextInt(3); i++) {
                items.addAll(getBaseDrops(state, builder));
            }
        }
        return items;
    }

    /** Replaces the original silk touch drop with this block, if applicable. */
    private List<ItemStack> handleSilkTouch(List<ItemStack> items) {
        items.replaceAll(item -> {
            if (item.isItemEqual(getBackgroundStack())) {
                return getStack();
            } else {
                return item;
            }
        });
        return items;
    }

    /** Returns whether the background block is present in the input item stack. */
    private boolean dropsBgBlock(List<ItemStack> items) {
        final ItemStack bgStack = getBackgroundStack();
        return find(items, item -> item.isItemEqual(bgStack)).isPresent();
    }

    /* --- Block interface events --- */

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moving) {
        handleGravity(state, world.getWorld(), pos);
    }

    /* --- Block updates --- */

    @Override
    public boolean ticksRandomly(BlockState state) {
        return variantTicksRandomly;
    }

    @Override
    public int tickRate(IWorldReader world) {
        return tickRate;
    }

    public BlockState updatePostPlacement(BlockState state, Direction dir, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
        handleGravity(state, world.getWorld(), pos);
        return state;
    }

    /* --- Gravity features --- */

    /** Determines whether this block should attempt to fall. If so, does. */
    private void handleGravity(BlockState state, World world, BlockPos pos) {
        if (!world.isRemote && hasGravity) {
            checkFallable(state, world, pos);
        }
    }

    /** From FallingBlock.java: returns whether this block can fall at the current position. */
    private void checkFallable(BlockState state, World world, BlockPos pos) {
        if (pos.getY() > 0 && canFallThrough(world.getBlockState(pos.down()))) {
            world.addEntity(new FallingBlockEntity(world, (double) pos.getX() + 0.5, (double) pos.getY(), (double) pos.getZ() + 0.5, state));
        }
    }

    /** From FallingBlock.java: returns whether this block is suitable to fall through. */
    private static boolean canFallThrough(BlockState state) {
        final Material mat = state.getMaterial();
        return state.isAir() || mat.isLiquid() || mat.isReplaceable();
    }

    /* --- Animations --- */

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (hasGravity) {
            bgBlock.getBlock().animateTick(state, world, pos, rand);
        }
    }
}