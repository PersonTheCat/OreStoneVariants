package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.item.DenseVariantItem;
import com.personthecat.orestonevariants.properties.BlockPropertiesHelper;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.find;
import static com.personthecat.orestonevariants.util.CommonMethods.formatState;
import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

// Todo: This constructor will be easier to read using a builder.
public class BaseOreVariant extends SharedStateBlock implements IForgeBlock {

    /** Contains the standard block properties and any additional values, if necessary. */
    public final OreProperties properties;

    /** A reference to the background block represented by this variant. */
    public final BlockState bgState;

    /** Reports whether this block should fall like sand. */
    private final boolean hasGravity;

    /** Reports whether this block should tick randomly. */
    private final boolean variantTicksRandomly;

    /** The item representing the normal state of this block. */
    public final Lazy<Item> normalItem;

    /** The item representing the dense state of this block. */
    public final Lazy<Item> denseItem;

    /** BlockState properties used by all ore variants. */
    public static final BooleanProperty DENSE = BooleanProperty.create("dense");

    protected BaseOreVariant(OreProperties osvProps, BlockState bgState) {
        this(osvProps, createProperties(osvProps.block, bgState.getBlock()), bgState);
    }

    private BaseOreVariant(OreProperties osvProps, Properties mcProps, BlockState bgState) {
        super(mcProps, createBackground(osvProps, bgState), osvProps.ore.get().getBlock());
        this.properties = osvProps;
        this.bgState = bgState;
        this.hasGravity = initGravity();
        this.variantTicksRandomly = initTickRandomly();
        this.normalItem = new Lazy<>(this::initNormalItem);
        this.denseItem = new Lazy<>(this::initDenseItem);
        setDefaultState(createDefaultState());
        setRegistryName(createName());
    }

    /** Determines the most appropriate child class to spawn for this configuration. */
    public static BaseOreVariant of(OreProperties properties, BlockState bgBlock) {
        return properties.name.equals("redstone_ore")
            ? new RedstoneOreVariant(properties, bgBlock)
            : new BaseOreVariant(properties, bgBlock);
    }

    /** Decides whether to merge block properties for this ore. */
    private static Block.Properties createProperties(Block.Properties ore, Block bgBlock) {
        return Cfg.bgImitation.get() ? BlockPropertiesHelper.merge(ore, bgBlock) : ore;
    }

    /** Determines which block we are imitating, if any. */
    private static Block createBackground(OreProperties properties, BlockState bgState) {
        return Cfg.bgImitation.get() ? bgState.getBlock() : new Block(properties.block);
    }

