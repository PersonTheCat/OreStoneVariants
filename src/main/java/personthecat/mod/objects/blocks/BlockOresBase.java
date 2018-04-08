package personthecat.mod.objects.blocks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.init.BlockInit;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.NameReader;

public class BlockOresBase extends BlockBase implements IHasModel
{
protected boolean imNormalRedstone, imLitRedstone; //changeRenderLayer;
protected OreProperties props;
protected DropProperties[] drops;

//Dummies
protected static float getHardness;
protected static int getLevel;
	
	public BlockOresBase(String name, boolean isDynamic, boolean useVariants, int enumerate)
	{
		super(name, Material.ROCK, getHardness, "pickaxe", getLevel, isDynamic, useVariants, enumerate);
		
		props = OreProperties.propertiesOf(name);

		if (name.contains("redstone"))
		{
			if (name.contains("lit_")) this.imLitRedstone = true;
				
			else this.imNormalRedstone = true;
		}

		setCurrentDrops(props.getDropProperties()[0]);
		setResistance(15.0f);
		setLightLevel(props.getLightLevel());
		setDefaultState(this.blockState.getBaseState());
		if (imLitRedstone) setTickRandomly(true);	
		
		BlockInit.BLOCK_PROPERTY_MAP.put(this, props);
	}
	
	protected void setCurrentDrops(DropProperties... drops)
	{
		this.drops = drops;
	}
	
	@Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune)
    {
    	Random rand = world instanceof World ? ((World)world).rand : new Random();
    	
    		int i = 0;
    		
    		for (DropProperties drop : drops)
    		{    			
    			if (!drop.isDropBlock()) i += MathHelper.getInt(rand, drop.getLeastXp(), drop.getMostXp());
    		}

    		return i;
    }
	
	//Edit: this appears to no longer do anything. Soooooo.... 
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
    	//if (ConfigFile.noTranslucent) return (getBlockLayer() == BlockRenderLayer.CUTOUT_MIPPED);
    	
    	//return (getBlockLayer() == BlockRenderLayer.CUTOUT_MIPPED);
    	
    	return (getBlockLayer() == BlockRenderLayer.TRANSLUCENT | getBlockLayer() == BlockRenderLayer.CUTOUT_MIPPED);
    }
    
    //Changes the render type when the block is being mined using onBlockClicked (below). See above.
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
    	//if (changeRenderLayer || ConfigFile.noTranslucent) return BlockRenderLayer.CUTOUT_MIPPED

    	return BlockRenderLayer.TRANSLUCENT;
    }
    
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
    	//changeRenderLayer = true;
    	
    	this.activate(worldIn, pos);
    	
    	this.setCurrentDrops(props.getDropPropertiesByChance(worldIn, playerIn));
    	
    	super.onBlockClicked(worldIn, pos, playerIn); //Not necessary? I forget why this is here...
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

    	super.onEntityWalk(worldIn, pos, entityIn); //Not necessary? I forget why this is here...
    }
    
    protected void activate(World worldIn, BlockPos pos)
    {        
       	if (imNormalRedstone)
       	{
       		IBlockState state = worldIn.getBlockState(pos);
       		worldIn.setBlockState(pos, NameReader.getLitVariant(this).getStateFromMeta(this.getMetaFromState(state)));
       		
       		this.spawnParticles(worldIn, pos);
       	}
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    	if (imLitRedstone) worldIn.setBlockState(pos, NameReader.getUnlitVariant(this).getStateFromMeta(this.getMetaFromState(state)));
    }
	
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (imLitRedstone) this.spawnParticles(worldIn, pos);
    }

    @Override
    public int tickRate(World worldIn)
    {
    	if (imLitRedstone) return 30;
    	
    	else return 0;
    }
    
    //Chance is decided onBlockClicked > setCurrentDrops() > drop.getDropPropertiesByChance(). Just reusing this method as an event to spawn all drops.
    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {    	
    	if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots)
    	{    		
    		for (DropProperties drop : drops)
    		{
    			Item item = Item.getItemFromBlock(this);
    			
    			int quantity = MathHelper.getInt(worldIn.rand, drop.getLeastDrop(), drop.getMostDrop());
    			if (NameReader.isDense(this)) quantity *= 3;
    			int meta = drop.getDropMeta();
    			
    			if (drop.isDropBlock())
    			{
    				if (ConfigFile.variantsDrop) meta = this.getMetaFromState(state);
    				
    				else
    				{
    					item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(drop.getDropAltLookup()));
    					
    					meta = drop.getDropAltMeta();
    				}
    			}
    			
    			else
    			{
    				item = ForgeRegistries.ITEMS.getValue(drop.getDropLookup());
    				
    				if (fortune > 0)
    				{
    					int i = worldIn.rand.nextInt(fortune + 2) - 1;
    					
    					if (i < 0) i = 0;
    					
    					quantity *= (i + 1);
    				}
    			}
    			
    			//Spawning multiple entities as opposed to a larger ItemStack for a more authentic visual effect.
    			for (int i = 0; i < quantity; i++) spawnAsEntity(worldIn, pos, new ItemStack(item, 1, meta));
    		}
    	}
    }
	
	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state)
    {
		Item item = Item.getItemFromBlock(this);
		int meta = this.getMetaFromState(state);
		
		if (ConfigFile.variantsDropWithSilkTouch)
		{
			if (imLitRedstone) item = Item.getItemFromBlock(NameReader.getUnlitVariant(this));
			
			else item = Item.getItemFromBlock(this);
		}
		else
		{
			item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(props.getDropProperties()[0].getDropAltLookup()));
			meta = props.getDropProperties()[0].getDropAltMeta();
		}

		return new ItemStack(item, 1, meta);
    }
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{		
		return new ItemStack(Item.getItemFromBlock(NameReader.getUnlitVariant(this)), 1, getMetaFromState(world.getBlockState(pos)));
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

    protected void spawnParticles(World worldIn, BlockPos pos)
    {
        Method reflect;
        
        try
        {
        	reflect = ReflectionHelper.findMethod(BlockRedstoneOre.class, "spawnParticles", "func_180489_a", World.class, BlockPos.class);
        	reflect.setAccessible(true);
        	reflect.invoke(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("redstone_ore")), worldIn, pos);
        }
        
        catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {}
    }
}
