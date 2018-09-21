package personthecat.mod.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.CreativeTab;
import personthecat.mod.config.Cfg;
import personthecat.mod.config.ModConfigReader;
import personthecat.mod.objects.blocks.BlockEntry;
import personthecat.mod.objects.blocks.BlockGroup;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.objects.blocks.BlockOresBase.VariantType;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;

import static personthecat.mod.Main.logger;

public class BlockInit 
{	
public static final List<BlockOresBase> BLOCKS = new ArrayList<>();
public static final List<IBlockState> BLOCKSTATES = new ArrayList<>();

	public static void init()
	{
		PropertyGroup.Builder.buildAll();
		BlockGroup.Builder.buildAll();
		Cfg.testForDuplicateBGBlocks();
		BlockEntry.setupEntriesFromRegistry();
		
		initOres();
		
		BlockEntry.clearAllReferences();
		CreativeTab.postBlockInit();
	}
	
	private static void initOres()
	{
		logger.info("There are " + BlockEntry.BLOCK_ENTRY_REGISTRY.size() + " entries to process.");
		
		for (BlockEntry entry : BlockEntry.BLOCK_ENTRY_REGISTRY)
		{
			for (OreProperties props : entry.getPropertyGroup().getProperties())
			{
				if (shouldCreateOre(entry.getPropertyGroup(), entry.getBlockGroup()))
				{
					createAndRegisterVariants(new BlockOresBase(props, entry.getBlockGroup()));
				}				
			}
		}
		
		logger.info("All ores have been processed.");
	}
	
	private static void createAndRegisterVariants(BlockOresBase ofOre)
	{
		if (ofOre.getProperties().getName().equals("redstone_ore"))
		{
			BlockOresBase litVariant = ofOre.createVariant(VariantType.LIT_REDSTONE);
			
			if (Cfg.denseCat.generalDenseCat.denseVariants) litVariant.createVariant(VariantType.DENSE);
		}
		
		if (Cfg.denseCat.generalDenseCat.denseVariants) ofOre.createVariant(VariantType.DENSE);
		
		ofOre.finalizePropertiesAndRegisterAllVariants();
	}
	
	/**
	 * Quark and UBC are still getting special treatment.
	 */
	private static boolean shouldCreateOre(PropertyGroup props, BlockGroup blocks)
	{
		if (blocks.getName().equals("quark"))
		{
			if (!(ModConfigReader.quarkLimestoneOn && ModConfigReader.quarkMarbleOn))
			{
				return false;
			}
		}
		
		if (Loader.isModLoaded("undergroundbiomes") && doesUBCSupportMod(props.getName()))
		{
			return false;
		}
		
		return true;
	}
	
	private static boolean doesUBCSupportMod(String modName)
	{
		if (modName.equals("minecraft")) return true;
		
		for (ResourceLocation allBlocks : ForgeRegistries.BLOCKS.getKeys())
		{
			if (allBlocks.getResourceDomain().equals("undergroundbiomes"))
			{
				if (allBlocks.getResourcePath().contains(modName))
				{
					return true;
				}
			}
		}
		
		return false;
	}
}