    /** Conditionally generates the default state for this ore. */
    protected BlockState createDefaultState() {
        return getDefaultState().with(DENSE, false);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DENSE);
        super.fillStateContainer(builder);
    }

    /** Generates the full registry name for this ore variant. */
    private ResourceLocation createName() {
        final String bgFormat = formatState(bgState);
        final String fgFormat = properties.name;

        final StringBuilder sb = new StringBuilder(fgFormat);
        if (bgFormat.length() > 0) {
            sb.append('_');
            sb.append(bgFormat);
        }
        return osvLocation(sb.toString());
    }

    /** Determines whether this block should fall like sand. */
    private boolean initGravity() {
        return Cfg.bgImitation.get() && bgState.getBlock() instanceof FallingBlock;
    }

    /** Determines whether this block should tick randomly. */
    private boolean initTickRandomly() {
        return ticksRandomly || bgState.ticksRandomly() || hasGravity;
    }

    /** Locates the item representing the normal variant of this block. */
    private Item initNormalItem() {
        return find(Main.ITEMS, i -> !i.isDense() && i.getBlock().equals(this))
            .orElseThrow(() -> runExF("Item for {} was not registered correctly.", this));
    }

    /** Locates the item representing the dense variant of this block.  */
    private Item initDenseItem() {
        return find(Main.ITEMS, i -> i.isDense() && i.getBlock().equals(this))
            .orElseThrow(() -> runExF("Dense item for {} was not registered correctly.", this));
    }

    private ItemStack getStack(BlockState state) {
        return new ItemStack(state.get(DENSE) ? denseItem.get() : normalItem.get());
    }

    /** Returns a stack containing the background ore block represented by this block. */
    public ItemStack getOreStack() {
        return new ItemStack(properties.ore.get().getBlock());
    }

    @Override
    public Block getBlock() {
        return Cfg.bgImitation.get() ? bgState.getBlock() : this;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        if (bgState.getMaterial() != Material.ROCK && Cfg.bgImitation.get()) {
            return bgState.canHarvestBlock(world, pos, player);
        }
        return super.canHarvestBlock(state, world, pos, player);
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return getBlock() == Blocks.SLIME_BLOCK || getBlock() == Blocks.HONEY_BLOCK;
    }

    @NotNull
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public PushReaction getPushReaction(@NotNull BlockState state) {
        if (bgState.getBlock().equals(Blocks.OBSIDIAN) && Cfg.bgImitation.get()) {
            return PushReaction.BLOCK; // There's a special exemption in PistonBlock.
        }
        return super.getPushReaction(state);
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return properties.translationKey.orElse(properties.ore.get().getBlock().getTranslationKey());
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(state.get(DENSE) ? denseItem.get() : normalItem.get());
    }

    @NotNull
    @Override
    public Item asItem() {
        return normalItem.get();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canRenderInLayer(RenderType layer) {
        return layer == getBgLayer() || layer == getFgLayer();
    }

    @OnlyIn(Dist.CLIENT)
    public RenderType getBgLayer() {
        return RenderTypeLookup.func_239221_b_(bgState);
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getFgLayer() {
        return Cfg.translucentTextures.get() ? RenderType.getTranslucent() : RenderType.getCutoutMipped();
    }

    @NotNull
    @Override
    @Deprecated
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getRenderShape(@NotNull BlockState state, @NotNull IBlockReader worldIn, @NotNull BlockPos pos) {
        return bgState.getShape(worldIn, pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean isTransparent(@NotNull BlockState state) {
        return Cfg.translucentTextures.get() || (Cfg.bgImitation.get() && bgState.isTransparent());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(@NotNull BlockState state, @NotNull BlockState next, @NotNull Direction dir) {
        return bgState.isSideInvisible(next, dir);
    }

    @NotNull
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootContext.Builder builder) {
        final List<ItemStack> items = getBaseDrops(state, builder);
        handleDense(items, state, builder);
        return handleSelfDrops(items, state, hasSilkTouch(builder));
    }

    /** Substitutes drops from the lookup loot table with those of a raw table, if applicable. */
    private List<ItemStack> getBaseDrops(BlockState state, LootContext.Builder builder) {
        final LootContext ctx = builder
            .withParameter(LootParameters.BLOCK_STATE, state)
            .build(LootParameterSets.BLOCK);
        return properties.drops.get()
            .map(loot -> loot.generate(ctx))
            .orElseGet(() -> super.getDrops(state, builder));
    }

    /** Generates additional loot, if applicable */
    private void handleDense(List<ItemStack> items, BlockState state, LootContext.Builder builder) {
        if (state.get(DENSE)) {
            int count = Cfg.denseDropMultiplier.get();
            if (Cfg.randomDropCount.get()) {
                count = RANDOM.nextInt(count + 1);
            }
            if (count < Cfg.denseDropMultiplierMin.get()) {
                count = Cfg.denseDropMultiplierMin.get();
            }
            for (int i = 0; i < count; i++) {
                items.addAll(getBaseDrops(state, builder));
            }
        }
    }

    /** Replaces the original background ore drop with this block, if applicable. */
    private List<ItemStack> handleSelfDrops(List<ItemStack> items, BlockState state, boolean silkTouch) {
        if (Cfg.variantsDrop.get() || (silkTouch && Cfg.variantsSilkTouch.get())) {
            items.replaceAll(item -> {
                if (item.isItemEqual(getOreStack())) {
                    return getStack(state);
                } else {
                    return item;
                }
            });
        }
        // Correcting the duplicate dense drops. Better algorithm?
        return state.get(DENSE) ? removeDuplicateDense(items) : items;
    }

    // Todo: copy logic from 1.12
    /** Removes any duplicate dense variants from the input stack in a new list. */
    private List<ItemStack> removeDuplicateDense(List<ItemStack> items) {
        final List<ItemStack> newList = new ArrayList<>();
        boolean denseFound = false;
        for (ItemStack item : items) {
            final boolean dense = item.getItem() instanceof DenseVariantItem;
            if (!(dense && denseFound)) {
                newList.add(item);
            }
            denseFound = denseFound || dense;
        }
        return newList;
    }

    /** Determines whether silk touch is used in the current context. */
    private boolean hasSilkTouch(LootContext.Builder builder) {
        final ItemStack tool = builder.get(LootParameters.TOOL);
        if (tool == null) {
            return false;
        }
        return EnchantmentHelper.getEnchantments(tool).containsKey(Enchantments.SILK_TOUCH);
    }

    @Override
    public int getExpDrop(BlockState state, @NotNull IWorldReader reader, @NotNull BlockPos pos,
              int fortune, int silktouch) {
        final Random rand = reader instanceof World ? ((World) reader).rand : RANDOM;
        final int xp = properties.xp.map(range -> range.rand(rand))
            .orElseGet(() -> properties.ore.get().getExpDrop(reader, pos, fortune, silktouch));
        return state.get(DENSE) ? xp * 2 : xp;
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void onBlockAdded(@NotNull BlockState state, World world, @NotNull BlockPos pos,
             @NotNull BlockState oldState, boolean moving) {
        world.getPendingBlockTicks().scheduleTick(pos, this, 2);
    }

    @Override
    public boolean ticksRandomly(@NotNull BlockState state) {
        return variantTicksRandomly;
    }

    @NotNull
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(@NotNull BlockState state, @NotNull Direction dir,
              @NotNull BlockState facingState, IWorld world, @NotNull BlockPos pos, @NotNull BlockPos facingPos) {
        world.getPendingBlockTicks().scheduleTick(pos, this, 2);
        return state;
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void tick(@NotNull BlockState state, @NotNull ServerWorld world, @NotNull BlockPos pos,
            @NotNull Random rand) {
        handleGravity(state, world, pos);
    }

    /** Determines whether this block should attempt to fall. If so, does. */
    private void handleGravity(BlockState state, World world, BlockPos pos) {
        if (!world.isRemote && hasGravity) {
            checkFallable(state, world, pos);
        }
    }

    /** From FallingBlock.java: returns whether this block can fall at the current position. */
    private void checkFallable(BlockState state, World world, BlockPos pos) {
        if (pos.getY() > 0 && canFallThrough(world.getBlockState(pos.down()))) {
            world.addEntity(new FallingBlockEntity(
                world, (double) pos.getX() + 0.5, (double) pos.getY(), (double) pos.getZ() + 0.5, state));
        }
    }

    /** From FallingBlock.java: returns whether this block is suitable to fall through. */
    private static boolean canFallThrough(BlockState state) {
        final Material mat = state.getMaterial();
        return state.isAir() || state.isIn(BlockTags.FIRE) || mat.isLiquid() || mat.isReplaceable();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(@NotNull BlockState state, @NotNull World world, @NotNull BlockPos pos,
            @NotNull Random rand) {
        bgState.getBlock().animateTick(state, world, pos, rand);
    }
}