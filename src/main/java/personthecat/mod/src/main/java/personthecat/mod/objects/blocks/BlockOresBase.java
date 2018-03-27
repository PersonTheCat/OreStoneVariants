package personthecat.mod.objects.blocks;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.Reference;

public class BlockOresBase extends BlockBase implements IHasModel
{
protected boolean isDropBlock, imNormalRedstone, imLitRedstone; //changeRenderLayer;
protected int leastDrop, mostDrop, leastXp, mostXp, dropMeta, dropAltMeta;
protected ResourceLocation dropLookup, dropAltLookup;

//Dummies
protected static float getHardness;
protected static int getLevel;
	
	public BlockOresBase(String name, boolean isDynamic, boolean useVariants, int enumerate)
	{
		super(name, Material.ROCK, getHardness, "pickaxe", getLevel, isDynamic, useVariants, enumerate);
		
		OreProperties properties = OreProperties.propertiesOf(name);
		
		setResistance(15.0f);
		setDefaultState(this.blockState.getBaseState());

		if (name.contains("redstone"))
		{
			if (name.contains("lit_")) this.imLitRedstone = true;
				
			else this.imNormalRedstone = true;
		}
		
		if (imLitRedstone)
		{
			setLightLevel(0.625F);
			setTickRandomly(true);
		}
		
		this.isDropBlock = properties.isDropBlock();
		this.dropLookup = properties.getDropLookup();
		this.dropMeta = properties.getDropMeta();
		this.dropAltLookup = properties.getDropAltLookup();
		this.dropAltMeta = properties.getDropAltMeta();
		this.leastDrop = properties.getLeastDrop();
		this.mostDrop = name.contains("dense_") ? properties.getMostDrop() * 3 : properties.getMostDrop(); 
		this.leastXp = properties.getLeastXp();
		this.mostXp = properties.getMostXp();
	}
	
	@Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune)
    {
    	Random rand = world instanceof World ? ((World)world).rand : new Random();
        
    	if (this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this))
        {
            int i = 0;
            i = MathHelper.getInt(rand, leastXp, mostXp);
            return i;
        }
        
    	return 0;
    }
    
    /**
     * This is necessary so the block can switch render types when it's being mined. TRANSLUCENT is used for the overlay texture
     * to be able to blend into the main texture (i.e. it can have more than one opacity level) instead of strictly overlaying it,
     * but doing so also means that the block becomes fully or partly invisible when mining it. Switching to CUTOUT_MIPPED, CUTOUT, 
     * or SOLID prevents the block from turning into an x-ray machine. I just used CUTOUT_MIPPED because it still looks better than 
     * either of the other options. 
     */
	
	//Edit: this appears to no longer do anything. Soooooo.... 
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
    	//if (ConfigFile.noTranslucent)    	
    		//return (getBlockLayer() == BlockRenderLayer.CUTOUT_MIPPED);
    	//else
    		//return (getBlockLayer() == BlockRenderLayer.TRANSLUCENT || getBlockLayer() == BlockRenderLayer.CUTOUT_MIPPED);
    	
    	return (getBlockLayer() == BlockRenderLayer.CUTOUT_MIPPED);
    }
    
    //Changes the render type when the block is being mined using onBlockClicked (below). See above.
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
    	//if (changeRenderLayer || ConfigFile.noTranslucent)
    		//return BlockRenderLayer.CUTOUT_MIPPED;
    	//else 
    		//return BlockRenderLayer.TRANSLUCENT;
    	
    	return BlockRenderLayer.CUTOUT_MIPPED;
    }
    
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
    	//changeRenderLayer = true;
    	
    	if (imNormalRedstone) this.activate(worldIn, pos);
    	
    	super.onBlockClicked(worldIn, pos, playerIn);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
    	if (imNormalRedstone) this.activate(worldIn, pos);
    	
    	return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
    
    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
    	if (imNormalRedstone) this.activate(worldIn, pos);

    	super.onEntityWalk(worldIn, pos, entityIn);
    }
    
    protected void activate(World worldIn, BlockPos pos)
    {        
       	if (imNormalRedstone)
       	{
       		IBlockState state = worldIn.getBlockState(pos);
       		int meta = this.getMetaFromState(state);
       		worldIn.setBlockState(pos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Reference.MODID, this.getRegistryName().getResourcePath().replaceAll("redstone", "lit_redstone"))).getStateFromMeta(meta));
       	}
       	
       	this.spawnParticles(worldIn, pos);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    	if (imLitRedstone)
    	{
       		int meta = this.getMetaFromState(state);
    		worldIn.setBlockState(pos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Reference.MODID, this.getRegistryName().getResourcePath().replaceAll("lit_redstone", "redstone"))).getStateFromMeta(meta));
   		}
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
   
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		Item item;
		
		if (isDropBlock)
		{
			if (ConfigFile.variantsDrop)
			{
				item = Item.getItemFromBlock(this);
			}
			
			else item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(dropAltLookup));
		}
		
		else item = ForgeRegistries.ITEMS.getValue(dropLookup);
		
		return item;
	}
	
	@Override
	public int damageDropped(IBlockState state)
	{
		return this.dropMeta;
	}
	
	@Override
	public int quantityDropped(Random random)
	{
		Random rand = new Random();
		int i = MathHelper.getInt(rand, leastDrop, mostDrop);
		
		return i;
	}
	
	@Override
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped((IBlockState)this.getBlockState().getValidStates().iterator().next(), random, fortune))
        {
            int i = random.nextInt(fortune + 2) - 1;

            if (i < 0) i = 0;

            return this.quantityDropped(random) * (i + 1);
        }
        else
        {
            return this.quantityDropped(random);
        }
    }
	
	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state)
    {
		Item item;
		int meta = this.getMetaFromState(state);
		
		if (ConfigFile.variantsDropWithSilkTouch)
		{
			if (imLitRedstone) item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(getRegistryName().getResourceDomain(), getRegistryName().getResourcePath().replaceAll("lit_", ""))));
			
			else item = Item.getItemFromBlock(this);
		}
		else
		{
			item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(dropAltLookup));
			meta = dropAltMeta;
		}

		return new ItemStack(item, 1, meta);
    }
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{		
		return new ItemStack(Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Reference.MODID, this.getRegistryName().getResourcePath().replaceAll("lit_", "")))), 1, getMetaFromState(world.getBlockState(pos)));
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

    //Had to do some copypasta in order to get the exact particle effect from redstone_ore.
    protected void spawnParticles(World worldIn, BlockPos pos)
    {
        Random random = worldIn.rand;
        double d0 = 0.0625D;

        for (int i = 0; i < 6; ++i)
        {
            double d1 = (double)((float)pos.getX() + random.nextFloat());
            double d2 = (double)((float)pos.getY() + random.nextFloat());
            double d3 = (double)((float)pos.getZ() + random.nextFloat());

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
}
