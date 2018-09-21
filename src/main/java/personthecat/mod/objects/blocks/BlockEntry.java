package personthecat.mod.objects.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.config.Cfg;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.CommonMethods;

import static personthecat.mod.Main.logger;

public class BlockEntry
{
	private final BlockGroup blockGroup;
	private final PropertyGroup propertyGroup;

	/**Ignore repeated entries.*/
	public static final Set<BlockEntry> BLOCK_ENTRY_REGISTRY = new HashSet<>();
	
	public BlockEntry(String configEntry)
	{
		String[] split = configEntry.split(",");
		
		String props = split[0].trim();
		String blocks = split[1].trim();
		
		this.propertyGroup = PropertyGroup.getPropertyGroup(props);
		
		propertyGroup.setPropsInUse();
		
		if (blocks.endsWith(":*"))
		{
			Block parent = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blocks.substring(blocks.length() - 2)));
			
			String name = CommonMethods.formatStateName(parent.getDefaultState());
			IBlockState[] states = parent.getBlockState().getValidStates().toArray(new IBlockState[0]);
			
			this.blockGroup = new BlockGroup(name, states);
		}
		else
		{
			BlockGroup temporaryBlocks = BlockGroup.findOrCreateGroup(blocks);
			
			if (temporaryBlocks.equals(BlockGroup.ALL))
			{
				BlockGroup.isGroupAllInUse = true;
				
				this.blockGroup = BlockGroup.BLOCK_GROUP_REGISTRY.get(0);
				
				finishSplitEntry(propertyGroup, BlockGroup.BLOCK_GROUP_REGISTRY);
			}
			else if (temporaryBlocks.equals(BlockGroup.DEFAULT))
			{
				BlockGroup.isGroupAllInUse = true;
				
				List<BlockGroup> defaultGroups = BlockGroup.getDefaultGroups();
				
				this.blockGroup = defaultGroups.get(0);
				
				finishSplitEntry(propertyGroup, defaultGroups);
			}
			else this.blockGroup = temporaryBlocks;
		}
		
		BLOCK_ENTRY_REGISTRY.add(this);
	}
	
	private BlockEntry(PropertyGroup props, BlockGroup blocks)
	{
		this.propertyGroup = props;
		this.blockGroup = blocks;
		
		BLOCK_ENTRY_REGISTRY.add(this);
	}
	
	/**
	 * Separates entries that use "all" and "default."
	 * Allows repeated entries to be ignored.
	 */
	private void finishSplitEntry(PropertyGroup props, List<BlockGroup> blocks)
	{
		if (blocks.size() > 1)
		{
			for (int i = 1; i < blocks.size(); i++)
			{
				new BlockEntry(props, blocks.get(i));
			}
		}
	}

	public static void setupEntriesFromRegistry()
	{
		logger.info("Setting up entries from the registry.");

		for (String entry : Cfg.blockRegistryCat.registry.values)
		{			
			new BlockEntry(entry);
		}
		
		logger.info("Entries processed successfully.");
	}
	
	public BlockGroup getBlockGroup()
	{
		return blockGroup;
	}
	
	public PropertyGroup getPropertyGroup()
	{
		return propertyGroup;
	}
	
	@Override
	public String toString()
	{
		return "{" + propertyGroup.getName() + " ores -> " + blockGroup.getName() + " blocks}";
	}
	
	public static void clearAllReferences()
	{
		logger.info("Removing temporary block entries from the registry.");
		
		BLOCK_ENTRY_REGISTRY.clear();
	}
}