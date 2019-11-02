package personthecat.mod.objects.blocks;

import static personthecat.mod.Main.logger;
import static personthecat.mod.Main.proxy;
import static personthecat.mod.util.CommonMethods.*;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.CreativeTab;
import personthecat.mod.Main;
import personthecat.mod.config.Cfg;
import personthecat.mod.init.BlockInit;
import personthecat.mod.init.ItemInit;
import personthecat.mod.objects.blocks.item.ItemBlockVariants;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.interfaces.IHasModel;

/**
 * Maps different variants of the same ore type, interprets 
 * information that might normally go in child classes.
 *
 * All information is stored here to provide the easiest retrieval
 * of needed information from external classes, such as: whether 
 * the block is dynamic, whether it is dense and/or lit, etc.
 */
public class BlockOresBase extends Block implements IHasModel
{
    protected final String name;

    protected final BlockGroup bgBlocks;

    protected final BlockStateContainer actualBlockState;

    protected boolean changeRenderLayer, isBlockRegistered;

    protected static boolean bgImitation = Cfg.BlocksCat.MiscCat.bgBlockImitation;

    protected OreProperties props;
    protected DropProperties[] currentDrops;

    protected final int numStates;

    protected Item item;

    protected PropertyInteger variants;

    protected BlockOresBase normalVariant, denseVariant,
        normalRedstoneVariant, litRedstoneVariant;

    protected final IBlockState[] bgBlockStates;
    protected final ModelResourceLocation[] bgModelLocations;

    /**
     * Use createVariant() to automatically handle dense and lit/unlit variants.
     */
    private BlockOresBase(OreProperties props, BlockGroup blocks, boolean isDense)
    {
        super(Material.ROCK);

        assert props.inUse();

        this.name = getFullName(props, blocks, isDense);
        this.props = props;
        this.bgBlocks = blocks;
        this.numStates = blocks.size();
        this.bgBlockStates = blocks.getBlocks();
        this.bgModelLocations = new ModelResourceLocation[numStates];

        proxy.setBackgroundModels(this, blocks);
        setVariantsConditionally(numStates);
        setRegistryName(name);
        setTranslationKey(name);
        setResistance(15.0f);

        this.actualBlockState = createActualBlockState();
        setDefaultState(actualBlockState.getBaseState());
    }

    public BlockOresBase(OreProperties props, BlockGroup blocks)
    {
        this(props, blocks, false);
    }

    private String getFullName(OreProperties props, BlockGroup blocks, boolean isDense)
    {
        String prefix = isDense ? "dense_" : "";

        if (blocks.getName().equals("minecraft")) return prefix + props.getName();

        return prefix + props.getName() + "_" + blocks.getName();
    }

    public void setBackgroundModels(BlockGroup blocks)
    {
        for (int i = 0; i < blocks.size(); i++)
        {
            IBlockState backgroundBS = blocks.getBlocks()[i];

            ModelResourceLocation backgroundMRL = getModelResourceLocation(backgroundBS);

            setBackgroundModelLocation(backgroundMRL, i);
        }
    }

    private void setVariantsConditionally(int numStates)
    {
        if (numStates > 1) this.variants = PropertyInteger.create("variant", 0, numStates - 1);
    }

    /**
     * Registers the two blocks as counterparts of one another.
     */
    private static void assignDenseAndNormalVariants(BlockOresBase denseVariant, BlockOresBase normalVariant)
    {
        denseVariant.setNormalVariant(normalVariant);
        normalVariant.setDenseVariant(denseVariant);
    }

    private static void assignNormalAndLitRedstone(BlockOresBase normalRedstone, BlockOresBase litRedstone)
    {
        normalRedstone.setLitRedstoneVariant(litRedstone);
        litRedstone.setNormalRedstoneVariant(normalRedstone);
    }

