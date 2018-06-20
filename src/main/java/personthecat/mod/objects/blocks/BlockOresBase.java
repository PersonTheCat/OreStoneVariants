package personthecat.mod.objects.blocks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.NameReader;

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
	protected String name;
	
	protected boolean changeRenderLayer, isBlockRegistered;
	
	protected OreProperties props;
	protected DropProperties[] currentDrops;
	
	protected Map<Integer, IBlockState> normalVariantMap = new HashMap<>();
	protected Map<Integer, IBlockState> denseVariantMap = new HashMap<>();
	protected Map<Integer, IBlockState> normalRedstoneVariantMap = new HashMap<>();
	protected Map<Integer, IBlockState> litRedstoneVariantMap = new HashMap<>();
	protected Map<Integer, IBlockState> bgBlockStateMap = new HashMap<>();
	protected Map<Integer, ModelResourceLocation> bgModelLocationMap = new HashMap<>();
	
	/**
	 * Not all information is registered correctly until an appropriate
	 * "reigisterAs...()" method has been called. Alternatively,
	 * 
	 * Use BlockInit$ClassChooser#registerAndCreate... to automatically determine
	 * the correct child version of this class to use in the creation of a counterpart.
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
	public static void assignDenseAndNormalVariants(BlockOresBase denseVariant, BlockOresBase normalVariant)
	{
		denseVariant.setNormalVariants(normalVariant);
		normalVariant.setDenseVariants(denseVariant);
	}

	public static void assignNormalAndLitRedstone(BlockOresBase normalRedstone, BlockOresBase litRedstone)
	{
		normalRedstone.setLitRedstoneVariants(litRedstone);
		litRedstone.setNormalRedstoneVariants(normalRedstone);
	}
	
	/**
	 * Make sure to call this after tagging the object as lit, dense, etc.
	 */
	public void finalizePropertiesAndRegisterAllVariants()
	{
		discoverAndMapDenseLitRedstone();
		
		finalizeProperties();
		register();
		
		if (litRedstoneVariantMap.get(0) != null)
		{
			BlockOresBase litRedstone = (BlockOresBase) litRedstoneVariantMap.get(0).getBlock();
			
			litRedstone.finalizeProperties();
			litRedstone.register();
			
			if (litRedstone.denseVariantMap.get(0) != null)
			{
				BlockOresBase denseLitRedstone = (BlockOresBase) litRedstone.denseVariantMap.get(0).getBlock();

				denseLitRedstone.finalizeProperties();
				denseLitRedstone.register();
			}
		}
		
		if (denseVariantMap.get(0) != null)
		{
			BlockOresBase denseOre = (BlockOresBase) denseVariantMap.get(0).getBlock();
			
			denseOre.finalizeProperties();
			denseOre.register();
			
			//Doesn't happen.
//			if (denseOre.litRedstoneVariantMap.get(0) != null)
		}
	}
	
	//A little redundant, but it looks nicer this way.
	private void discoverAndMapDenseLitRedstone()
	{
		if (litRedstoneVariantMap.get(0) != null)
		{
			BlockOresBase litRedstone = (BlockOresBase) litRedstoneVariantMap.get(0).getBlock();
			
			if (litRedstone.denseVariantMap.get(0) != null)
			{
				BlockOresBase denseLitRedstone = (BlockOresBase) litRedstone.denseVariantMap.get(0).getBlock();
				
				assert denseVariantMap.get(0) != null; //Just indicating that we definitely want a crash here.
				BlockOresBase denseRedstone = (BlockOresBase) denseVariantMap.get(0).getBlock();

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
			BlockInit.BLOCKS.add(this);
			ItemInit.ITEMS.add(new ItemBlockVariants(this));
			
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
	
	/**
	 * Returns true if a dense counterpart has been assigned
	 * OR if dense variants cannot exist.
	 */
	public boolean isNormalVariant()
	{
		return ((!denseVariantMap.isEmpty()) ||
				(!ConfigFile.denseVariants));
	}
	
	public boolean isDenseVariant()
	{
		return !normalVariantMap.isEmpty();
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
		return !litRedstoneVariantMap.isEmpty();
	}
	
	public boolean isLitRedstone()
	{
		return !normalRedstoneVariantMap.isEmpty();
	}
	
	public void setBackgroundBlockState(IBlockState backgroundBlockState)
	{
		setBackgroundBlockState(backgroundBlockState, 0);
	}
	
	public void setBackgroundBlockState(IBlockState backgroundBlockState, int meta)
	{
		bgBlockStateMap.put(meta, backgroundBlockState);
	}
	
	public IBlockState getBackgroundBlockState()
	{
		return getBackgroundBlockState(0);
	}

	public IBlockState getBackgroundBlockState(int meta)
	{
		if (bgBlockStateMap.get(meta) != null)
		{
			return bgBlockStateMap.get(meta);
		}

		System.err.println("Error: Background blockstate may not have been mapped for this variant. Returning null.");
		
		return null;
	}
	
	public void setBackgroundModelLocation(ModelResourceLocation location)
	{
		setBackgroundModelLocation(location, 0);
	}
	
	public void setBackgroundModelLocation(ModelResourceLocation location, int meta)
	{
		bgModelLocationMap.put(meta, location);
	}
	
	public ModelResourceLocation getBackgroundModelLocation(int meta)
	{
		if (bgModelLocationMap.get(meta) != null)
		{
			return bgModelLocationMap.get(meta);
		}
		
		System.err.println("Error: Background model location may not have been mapped for this variant. Returning null.");
		
		return null;
	}
	
	public void setLitRedstoneVariants(BlockOresBase block)
	{
		for (IBlockState unlitState : this.getBlockState().getValidStates())
		{
			for (IBlockState litState : block.getBlockState().getValidStates())
			{
				if (getMetaFromState(unlitState) == getMetaFromState(litState))
				{
					int meta = getMetaFromState(unlitState); // || getMetaFromState(litState)
					
					litRedstoneVariantMap.put(meta, litState);
				}
			}
		}
	}
	
	public IBlockState getLitRedstoneVariant()
	{
		return getLitRedstoneVariant(0);
	}
	
	public IBlockState getLitRedstoneVariant(int meta)
	{
		if (isNormalRedstone()) return litRedstoneVariantMap.get(meta);
		
		System.err.println("Error: tried to retrieve a lit variant from an invalid candidate. Returning null.");
		
		return null;
	}
	
	public void setNormalRedstoneVariants(BlockOresBase block)
	{
		for (IBlockState litState : this.getBlockState().getValidStates())
		{
			for (IBlockState unlitState : block.getBlockState().getValidStates())
			{
				if (getMetaFromState(unlitState) == getMetaFromState(litState))
				{
					int meta = getMetaFromState(unlitState); // || getMetaFromState(litState)
					
					normalRedstoneVariantMap.put(meta, unlitState);
				}
			}
		}
	}
	
	public IBlockState getNormalRedstoneVariant()
	{
		return getNormalRedstoneVariant(0);
	}
	
	public IBlockState getNormalRedstoneVariant(int meta)
	{
		if (isLitRedstone()) return this.normalRedstoneVariantMap.get(meta);
		
		System.err.println("Error: tried to retrieve normal redstone from an invalid candidate. Returning null.");
		
		return null;
	}
	
	
	private IBlockState ensureNotLit()
	{
		return ensureNormalRedstone(0);
	}
	
	private IBlockState ensureNormalRedstone(int meta)
	{
		if (getNormalRedstoneVariant() != null)
		{
			return getNormalRedstoneVariant();
		}
		
		return this.getStateFromMeta(meta);
	}
	
	public void setDenseVariants(BlockOresBase block)
	{
		for (IBlockState normalState : this.getBlockState().getValidStates())
		{
			for (IBlockState denseState : block.getBlockState().getValidStates())
			{
				if (getMetaFromState(normalState) == getMetaFromState(denseState))
				{
					int meta = getMetaFromState(normalState); // || getMetaFromState(denseState)
					
					denseVariantMap.put(meta, denseState);
				}
			}
		}
	}
	
	public IBlockState getDenseVariant()
	{
		return getDenseVariant(0);
	}
	
	public IBlockState getDenseVariant(int meta)
	{
		if (isNormalVariant()) return this.denseVariantMap.get(meta);
		
		System.err.println("Error: tried to retrieve a normal variant from an invalid candidate. Returning null.");
		
		return null;
	}
	
	public void setNormalVariants(BlockOresBase block)
	{		
		for (IBlockState denseState : this.getBlockState().getValidStates())
		{
			for (IBlockState normalState : block.getBlockState().getValidStates())
			{
				if (getMetaFromState(normalState) == getMetaFromState(denseState))
				{
					int meta = getMetaFromState(normalState); // || getMetaFromState(denseState)
					
					normalVariantMap.put(meta, normalState);
				}
			}
		}
	}
	
	public IBlockState getNormalVariant()
	{
		return getNormalVariant(0);
	}
	
	public IBlockState getNormalVariant(int meta)
	{
		if (isDenseVariant()) return normalVariantMap.get(meta);
		
		System.err.println("Error: tried to retrieve a dense variant from an invalid candidate. Returning null.");
		
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
        			
        			Item item = null;
        			int meta = 0;
        			
        			//drop == dropAlt + variantsDrop == true -> drop self
        			if (drop.canDropSelf() && ConfigFile.variantsDrop)
        			{
            			item = Item.getItemFromBlock(this);
                		meta = this.getMetaFromState(state);
        			}
        			
        			else //no silk touch? -> use dropLookup
        			{
        				if (drop.isDropBlock())
            			{
        					item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(drop.getDropLookup()));
            			}
            			
            			else //if (!drop.isDropBlock())
            			{            				
            				quantity = fortune > 0 ? quantity * (MathHelper.abs(worldIn.rand.nextInt(fortune + 2) - 1) + 1) : quantity;
            				
            				item = ForgeRegistries.ITEMS.getValue(drop.getDropLookup());
            			}
        				
        				meta = drop.getDropMeta();
        			}
        			
        			//Spawning multiple entities as opposed to a larger ItemStack for a more authentic visual effect.
        			for (int i = 0; i < quantity; i++) drops.add(new ItemStack(item, 1, meta));
        		}
        	}
    	}
    }
	
	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state)
    {
		int meta = this.getMetaFromState(state);
		Item item = Item.getItemFromBlock(ensureNotLit().getBlock());
		
		if (!ConfigFile.variantsDropWithSilkTouch)
		{
			item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(props.getDropProperties()[0].getDropAltLookup()));
			meta = props.getDropProperties()[0].getDropAltMeta();
		}

		return new ItemStack(item, 1, meta);
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
		return new ItemStack(Item.getItemFromBlock(ensureNotLit().getBlock()), 1, getMetaFromState(world.getBlockState(pos)));
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
       		worldIn.setBlockState(pos, litRedstoneVariantMap.get(getMetaFromState(state)));
       		
       		this.spawnParticles(worldIn, pos);
       	}
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {    	
    	if (isLitRedstone()) worldIn.setBlockState(pos, normalRedstoneVariantMap.get(getMetaFromState(state)));
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
		Main.proxy.registerVariantRenderer(Item.getItemFromBlock(this), 0, this.getRegistryName().getResourcePath());
	}
}
