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
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
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
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BaseOreVariant extends OreBlock implements IForgeBlock {
    // ----------------------- Todo list: ------------------------- //
    //  * Check tick rate. Removed?
    //  * Check leaf decay. Have to do this manually now?
    //  * Check isSolid. Alternative? Only in block state?
    //  * Look for new block methods that should be overridden.
    // -----------------------  End list  ------------------------- //

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
    /** The item representing the normal state of this block. */
    public final Lazy<Item> normalItem = new Lazy<>(this::initNormalItem);
    /** The item representing the dense state of this block. */
    public final Lazy<Item> denseItem = new Lazy<>(this::initDenseItem);
    private final Lazy<RenderType> bgLayer = new Lazy<>(this::getBgLayer);

    /** The render layer used by variant overlays. */
    public static final RenderType LAYER = Cfg.translucentTextures.get()
        ? RenderType.getTranslucent()
        : RenderType.getCutoutMipped();

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
        setDefaultState(createDefaultState());
        setRegistryName(createName());
    }

    /** Determines the most appropriate child class to spawn for this configuration. */
    public static BaseOreVariant of(OreProperties properties, BlockState bgBlock) {
        return properties.name.equals("redstone_ore")
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

    /* --- Post block registry setup */

    // This must be handled after registry delegates are setup.
    public void updatePostRegister() {
        if (this.delegate.name() == null) {
            throw runEx("Call to #updatePostRegister before block registry.");
        }
        RenderTypeLookup.setRenderLayer(this, this::canRenderInLayer);
    }

    /* --- Registry name && functions --- */

    /** Generates the full registry name for this ore variant. */
    private ResourceLocation createName() {
        final String bgFormat = formatState(bgBlock);
        final String fgFormat = properties.name;

        final StringBuilder sb = new StringBuilder(fgFormat);
        if (bgFormat.length() > 0) {
            sb.append('_');
            sb.append(bgFormat);
        }
        return osvLocation(sb.toString());
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

    /** Returns the background block, if bgImitation is enabled. */
    private Block initImitationBlock() {
        // if bgImitation -> use the background block : use a standard block, equivalent to super.
        return Cfg.bgImitation.get() ? bgBlock.getBlock() : new SharedStateBlock(this, properties.block);
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
    public ItemStack getStack() {
        return new ItemStack(this);
    }

    public ItemStack getStack(BlockState state) {
        return new ItemStack(state.get(DENSE) ? denseItem.get() : normalItem.get());
    }

    /** Returns a stack containing the background ore block represented by this block. */
    public ItemStack getBackgroundStack() {
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
        return imitationHandler.isLadder(imitate(state), world, pos, entity);
    }

    @Override
    public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos) {
        return imitationHandler.isBurning(imitate(state), world, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return imitationHandler.canHarvestBlock(imitate(state), world, pos, player);
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType placement, EntityType<?> type) {
        return imitationHandler.canCreatureSpawn(imitate(state), world, pos, placement, type);
    }

//    @Override
//    public void beginLeaDecay(BlockState state, IWorldReader world, BlockPos pos) {
//        imitationHandler.beginLeaveDecay(imitate(state), world, pos);
//    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.canConnectRedstone(imitate(state), world, pos, side);
    }

//    @Override
//    public boolean isFoliage(BlockState state, IWorldReader world, BlockPos pos) {
//        return imitationHandler.isFoliage(imitate(state), world, pos);
//    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld server, BlockPos pos, BlockState state2, LivingEntity entity, int particles) {
        return imitationHandler.addLandingEffects(imitate(state1), server, pos, imitate(state2), entity, particles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        return imitationHandler.addRunningEffects(imitate(state), world, pos, entity);
    }

    @Override
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        return imitationHandler.addHitEffects(imitate(state), world, target, manager);
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return imitationHandler.addDestroyEffects(imitate(state), world, pos, manager);
    }

    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plant) {
        return imitationHandler.canSustainPlant(imitate(state), world, pos, facing, plant);
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return getBlock() == Blocks.SLIME_BLOCK || getBlock() == Blocks.HONEY_BLOCK;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.getFlammability(imitate(state), world, pos, side);
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.isFlammable(imitate(state), world, pos, side);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return imitationHandler.getFireSpreadSpeed(imitate(state), world, pos, side);
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        if (bgBlock.getBlock().equals(Blocks.OBSIDIAN) && Cfg.bgImitation.get()) {
            return PushReaction.BLOCK; // There's a special exemption in PistonBlock.
        }
        return imitationHandler.getPushReaction(imitate(state));
    }

    @Override
    public int getExpDrop(BlockState state, IWorldReader reader, BlockPos pos, int fortune, int silktouch) {
        final Random rand = reader instanceof World ? ((World) reader).rand : new Random();
        final int xp = properties.xp.map(range -> range.rand(rand))
            .orElseGet(() -> properties.ore.get().getExpDrop(reader, pos, fortune, silktouch));
        return state.get(DENSE) ? xp * 2 : xp;
    }

    /** To-do: improve syntax for readability. */
    private BlockState imitate(BlockState state) {
        return imitationHandler == bgBlock.getBlock() ? imitationHandler.getDefaultState() : state;
    }

    @Override
    public String getTranslationKey() {
        return properties.ore.get().getBlock().getTranslationKey();
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

    public boolean canRenderInLayer(RenderType layer) {
        return layer == getBgLayer() || layer == LAYER;
    }

    // Todo: doesn't work
    public RenderType getBgLayer() {
        if (bgLayer.computed()) {
            return bgLayer.get();
        }
        return RenderTypeLookup.func_239221_b_(bgBlock);
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return bgBlock.getShape(worldIn, pos);
    }

    @Override
    public boolean isTransparent(BlockState state) {
        return Cfg.translucentTextures.get() || (Cfg.bgImitation.get() && bgBlock.isTransparent());
    }

    /* --- Handle block drops --- */

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        final List<ItemStack> items = getBaseDrops(state, builder);
        handleDense(items, state, builder);
        return handleSelfDrops(items, state, hasSilkTouch(builder));
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
        if (state.get(DENSE)) {
            for (int i = 0; i < builder.getWorld().rand.nextInt(Cfg.denseDropMultiplier.get()); i++) {
                items.addAll(getBaseDrops(state, builder));
            }
        }
        return items;
    }

    /** Replaces the original background ore drop with this block, if applicable. */
    private List<ItemStack> handleSelfDrops(List<ItemStack> items, BlockState state, boolean silkTouch) {
        if (Cfg.variantsDrop.get() || (silkTouch && Cfg.variantsSilkTouch.get())) {
            items.replaceAll(item -> {
                if (item.isItemEqual(getBackgroundStack())) {
                    return getStack(state);
                } else {
                    return item;
                }
            });
        }
        // Correcting the duplicate dense drops. Better algorithm?
        return state.get(DENSE) ? removeDuplicateDense(items) : items;
    }

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
        for (INBT nbt : builder.get(LootParameters.TOOL).getEnchantmentTagList()) {
            final String enchantment = ((CompoundNBT) nbt).getString("id");
            if (enchantment.equals("minecraft:silk_touch")) {
                return true;
            }
        }
        return false;
    }

    /* --- Block interface events --- */

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moving) {
        world.getPendingBlockTicks().scheduleTick(pos, this, 2);
    }

    /* --- Block updates --- */

    @Override
    public boolean ticksRandomly(BlockState state) {
        return variantTicksRandomly;
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction dir, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
        world.getPendingBlockTicks().scheduleTick(pos, this, 2);
        return state;
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        handleGravity(state, world, pos);
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
        return state.isAir() || state.isIn(BlockTags.FIRE) || mat.isLiquid() || mat.isReplaceable();
    }

    /* --- Animations --- */

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        bgBlock.getBlock().animateTick(state, world, pos, rand);
    }
}