    /**
     * Make sure to call this after tagging the object as lit, dense, etc.
     */
    public void finalizePropertiesAndRegisterAllVariants()
    {
        discoverAndMapDenseLitRedstone();

        finalizeProperties();
        register();

        if (litRedstoneVariant != null)
        {
            BlockOresBase litRedstone = litRedstoneVariant;

            litRedstone.finalizeProperties();
            litRedstone.register();

            if (litRedstone.denseVariant != null)
            {
                BlockOresBase denseLitRedstone = litRedstone.denseVariant;

                denseLitRedstone.finalizeProperties();
                denseLitRedstone.register();
            }
        }

        if (denseVariant != null)
        {
            BlockOresBase denseOre = denseVariant;

            denseOre.finalizeProperties();
            denseOre.register();

            //In case blocks are mapped in reverse order.
            if (denseOre.litRedstoneVariant != null)
            {
                BlockOresBase denseLitRedstone = denseOre.litRedstoneVariant;

                denseLitRedstone.finalizeProperties();
                denseLitRedstone.register();
            }
        }
    }

    //A little redundant, but it looks nicer this way.
    private void discoverAndMapDenseLitRedstone()
    {
        if (litRedstoneVariant != null)
        {
            BlockOresBase litRedstone = litRedstoneVariant;

            if (litRedstone.denseVariant != null)
            {
                BlockOresBase denseLitRedstone = litRedstone.denseVariant;

                assert denseVariant != null; //Just indicating that we definitely want a crash here.
                BlockOresBase denseRedstone = denseVariant;

                assignNormalAndLitRedstone(denseRedstone, denseLitRedstone);
            }
        }
    }

    private void finalizeProperties()
    {
        if (isLitRedstone())
        {
            setTickRandomly(true);
        }
        else
        {
            if (isDenseVariant()) setCreativeTab(CreativeTab.DENSE_VARIANTS);

            else setCreativeTab(CreativeTab.ORE_VARIANTS);
        }

        setHardness(props.getHardness());
        setLightLevel(props.getLightLevel());
        setHarvestLevel("pickaxe", props.getLevel());
        setCurrentDrops(props.getDropProperties()[0]);
    }

    private void register()
    {
        if (!isBlockRegistered)
        {
            item = new ItemBlockVariants(this);

            BlockInit.BLOCKS.add(this);
            ItemInit.ITEMS.add(item);

            for (IBlockState state : getBlockState().getValidStates())
            {
                BlockInit.BLOCKSTATES.add(state);
            }

            isBlockRegistered = true;
        }
    }

    public String getOriginalName()
    {
        return name;
    }

    public OreProperties getProperties()
    {
        return props;
    }

    //Couldn't create an ItemStack from a BlockOresBase, for some reason.
    public Item getItem()
    {
        return item;
    }

    public static enum VariantType
    {
        NORMAL(),
        DENSE(),
        NORMAL_REDSTONE(),
        LIT_REDSTONE();
    }

    public BlockOresBase createVariant(VariantType type)
    {
        if (!isBlockRegistered)
        {
            switch(type)
            {
                case DENSE:

                    BlockOresBase denseVariant = new BlockOresBase(props, bgBlocks, true);

                    assignDenseAndNormalVariants(denseVariant, this);

                    return denseVariant;

                case LIT_REDSTONE:

                    OreProperties lit = OreProperties.propertiesOf("lit_redstone_ore");

                    lit.loadOre();
                    lit.loadItems();

                    BlockOresBase litVariant = new BlockOresBase(lit, bgBlocks, isDenseVariant());

                    assignNormalAndLitRedstone(this, litVariant);

                    return litVariant;

                case NORMAL_REDSTONE:

                case NORMAL:

                default:

                    logger.warn("Error: Invalid case: " + type + ". Block is already a normal variant. Returning null.");

                    return null;
            }
        }

        logger.warn("Error: Block is already registered. Cannot create variant. Returning null.");

        return null;
    }

    /**
     * Returns true if a dense counterpart has been assigned
     * OR if dense variants cannot exist.
     */
    public boolean isNormalVariant()
    {
        return ((denseVariant != null) ||
                (!Cfg.DenseCat.GeneralDenseCat.denseVariants));
    }

