package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.item.VariantTab;
import com.personthecat.orestonevariants.properties.BlockPropertiesHelper;
import com.personthecat.orestonevariants.properties.DropProperties;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BaseOreVariant extends BlockOre {
    /** Contains the standard block properties and any additional values, if necessary. */
    public final OreProperties properties;
    /** A reference to the background block represented by this variant. */
    public final IBlockState bgBlock;
    /** A reference to bgBlock that only exists if bgImitation is enabled. */
    private final Block imitationHandler;
    /** Reports whether this block should fall like sand. */
    private final boolean hasGravity;
    /** Stores information about the current set of items / experience being dropped. */
    private final List<DropProperties> currentDrops = list();
    /** Tracking whether drops have been prepped for xp sync. Not thread safe. */
    private boolean dropsPrepped = false;
    /** The item representing the normal state of this block. */
    public final Lazy<Item> normalItem = new Lazy<>(this::initNormalItem);

    /** The render layer used by variant overlays. */
    private static final BlockRenderLayer LAYER = Cfg.BlocksCat.translucentTextures
        ? BlockRenderLayer.TRANSLUCENT
        : BlockRenderLayer.CUTOUT_MIPPED;

    /** BlockState properties used by all ore variants. */
    public static final PropertyBool DENSE = PropertyBool.create("dense");

    /** Primary constructor. */
    protected BaseOreVariant(OreProperties properties, IBlockState bgBlock) {
        super(properties.block.getMapColor());
        createProperties(properties.block, bgBlock).cloneInto(this);
        this.properties = properties;
        this.bgBlock = bgBlock;
        this.imitationHandler = initImitationBlock();
        this.hasGravity = initGravity();
        setTickRandomly(needsRandomTick || hasGravity);
        setDefaultState(createDefaultState());
        setRegistryName(createName());
        info("Setting registry name to {}", getRegistryName());
        setCreativeTab(VariantTab.TAB);
    }

    /** Determines the most appropriate child class to spawn for this configuration. */
    public static BaseOreVariant of(OreProperties properties, IBlockState bgBlock) {
        return properties.name.equals("redstone_ore")
            ? new RedstoneOreVariant(properties, bgBlock)
            : new BaseOreVariant(properties, bgBlock);
    }

    /* --- Immediate block setup --- */

    /** Decides whether to merge block properties for this ore. */
    private static BlockPropertiesHelper createProperties(BlockPropertiesHelper ore, IBlockState bgBlock) {
        return Cfg.BlocksCat.bgImitation ? BlockPropertiesHelper.merge(ore.properties, bgBlock) : ore;
    }

    /** Conditionally generates the default state for this ore. */
    protected IBlockState createDefaultState() {
        return getDefaultState().withProperty(DENSE, false);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DENSE);
    }

    @Override
    public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items) {
        for (int i = 0; i < getBlockState().getValidStates().size(); i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DENSE) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        final IBlockState defaultState = getDefaultState();
        return meta == 1
            ? defaultState.withProperty(DENSE, true)
            : defaultState;
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
        return Cfg.BlocksCat.bgImitation && bgBlock.getBlock() instanceof BlockFalling;
    }

    /** Returns the background block, if bgImitation is enabled. */
    private Block initImitationBlock() {
        // if bgImitation -> use the background block : use a standard block, equivalent to super.
        return Cfg.BlocksCat.bgImitation ? bgBlock.getBlock() : properties.ore.get().getBlock();
    }

    /** Locates the item representing the normal variant of this block. */
    private Item initNormalItem() {
        return find(Main.ITEMS, i -> i.getRegistryName().equals(getRegistryName()))
                .orElseThrow(() -> runExF("Item for {} was not registered correctly.", this));
    }

    /* --- Helpful BOV functions --- */

    /** Returns a stack containing this block. */
    public ItemStack getStack() {
        return new ItemStack(this);
    }

    public ItemStack getStack(IBlockState state) {
        return new ItemStack(normalItem.get(), 1, getMetaFromState(state));
    }

    /** Returns a stack containing the background ore block represented by this block. */
    public ItemStack getBackgroundStack() {
        return toStack(properties.ore.get());
    }

    /* --- Background block imitation --- */

    public Block getBlock() {
        // In most cases, returning the background block for IForgeBlock#getBlock
        // will allow IForgeBlock's methods to piggyback off of it, thus requiring
        // fewer manual method overrides. Any methods that depend on the current
        // IBlockState will still require manual overrides.
        return Cfg.BlocksCat.bgImitation ? bgBlock.getBlock() : this;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return imitationHandler.isLadder(imitate(state), world, pos, entity);
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos) {
        return imitationHandler.isBurning(world, pos);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType placement) {
        return imitationHandler.canCreatureSpawn(imitate(state), world, pos, placement);
    }

    @Override
    public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {
        imitationHandler.beginLeavesDecay(imitate(state), world, pos);
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return imitationHandler.canConnectRedstone(imitate(state), world, pos, side);
    }

    @Override
    public boolean isFoliage(IBlockAccess world, BlockPos pos) {
        return imitationHandler.isFoliage(world, pos);
    }

    @Override
    public boolean addLandingEffects(IBlockState state1, WorldServer server, BlockPos pos, IBlockState state2, EntityLivingBase entity, int particles) {
        return imitationHandler.addLandingEffects(imitate(state1), server, pos, imitate(state2), entity, particles);
    }

    @Override
    public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
        return imitationHandler.addRunningEffects(imitate(state), world, pos, entity);
    }

    @Override
    public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
        return imitationHandler.addHitEffects(imitate(state), world, target, manager);
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return imitationHandler.addDestroyEffects(world, pos, manager);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing, IPlantable plant) {
        return imitationHandler.canSustainPlant(imitate(state), world, pos, facing, plant);
    }

    @Override
    public boolean isStickyBlock(IBlockState state) {
        return getBlock() == Blocks.SLIME_BLOCK;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return imitationHandler.getFlammability(world, pos, side);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return imitationHandler.isFlammable(world, pos, side);
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return imitationHandler.getFireSpreadSpeed(world, pos, side);
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return imitationHandler.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        if (bgBlock.getBlock().equals(Blocks.OBSIDIAN) && Cfg.BlocksCat.bgImitation) {
            return EnumPushReaction.BLOCK; // There's a special exemption in BlockPistonBase.
        }
        return imitationHandler.getPushReaction(imitate(state));
    }

    private IBlockState imitate(IBlockState state) {
        return imitationHandler == bgBlock.getBlock() ? bgBlock : state;
    }

    @Override
    public String getTranslationKey() {
        final String key = super.getTranslationKey();
        return key.endsWith(".")
            ? properties.ore.get().getBlock().getTranslationKey()
            : key;
    }

    /* --- Don't imitate these --- */

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        // World#getBlockState seems to fix a bug with using state directly.
        return toStack(world.getBlockState(pos));
    }

    public Item asItem(IBlockState state) {
        return normalItem.get();
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(asItem(state));
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return false;
    }

    /* --- Rendering --- */

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == LAYER;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing facing) {
        return bgBlock.getBlockFaceShape(worldIn, pos, facing);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return bgBlock == null || bgBlock.isOpaqueCube();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        final IBlockState ore = properties.ore.get();
        ore.getBlock().randomDisplayTick(ore, world, pos, rand);
    }

    /* --- Handle block drops --- */

    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!dropsPrepped) {
            // Drops are synchronized so that xp can be unique to each drop.
            checkPrepareDrops(world);
        }
        if (properties.drops.isPresent()) {
            final Random rand = world instanceof World ? ((World) world).rand : new Random();
            for (DropProperties drop : currentDrops) {
                final ItemStack stack = drop.drop.get().copy();
                final int multiple = getDropMultiple(rand, state, stack, fortune);

                stack.setCount(drop.count.rand(rand) * multiple);
                drops.add(stack);
            }
        } else {
            final IBlockState ore = properties.ore.get();
            ore.getBlock().getDrops(drops, world, pos, ore, fortune);
        }
        replaceSelfDrops(drops, state);
        dropsPrepped = false;
    }

    private int getDropMultiple(Random rand, IBlockState state, ItemStack stack, int fortune) {
        final boolean isSelfDrop = stack.isItemEqual(getStack(state));
        final boolean isOreDrop = stack.isItemEqual(toStack(properties.ore.get()));
        final boolean isDense = state.getValue(DENSE);
        int i = 1;
        if (!(isSelfDrop || isOreDrop) || isDense) { // drop != this && drop != equivalent
            i = getMax(1, rand.nextInt(fortune + 2) - 1);
        }
        if (isDense) { // This is *not* silk touch, so we will always multiply when dense.
            i *= getDenseMultiple(rand);
        }
        return i;
    }

    private void replaceSelfDrops(NonNullList<ItemStack> drops, IBlockState state) {
        final List<ItemStack> clone = new ArrayList<>();
        for (ItemStack stack : drops) {
            final boolean isOreDrop = stack.isItemEqual(toStack(properties.ore.get()));
            if (Cfg.BlocksCat.variantsDrop && isOreDrop) {
                final ItemStack notDense = toStack(state.withProperty(DENSE, false));
                notDense.setCount(stack.getCount());
                clone.add(notDense);
            } else {
                clone.add(stack);
            }
        }
        drops.clear();
        drops.addAll(clone);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess reader, BlockPos pos, int fortune) {
        checkPrepareDrops(reader);
        if (properties.drops.isPresent()) {
            int xp = 0;
            for (DropProperties drop : currentDrops) {
                final Random rand = reader instanceof World ? ((World) reader).rand : new Random();
                xp += drop.xp.rand(rand);
            }
            return xp;
        } else {
            final IBlockState ore = properties.ore.get();
            return ore.getBlock().getExpDrop(ore, reader, pos, fortune);
        }
    }

    /** Checks the world reader if possible to make sure we're in the right thread. */
    private void checkPrepareDrops(IBlockAccess reader) {
        if (reader instanceof World) {
            final World world = (World) reader;
            if (!world.isRemote) {
                prepareDrops(world.rand);
            }
        } else {
           prepareDrops(new Random());
        }
    }

    /** Updates the current drops for this block so that xp and item drops can be synchronized. */
    private synchronized void prepareDrops(Random rand) {
        currentDrops.clear();
        properties.drops.ifPresent(drops ->
            drops.forEach(drop -> {
                if (drop.chance == 1.0 || rand.nextFloat() <= drop.chance) {
                    currentDrops.add(drop);
                }
            })
        );
        dropsPrepped = true;
    }

    private static int getDenseMultiple(Random rand) {
        int i = Cfg.DenseCat.dropMultiplier;
        if (Cfg.DenseCat.randomDropCount) {
            i = numBetween(rand, 1, i);
        }
        // This placement avoids range issues.
        if (i < Cfg.DenseCat.dropMultiplierMin) {
            i = Cfg.DenseCat.dropMultiplierMin;
        }
        return i;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        if (Cfg.BlocksCat.variantsSilktouch) {
            return toStack(state);
        } else if (state.getValue(DENSE)) { // No silk touch, but dense
            final ItemStack stack = Cfg.BlocksCat.variantsDrop
                ? toStack(state.withProperty(DENSE, false))
                : toStack(properties.ore.get());
            stack.setCount(getDropMultiple(new Random(), state, stack, 0));
            return stack;
        }
        return toStack(properties.ore.get());
    }

    /* --- Block interface events --- */

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (hasGravity) {
            world.scheduleUpdate(pos, this, 2);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (hasGravity) {
            world.scheduleUpdate(pos, this, 2);
        }
    }

    /* --- Block updates --- */

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        handleGravity(state, world, pos);
    }

    /* --- Gravity features --- */

    /** Determines whether this block should attempt to fall. If so, does. */
    private void handleGravity(IBlockState state, World world, BlockPos pos) {
        if (!world.isRemote && hasGravity) {
            checkFallable(state, world, pos);
        }
    }

    /** From FallingBlock.java: returns whether this block can fall at the current position. */
    private void checkFallable(IBlockState state, World world, BlockPos pos) {
        if (pos.getY() > 0 && canFallThrough(world.getBlockState(pos.down()))) {
            world.spawnEntity(new EntityFallingBlock(world, (double) pos.getX() + 0.5, (double) pos.getY(), (double) pos.getZ() + 0.5, state));
        }
    }

    /** From FallingBlock.java: returns whether this block is suitable to fall through. */
    private static boolean canFallThrough(IBlockState state) {
        final Material mat = state.getMaterial();
        return mat == Material.AIR || mat.isLiquid() || mat.isReplaceable();
    }

    public void registerModels() {
        for (IBlockState state : getBlockState().getValidStates()) {
            final ResourceLocation name = getRegistryName();
            final StringBuilder fullName = new StringBuilder(name.getPath());
            final boolean dense = state.getValue(DENSE);
            final int meta = getMetaFromState(state);
            final Item item = asItem(state);

            if (dense) {
                fullName.insert(0, "dense_");
            }
            final ModelResourceLocation inv = mrl(osvLocation(fullName.toString()), "inventory");
            Main.proxy.registerVariantRenderer(item, meta, inv);
        }
    }
}