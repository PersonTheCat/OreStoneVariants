package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.BlockPropertiesHelper;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.List;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BaseOreVariant extends Block implements IForgeBlock {
    /** Contains the standard block properties and any additional values, if necessary. */
    public final OreProperties properties;
    /** A reference to the background block represented by this variant. */
    public final BlockState bgBlock;
    /** Reports whether this block should fall like sand. */
    private final Lazy<Boolean> hasGravity = new Lazy<>(this::testGravity);
    /** Reports whether this block should tick randomly. */
    private final Lazy<Boolean> variantTicksRandomly = new Lazy<>(this::testTickRandomly);
    /** Determines this block's tick rate. */
    private final Lazy<Integer> tickRate = new Lazy<>(this::testTickRate);

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
    private boolean testGravity() {
        return Cfg.bgImitation.get() && bgBlock.getBlock() instanceof FallingBlock;
    }

    /** Determines whether this block should tick randomly. */
    private boolean testTickRandomly() {
        return ticksRandomly || bgBlock.ticksRandomly() || hasGravity.get();
    }

    /** Determines the tick rate for this block. */
    private int testTickRate() {
        final Block bg = bgBlock.getBlock();
        final Block ore = properties.ore.get().getBlock();
        return getMin(bg.tickRate(null), ore.tickRate(null));
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
            .orElse(super.getDrops(state, builder));
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
        return variantTicksRandomly.get();
    }

    @Override
    public int tickRate(IWorldReader world) {
        return tickRate.get();
    }

    public BlockState updatePostPlacement(BlockState state, Direction dir, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
        handleGravity(state, world.getWorld(), pos);
        return state;
    }

    /* --- Gravity features --- */

    /** Determines whether this block should attempt to fall. If so, does. */
    private void handleGravity(BlockState state, World world, BlockPos pos) {
        if (!world.isRemote && hasGravity.get()) {
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
        if (hasGravity.get()) {
            bgBlock.getBlock().animateTick(state, world, pos, rand);
        }
    }
}