    public boolean isDenseVariant()
    {
        return normalVariant != null;
    }

    /**
     * Specifically, returns whether the block was created
     * using any derivative BlockOresEnumerated.
     */
    public boolean hasEnumBlockStates()
    {
        return numStates > 1;
    }

    public boolean isNormalRedstone()
    {
        return litRedstoneVariant != null;
    }

    public boolean isLitRedstone()
    {
        return normalRedstoneVariant != null;
    }

    public BlockGroup getBlockGroup()
    {
        return bgBlocks;
    }

    public void setBackgroundBlockState(IBlockState backgroundBlockState)
    {
        setBackgroundBlockState(backgroundBlockState, 0);
    }

    public void setBackgroundBlockState(IBlockState backgroundBlockState, int meta)
    {
        bgBlockStates[meta] = backgroundBlockState;
    }

    public IBlockState getBackgroundBlockState()
    {
        return getBackgroundBlockState(0);
    }

    public IBlockState getBackgroundBlockState(IBlockState state)
    {
        return getBackgroundBlockState(getMetaFromState(state));
    }

    public IBlockState getBackgroundBlockState(int meta)
    {
        if (bgBlockStates[meta] != null) return bgBlockStates[meta];

        logger.warn("Error: Background blockstate may not have been mapped for this variant. Returning null.");
        logger.warn("Block name: " + name);

        return null;
    }

    public ItemStack getBackgroundStack(int meta)
    {
        IBlockState bgBlockState = getBackgroundBlockState(meta);
        Block bgBlock = bgBlockState.getBlock();

        return new ItemStack(bgBlock, 1, bgBlock.getMetaFromState(bgBlockState));
    }

    public void setBackgroundModelLocation(ModelResourceLocation location)
    {
        setBackgroundModelLocation(location, 0);
    }

    public void setBackgroundModelLocation(ModelResourceLocation location, int meta)
    {
        bgModelLocations[meta] = location;
    }

    public ModelResourceLocation getBackgroundModelLocation(int meta)
    {
        if (bgModelLocations[meta] != null) return bgModelLocations[meta];

        logger.warn("Error: Background model location may not have been mapped for this variant. Returning null.");
        logger.warn("Block name: " + name);

        return null;
    }

    public void setLitRedstoneVariant(BlockOresBase block)
    {
        this.litRedstoneVariant = block;
    }

    public BlockOresBase getLitRedstoneVariant()
    {
        return (BlockOresBase) getLitRedstoneVariant(0).getBlock();
    }

    public IBlockState getLitRedstoneVariant(int meta)
    {
        if (isNormalRedstone()) return litRedstoneVariant.getStateFromMeta(meta);

        logger.warn("Error: tried to retrieve a lit variant from an invalid candidate. Returning null.");
        logger.warn("Block name: " + name);

        return null;
    }

    public void setNormalRedstoneVariant(BlockOresBase block)
    {
        this.normalRedstoneVariant = block;
    }

    public BlockOresBase getNormalRedstoneVariant()
    {
        return (BlockOresBase) getNormalRedstoneVariant(0).getBlock();
    }

    public IBlockState getNormalRedstoneVariant(int meta)
    {
        if (isLitRedstone()) return normalRedstoneVariant.getStateFromMeta(meta);

        logger.warn("Error: tried to retrieve normal redstone from an invalid candidate. Returning null.");
        logger.warn("Block name: " + name);

        return null;
    }


    public BlockOresBase ensureNotLit()
    {
        if (isLitRedstone()) return normalRedstoneVariant;

        return this;
    }

    public IBlockState ensureNotLit(int meta)
    {
        if (isLitRedstone()) return getNormalRedstoneVariant(meta);

        return this.getStateFromMeta(meta);
    }

    public void setDenseVariant(BlockOresBase block)
    {
        this.denseVariant = block;
    }

    public BlockOresBase getDenseVariant()
    {
        return (BlockOresBase) getDenseVariant(0).getBlock();
    }

