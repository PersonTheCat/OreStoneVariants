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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.OreProperties.DropProperties;
import personthecat.mod.util.IHasModel;
import personthecat.mod.util.NameReader;

public class BlockOresBase extends BlockBase implements IHasModel
{
	protected boolean imNormalRedstone, imLitRedstone, changeRenderLayer;
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
	}
	
	protected void setCurrentDrops(DropProperties... drops)
	{
		this.drops = drops;
	}
	
    @Override //Chance is decided onBlockClicked > setCurrentDrops() > drop.getDropPropertiesByChance(). Just reusing this method as an event to spawn all drops.
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {    	
    	if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) //From the docs: this prevents item dupe.
    	{    		
    		for (DropProperties drop : drops)
    		{
    			int quantity = MathHelper.getInt(worldIn.rand, drop.getLeastDrop(), NameReader.isDense(this) ? drop.getMostDrop() * 3 : drop.getMostDrop());
    			
    			Item item = Item.getItemFromBlock(this);
    			int meta = this.getMetaFromState(state);
    			
    			if (drop.isDropBlock() && !ConfigFile.variantsDrop) //Drop is a block, but variants don't drop.
    			{
					item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(drop.getDropAltLookup()));
					meta = drop.getDropAltMeta();
    			}
    			
    			else //Drop is an item.
    			{
    				quantity = fortune > 0 ? quantity * (MathHelper.abs(worldIn.rand.nextInt(fortune + 2) - 1) + 1) : quantity;
    				
    				item = ForgeRegistries.ITEMS.getValue(drop.getDropLookup());
    				meta = drop.getDropMeta();
    			}
    			
    			//Spawning multiple entities as opposed to a larger ItemStack for a more authentic visual effect.
    			for (int i = 0; i < quantity; i++) spawnAsEntity(worldIn, pos, new ItemStack(item, 1, meta));
    		}
    	}
    }
	
	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state)
    {
		Item item = Item.getItemFromBlock(NameReader.getUnlitVariant(this));
		int meta = this.getMetaFromState(state);
		
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
		
		for (DropProperties drop : drops)
		{    			
			if (!drop.isDropBlock()) i += MathHelper.getInt(rand, drop.getLeastXp(), drop.getMostXp());
		}

		return i;
    }
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{		
		return new ItemStack(Item.getItemFromBlock(NameReader.getUnlitVariant(this)), 1, getMetaFromState(world.getBlockState(pos)));
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
    	
    	return 0;
    }
    
    protected void spawnParticles(World worldIn, BlockPos pos)
    {
        Method reflect;
        
        try
        {
        	reflect = ReflectionHelper.findMethod(BlockRedstoneOre.class, "spawnParticles", "func_180489_a", World.class, BlockPos.class);
        	reflect.invoke(Blocks.REDSTONE_ORE, worldIn, pos);
        }
        
        catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {}
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
