package personthecat.mod.objects.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.CreativeTab;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.init.BlockInit;
import personthecat.mod.init.ItemInit;
import personthecat.mod.objects.blocks.item.ItemBlockVariants;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.IChooseConstructors;
import personthecat.mod.util.IHasModel;

/**
 * Maps different variants of the same ore type, interprets 
 * information that might normally go in child classes.
 * 
 * All information is stored here to provide the easiest retrieval
 * of needed information from external classes, such as: whether 
 * the block is dynamic, whether it is dense and/or lit, etc.
 */
public class BlockOresBase extends Block implements IHasModel, IChooseConstructors
{
	protected String name;
	
	protected boolean changeRenderLayer, isBlockRegistered;
	
	protected OreProperties props;
	protected DropProperties[] currentDrops;
	
	protected Item item;
	
	protected BlockOresBase normalVariant, denseVariant,
			normalRedstoneVariant, litRedstoneVariant;
	
	//Must be initialized by the child class.
	protected IBlockState[] bgBlockStates;
	protected ModelResourceLocation[] bgModelLocations;
	
	/**
	 * Not all information is registered correctly until variants are assigned
	 * and "finalizeProperties...()" has been called. Alternatively,
	 * 
	 * Use createVariant() to automatically determine the correct child 
	 * version of this class to use in the creation of a counterpart.
	 */
	public BlockOresBase(String name)
	{
		super(Material.ROCK);
		
		this.name = name;
		setRegistryName(name);
		setUnlocalizedName(name);
		
		setDefaultState(this.blockState.getBaseState());
		
		props = OreProperties.propertiesOf(name);
		setHardness(props.getHardness());
		setLightLevel(props.getLightLevel());
		setHarvestLevel("pickaxe", props.getLevel());
		setCurrentDrops(props.getDropProperties()[0]);
		
		setResistance(15.0f);
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
		if (isLitRedstone()) setTickRandomly(true);
		
		else
		{
			if (isDenseVariant()) setCreativeTab(CreativeTab.DENSE_VARIANTS);
			
			else setCreativeTab(CreativeTab.ORE_VARIANTS);
		}
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
					
					BlockOresBase denseVariant = chooseConstructor("dense_" + props.getName());
					
					assignDenseAndNormalVariants(denseVariant, this);
					
					return denseVariant;
				
				case LIT_REDSTONE:
					
					BlockOresBase litVariant = chooseConstructor(props.getName().replaceAll("redstone_ore", "lit_redstone_ore"));
					
					assignNormalAndLitRedstone(this, litVariant);
					
					return litVariant;
				
				case NORMAL_REDSTONE:
				
				case NORMAL:
				
				default:
				
					System.err.println("Error: Invalid case: " + type + ". Block is already a normal variant. Returning null.");
					
					return null;				
			}
		}
		
		System.err.println("Error: Block is already registered. Cannot create variant. Returning null.");
		
		return null;
	}
	
	/**
	 * Returns true if a dense counterpart has been assigned
	 * OR if dense variants cannot exist.
	 */
	public boolean isNormalVariant()
	{
		return ((denseVariant != null) ||
				(!ConfigFile.denseVariants));
	}
	
	public boolean isDenseVariant()
	{
		return normalVariant != null;
	}
	
	/**
	 * Specifically, returns whether the block was created
	 * using BlockOresDynamic.
	 */
	public boolean isDynamicVariant()
	{
		return (this instanceof BlockOresDynamic);
	}
	
	/**
	 * Specifically, returns whether the block was created
	 * using any derivative BlockOresEnumerated.
	 */
	public boolean hasEnumBlockStates()
	{
		return (this instanceof BlockOresEnumerated);
	}
	
	public boolean isNormalRedstone()
	{
		return litRedstoneVariant != null;
	}
	
	public boolean isLitRedstone()
	{
		return normalRedstoneVariant != null;
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

	public IBlockState getBackgroundBlockState(int meta)
	{
		if (bgBlockStates[meta] != null) return bgBlockStates[meta];

		System.err.println("Error: Background blockstate may not have been mapped for this variant. Returning null.");
		System.err.println("Block name: " + name);
		
		return null;
	}
	
	public ItemStack getBackgroundStack(int meta)
	{
		IBlockState bgBlockState = getBackgroundBlockState(meta);
		Block bgBlock = bgBlockState.getBlock();		
		
		return new ItemStack(bgBlockState.getBlock(), 1, bgBlock.getMetaFromState(bgBlockState));
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
		
		System.err.println("Error: Background model location may not have been mapped for this variant. Returning null.");
		System.err.println("Block name: " + name);
		
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
		
		System.err.println("Error: tried to retrieve a lit variant from an invalid candidate. Returning null.");
		System.err.println("Block name: " + name);
		
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
		
		System.err.println("Error: tried to retrieve normal redstone from an invalid candidate. Returning null.");
		System.err.println("Block name: " + name);
		
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
		
		System.err.println("Error: tried to retrieve a normal variant from an invalid candidate. Returning null.");
		System.err.println("Block name: " + name);
		
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
		
		System.err.println("Error: tried to retrieve a dense variant from an invalid candidate. Returning null.");
		System.err.println("Block name: " + name);
		
		return null;
	}
	
	protected void setCurrentDrops(DropProperties... drops)
	{
		this.currentDrops = drops;
	}
	
	//Ordinary block-related functionality starts here.
    
	@Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
    	if (world instanceof World)
    	{
    		World worldIn = (World) world;
    		
        	if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) //From the docs: this prevents item dupe.
        	{    		
        		for (DropProperties drop : currentDrops)
        		{
        			int quantity = MathHelper.getInt(worldIn.rand, drop.getLeastDrop(), isDenseVariant() ? drop.getMostDrop() * 3 : drop.getMostDrop());
        			
        			if (!drop.isDropBlock()) quantity = fortune > 0 ? quantity * (MathHelper.abs(worldIn.rand.nextInt(fortune + 2) - 1) + 1) : quantity;
        			
        			ItemStack stack = drop.getDropStack();
        			
        			//drop == dropAlt + variantsDrop == true -> drop self
        			if (drop.canDropSelf()) stack = getSelfStack(getMetaFromState(state));
        			
        			//Spawning multiple entities as opposed to a larger ItemStack for a more authentic visual effect.
        			for (int i = 0; i < quantity; i++) drops.add(stack);
        		}
        	}
    	}
    }
	
	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state)
    {
		ItemStack stack = getSelfStack(getMetaFromState(state));
		
		if (!ConfigFile.variantsDropWithSilkTouch)
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
    	Random rand = world instanceof World ? ((World)world).rand : new Random();
    	
		int i = 0;
		
		for (DropProperties drop : currentDrops)
		{    			
			if (!drop.isDropBlock()) i += MathHelper.getInt(rand, drop.getLeastXp(), drop.getMostXp());
		}

		return i;
    }
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{		
		return new ItemStack(ensureNotLit().getItem(), 1, getMetaFromState(world.getBlockState(pos)));
	}
	
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
    	return (layer == BlockRenderLayer.TRANSLUCENT | layer == BlockRenderLayer.CUTOUT_MIPPED);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
    	if (changeRenderLayer || ConfigFile.noTranslucent) return BlockRenderLayer.CUTOUT_MIPPED;
    	
    	return BlockRenderLayer.TRANSLUCENT;
    }
    
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
    	this.changeRenderLayer = true;
    	
    	this.activate(worldIn, pos);
    	
    	this.setCurrentDrops(props.getDropPropertiesByChance(worldIn, playerIn));
    	
    	this.changeRenderLayer = false;
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
    	this.activate(worldIn, pos);

    	return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
    
    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
    	this.activate(worldIn, pos);
    }
    
    protected void activate(World worldIn, BlockPos pos)
    {        
       	if (isNormalRedstone())
       	{
       		IBlockState state = worldIn.getBlockState(pos);
       		worldIn.setBlockState(pos, getLitRedstoneVariant(getMetaFromState(state)));
       		
       		this.spawnParticles(worldIn, pos);
       	}
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {    	
    	if (isLitRedstone()) worldIn.setBlockState(pos, getNormalRedstoneVariant(getMetaFromState(state)));
    }
	
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (isLitRedstone()) this.spawnParticles(worldIn, pos);
    }

    @Override
    public int tickRate(World worldIn)
    {
    	if (isLitRedstone()) return 30;
    	
    	return 0;
    }
    
    //protected void spawnParticles(World worldIn, BlockPos pos)
    {
    	//Method reflect;
        
        //try
        {
        	//reflect = ReflectionHelper.findMethod(BlockRedstoneOre.class, "spawnParticles", "func_180489_a", World.class, BlockPos.class);
        	//reflect.invoke(Blocks.REDSTONE_ORE, worldIn, pos);
        }
        
        //catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {}
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
	
	@Override
	public void registerModels()
	{
		Main.proxy.registerVariantRenderer(getItem(), 0, getOriginalName());
	}
}