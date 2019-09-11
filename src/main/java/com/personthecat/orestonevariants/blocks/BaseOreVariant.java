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
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BaseOreVariant extends Block {
    /** Contains the standard block properties and any additional values, if necessary. */
    private final OreProperties properties;
    /** A reference to the background block represented by this variant. */
    private final BlockState bgBlock;
    /** Reports whether this block should fall like sand. */
    private final Lazy<Boolean> hasGravity = new Lazy<>(this::testGravity);
    /** Reports whether this block should tick randomly. */
    private final Lazy<Boolean> variantTicksRandomly = new Lazy<>(this::testTickRandomly);

    /** BlockState properties used by all ore variants. */
    public static final BooleanProperty DENSE = BooleanProperty.create("dense");
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    /** Primary constructor. */
    public BaseOreVariant(OreProperties properties, BlockState bgBlock) {
        super(createProperties(properties.getBlock(), bgBlock));
        this.properties = properties;
        this.bgBlock = bgBlock;
        setDefaultState(createDefaultState());
        setRegistryName(createName());
    }

    /** Decides whether to merge block properties for this ore. */
    private static Block.Properties createProperties(Block.Properties ore, BlockState bgBlock) {
        return Cfg.bgImitation.get() ? BlockPropertiesHelper.merge(ore, bgBlock) : ore;
    }

    /** Conditionally generates the default state for this ore. */
    private BlockState createDefaultState() {
        final BlockState unlit = getDefaultState()
            .with(LIT, false);
        return Cfg.denseOres.get()
            ? unlit.with(DENSE, false)
            : unlit;
    }

    /** Generates the full registry name for this ore variant. */
    private ResourceLocation createName() {
        return osvLocation(f("{}{}", properties.getName(), createAffix()));
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

    /** Determines whether this block should fall like sand. */
    private boolean testGravity() {
        return Cfg.bgImitation.get() && bgBlock.getBlock() instanceof FallingBlock;
    }

    private boolean testTickRandomly() {
        return ticksRandomly || bgBlock.ticksRandomly() || hasGravity.get();
    }

    /** Returns a stack containing this block. */
    private ItemStack getStack() {
        return new ItemStack(this);
    }

    /** Returns a stack containing the background ore block represented by this block. */
    private ItemStack getBackgroundStack() {
        return new ItemStack(properties.getOre().getBlock());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DENSE, LIT);
    }

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
        return properties.getDrops()
            .map(loot -> loot.generate(ctx))
            .orElse(super.getDrops(state, builder));
    }

    /** Generates additional loot, if applicable */
    private List<ItemStack> handleDense(List<ItemStack> items, BlockState state, LootContext.Builder builder) {
        if (state.get(DENSE) && !bgBlockDropped(items)) {
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
    private boolean bgBlockDropped(List<ItemStack> items) {
        final ItemStack bgStack = getBackgroundStack();
        return find(items, item -> item.isItemEqual(bgStack)).isPresent();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moving) {
        scheduleTickConditionally(world, state, pos);
    }

    public BlockState updatePostPlacement(BlockState state, Direction dir, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
        scheduleTickConditionally(world, state, pos);
        return state;
    }

    private void scheduleTickConditionally(IWorld world, BlockState state, BlockPos pos) {
        if (ticksRandomly(state)) {
            world.getPendingBlockTicks().scheduleTick(pos, this, tickRate(world));
        }
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return variantTicksRandomly.get() || state.get(LIT);
    }

    @Override
    public int tickRate(IWorldReader world) {
        return 10;
    }

    @Override
    public void tick(BlockState state, World world, BlockPos pos, Random rand) {
        if (state.get(LIT)) {
            world.setBlockState(pos, state.with(LIT, false), 3);
        } else if (!world.isRemote && hasGravity.get()) {
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (state.get(LIT)) {
            spawnRedstoneParticles(world, pos);
        }
        if (hasGravity.get()) {
            bgBlock.getBlock().animateTick(state, world, pos, rand);
        }
    }

    /** Imitates the redstone ore particle effect. */
    private static void spawnRedstoneParticles(World world, BlockPos pos) {
        for (Direction d : Direction.values()) {
            final BlockPos offset = pos.offset(d);
            if (world.getBlockState(offset).isOpaqueCube(world, offset)) {
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