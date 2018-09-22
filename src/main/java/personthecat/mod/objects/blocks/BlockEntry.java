package personthecat.mod.objects.blocks;

import static personthecat.mod.Main.logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.config.Cfg;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.CommonMethods;

public class BlockEntry
{
	private final PropertyGroup propertyGroup;
	private final BlockGroup[] blockGroups;

	/**Ignore repeated entries.*/
	public static final Set<BlockEntry> BLOCK_ENTRY_REGISTRY = new HashSet<>();
	
	public BlockEntry(String configEntry)
	{
		String[] split = splitEntry(configEntry);
		
		String props = split[0];
		String blocks = split[1];
		
		this.propertyGroup = PropertyGroup.findOrCreateGroup(props);
		this.blockGroups = getBlockGroups(blocks);
		
		propertyGroup.setPropsInUse();
		
		BLOCK_ENTRY_REGISTRY.add(this);
	}
	
	private BlockEntry(PropertyGroup props, BlockGroup[] blocks)
	{
		this.propertyGroup = props;
		this.blockGroups = blocks;
		
		propertyGroup.setPropsInUse();
		
		BLOCK_ENTRY_REGISTRY.add(this);
	}
	
	public static String[] splitEntry(String entry)
	{
		String[] split = entry.split(",");
		
		if (split.length < 2)
		{
			split = entry.split(" ");
			
			split = ArrayUtils.removeAllOccurences(split, "");
		}
		
		split[0] = split[0].trim();
		split[1] = split[1].trim();
		
		return split;
	}
	
	private static BlockGroup[] getBlockGroups(String blocks)
	{
		if (blocks.endsWith(":*"))
		{
			Block parent = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blocks.substring(0, blocks.length() - 2)));
			
			String name = CommonMethods.formatStateName(parent.getDefaultState());
			IBlockState[] states = parent.getBlockState().getValidStates().toArray(new IBlockState[0]);
			
			return new BlockGroup[] { new BlockGroup(name, states) };
		}
		else
		{
			if (blocks.equals("all"))
			{
				BlockGroup.isGroupAllInUse = true;
				
				return BlockGroup.BLOCK_GROUP_REGISTRY.toArray(new BlockGroup[0]);
			}
			else if (blocks.equals("default"))
			{
				BlockGroup.isGroupAllInUse = true;
				
				return BlockGroup.getDefaultGroups().toArray(new BlockGroup[0]);
			}
			else return new BlockGroup[] { BlockGroup.findOrCreateGroup(blocks) };
		}
	}

	public static void setupEntriesFromRegistry()
	{
		logger.info("Setting up entries from the registry.");

		for (String entry : Cfg.blockRegistryCat.registry.values)
		{			
			String mod = splitEntry(entry)[0];
			
			if (PropertyGroup.isDefaultGroup(mod))
			{
				if (!Loader.isModLoaded(mod) || !Cfg.isSupportEnabled(mod))
				{
					if (Cfg.blockRegistryCat.registry.logSkippedEntries)
					{
						logger.info("Conditions not met for: \"" + entry + ".\" Skipping...");
					}
					
					continue;
				}
			}
			
			new BlockEntry(entry);
		}
		
		logger.info("Entries processed successfully.");
	}
	
	public BlockGroup[] getBlockGroups()
	{
		return blockGroups;
	}
	
	public PropertyGroup getPropertyGroup()
	{
		return propertyGroup;
	}
	
	@Override
	public String toString()
	{
		return "{" + propertyGroup.getName() + " ores -> " + Arrays.toString(blockGroups) + " blocks}";
	}
	
	public static void clearAllReferences()
	{
		logger.info("Removing temporary block entries from the registry.");
		
		BLOCK_ENTRY_REGISTRY.clear();
	}
}