    public IBlockState getDenseVariant(int meta)
    {
        if (isNormalVariant()) return denseVariant.getStateFromMeta(meta);

        logger.warn("Error: tried to retrieve a normal variant from an invalid candidate. Returning null.");
        logger.warn("Block name: " + name);

        return null;
    }

    public void setNormalVariant(BlockOresBase block)
    {
        this.normalVariant = block;
    }

    public BlockOresBase getNormalVariant()
    {
        return (BlockOresBase) getNormalVariant(0).getBlock();
    }

    public IBlockState getNormalVariant(int meta)
    {
        if (isDenseVariant()) return normalVariant.getStateFromMeta(meta);

        logger.warn("Error: tried to retrieve a dense variant from an invalid candidate. Returning null.");
        logger.warn("Block name: " + name);

        return null;
    }

    protected void setCurrentDrops(DropProperties... drops)
    {
        this.currentDrops = drops;
    }

    /**
     * Background block imitation below this point.
     */

    /**
     * Returns the original ore's hardness, raises or lowers it relative to stone's hardness.
     */
    @Override
    public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos)
    {
        if (bgImitation)
        {
            float bgHardness = getBackgroundBlockState(state).getBlockHardness(worldIn, pos) - 1.5F;

            float finalHardness = props.getHardness(worldIn, pos) + bgHardness;

            return finalHardness < 0 ? 0 : finalHardness;
        }

        return blockHardness;
    }

    /**
     * Returns the ore's light level or bgBlockState's light level, whichever is higher.
     */
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if (bgImitation)
        {
            int bgLightValue = getBackgroundBlockState(state).getLightValue();

            return lightValue > bgLightValue ? lightValue : bgLightValue;
        }

        return state.getLightValue();
    }

    @Override
    @Nullable
    public String getHarvestTool(IBlockState state)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(state);

            String tool = bgBlockState.getBlock().getHarvestTool(bgBlockState);

            return tool == null ? super.getHarvestTool(state) : tool;
        }

        return super.getHarvestTool(state);
    }

    @Override
    public boolean isToolEffective(String type, IBlockState state)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(state);

            return bgBlockState.getBlock().isToolEffective(type, bgBlockState);
        }

        return type != null && type.equals("pickaxe");
    }

    @Override
    public int getLightOpacity(IBlockState state)
    {
        if (bgImitation) return getBackgroundBlockState(state).getLightOpacity();

        return lightOpacity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isTranslucent(IBlockState state)
    {
        if (bgImitation) return getBackgroundBlockState(state).isTranslucent();

        return translucent;
    }

    @Override
    public Material getMaterial(IBlockState state)
    {
        if (bgImitation) return getBackgroundBlockState(state).getMaterial();

        return material;
    }

    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        if (bgImitation) getBackgroundBlockState(state).getPushReaction();

        return EnumPushReaction.NORMAL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress(IBlockState state)
    {
        if (bgImitation) return getBackgroundBlockState(state).hasCustomBreakingProgress();

        return false;
    }

    @Override
    public float getSlipperiness(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity entity)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(state);

            return bgBlockState.getBlock().getSlipperiness(bgBlockState, world, pos, entity);
        }

        return slipperiness;
    }

    @Override
    public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(state);

            return bgBlockState.getBlock().canSustainLeaves(bgBlockState, world, pos);
        }

        return false;
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(state);

            return bgBlockState.getBlock().canSustainPlant(bgBlockState, world, pos, direction, plantable);
        }

        return false;
    }

    @Override
    public boolean isWood(IBlockAccess world, BlockPos pos)
    {
        if (bgImitation) return getBackgroundBlockState(world.getBlockState(pos)).getMaterial().equals(Material.WOOD);

        return false;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(world.getBlockState(pos));

            return bgBlockState.getBlock().getFlammability(world, pos, face);
        }

        return 0;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(world.getBlockState(pos));

            return bgBlockState.getBlock().getFireSpreadSpeed(world, pos, face);
        }

        return 0;
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side)
    {
        if (bgImitation) return getBackgroundBlockState(world.getBlockState(pos)).getBlock().isFireSource(world, pos, side);

        return false;
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity)
    {
        if (bgImitation)
        {
            IBlockState bgBlockState = getBackgroundBlockState(state);

            return bgBlockState.getBlock().getSoundType(bgBlockState, world, pos, entity);
        }

        return getSoundType();
    }

    /*
     * The original does not have a parameter for its blockstate. Probably gets it from world.
     * This may not even work.
     */
    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        if (bgImitation) return getBackgroundBlockState(world.getBlockState(pos)).getBlock().canBeConnectedTo(world, pos, facing);

        return false;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (bgImitation)
        {
            if (getBackgroundBlockState(state).getBlock() instanceof BlockFalling)
            {
                worldIn.scheduleUpdate(pos, this, 2);
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (bgImitation)
        {
            if (getBackgroundBlockState(state).getBlock() instanceof BlockFalling)
            {
                worldIn.scheduleUpdate(pos, this, 2);
            }
        }
    }

    private void checkFallable(World worldIn, BlockPos pos)
    {
        if (bgImitation)
        {
            if (getBackgroundBlockState(worldIn.getBlockState(pos)).getBlock() instanceof BlockFalling)
            {
                if (!worldIn.isRemote &&
                    pos.getY() >= 0 &&
                    canFallThrough(worldIn.getBlockState(pos.down())) &&
                    worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32)))
                {
                    worldIn.spawnEntity(new EntityFallingBlock(
                        worldIn,
                        (double) pos.getX() + 0.5D,
                        (double) pos.getY(),
                        (double) pos.getZ() + 0.5D,
                        worldIn.getBlockState(pos)));
                }
            }
        }
    }

    private boolean canFallThrough(IBlockState state)
    {
        Material material = state.getMaterial();

        return state.getBlock().equals(Blocks.FIRE) ||
            material.equals(Material.AIR) ||
            material.equals(Material.WATER) ||
            material.equals(Material.LAVA);
    }

    /**
     * Except these. I don't want my ores getting replaced, regardless of the material.
     */
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
    {
        IBlockState bgBlockState = getBackgroundBlockState(state);

        if (bgImitation) return bgBlockState.getBlock().canEntityDestroy(bgBlockState, world, pos, entity);

        //Don't allow replacing these anyway.
        return !bgBlockState.getBlock().equals(Blocks.BARRIER) &&
            !bgBlockState.getBlock().equals(Blocks.BEDROCK) &&
            !bgBlockState.getBlock().equals(Blocks.END_PORTAL) &&
            !bgBlockState.getBlock().equals(Blocks.END_PORTAL_FRAME) &&
            !bgBlockState.getBlock().equals(Blocks.END_GATEWAY) &&
            !bgBlockState.getBlock().equals(Blocks.COMMAND_BLOCK) &&
            !bgBlockState.getBlock().equals(Blocks.CHAIN_COMMAND_BLOCK) &&
            !bgBlockState.getBlock().equals(Blocks.REPEATING_COMMAND_BLOCK);
    }

    /**
     * Ore, drop, and other miscellaneous properties below this point.
     */

    @Override
    public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items)
    {
        for (int i = 0; i < numStates; i++)
        {
            items.add(new ItemStack(this, 1, i));
        }
    }

    private BlockStateContainer createActualBlockState()
    {
        if (numStates < 2) return createBlockState();

        return new BlockStateContainer(this, new IProperty[] {variants});
    }

    @Override
    public BlockStateContainer getBlockState()
    {
        return this.actualBlockState;
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        if (numStates < 2) return 0;

        return (Integer) state.getValue(variants);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        if (numStates < 2) return this.getDefaultState();

        return this.getDefaultState().withProperty(variants, meta);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        if (world instanceof World)
        {
            World worldIn = (World) world;

            if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) //From the docs: this prevents item dupe.
            {
                int meta = getMetaFromState(state);

                ItemStack selfStack = isDenseVariant() ? normalVariant.getSelfStack(meta) : getSelfStack(meta);

                drops.addAll(props.getDrops(worldIn, pos, state, fortune, currentDrops, isDenseVariant(), selfStack));
            }
        }
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state)
    {
        ItemStack stack = getSelfStack(getMetaFromState(state));

        if (!Cfg.BlocksCat.OreDropCat.variantsDropWithSilkTouch)
        {
            stack = props.getDropProperties()[0].getDropSilkTouchStack();
        }

        return stack;
    }

    private ItemStack getSelfStack(int meta)
    {
        return new ItemStack(ensureNotLit().getItem(), 1, meta);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune)
    {
        return props.getExpDrop(state, world, pos, fortune, currentDrops);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        return getSelfStack(getMetaFromState(world.getBlockState(pos)));
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        return (layer == BlockRenderLayer.TRANSLUCENT | layer == BlockRenderLayer.CUTOUT_MIPPED);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        if (changeRenderLayer || Cfg.BlocksCat.MiscCat.noTranslucent) return BlockRenderLayer.CUTOUT_MIPPED;

        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        this.changeRenderLayer = true;

        activate(worldIn, pos);

        setCurrentDrops(props.getDropPropertiesByChance(worldIn, playerIn));

        this.changeRenderLayer = false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        activate(worldIn, pos);

        return false; //super#onBlockActivated returns false.
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        activate(worldIn, pos);
    }

    protected void activate(World worldIn, BlockPos pos)
    {
        if (isNormalRedstone())
        {
            IBlockState state = worldIn.getBlockState(pos);
            worldIn.setBlockState(pos, getLitRedstoneVariant(getMetaFromState(state)));

            spawnParticles(worldIn, pos);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (isLitRedstone()) worldIn.setBlockState(pos, getNormalRedstoneVariant(getMetaFromState(state)));

        checkFallable(worldIn, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (isLitRedstone()) this.spawnParticles(worldIn, pos);
    }

    protected void spawnParticles(World worldIn, BlockPos pos)
    {
        Random random = worldIn.rand;

        double d1 = (double)((float)pos.getX() + random.nextFloat());
        double d2 = (double)((float)pos.getY() + random.nextFloat());
        double d3 = (double)((float)pos.getZ() + random.nextFloat());

        for (int i = 0; i < 6; i++)
        {
            if (i == 0 && !worldIn.getBlockState(pos.up()).isOpaqueCube())
            {
                d2 = (double)pos.getY() + 0.0625D + 1.0D;
            }

            if (i == 1 && !worldIn.getBlockState(pos.down()).isOpaqueCube())
            {
                d2 = (double)pos.getY() - 0.0625D;
            }

            if (i == 2 && !worldIn.getBlockState(pos.south()).isOpaqueCube())
            {
                d3 = (double)pos.getZ() + 0.0625D + 1.0D;
            }

            if (i == 3 && !worldIn.getBlockState(pos.north()).isOpaqueCube())
            {
                d3 = (double)pos.getZ() - 0.0625D;
            }

            if (i == 4 && !worldIn.getBlockState(pos.east()).isOpaqueCube())
            {
                d1 = (double)pos.getX() + 0.0625D + 1.0D;
            }

            if (i == 5 && !worldIn.getBlockState(pos.west()).isOpaqueCube())
            {
                d1 = (double)pos.getX() - 0.0625D;
            }

            if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1))
            {
                worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[0]);
    }

    public String getModelName(int meta)
    {
        String dense = isDenseVariant() ? "dense_" : "";

        return dense + props.getName() + "_" + formatStateName(getBackgroundBlockState(meta));
    }

    @Override
    public void registerModels()
    {
        for (int i = 0; i < numStates; i++)
        {
            logger.info(i + ": " + getModelName(i));

            proxy.registerVariantRenderer(getItem(), i, getModelName(i));
        }
    }
}