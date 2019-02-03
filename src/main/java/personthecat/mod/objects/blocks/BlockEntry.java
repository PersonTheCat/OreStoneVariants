package personthecat.mod.objects.blocks;

import static personthecat.mod.Main.logger;
import static personthecat.mod.util.CommonMethods.*;
import static personthecat.mod.objects.blocks.BlockGroup.BLOCK_GROUP_REGISTRY;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.config.Cfg;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;

public class BlockEntry
{
	private final PropertyGroup propertyGroup;
	private final BlockGroup[] blockGroups;
	private final int ID;

	/**Ignore repeated entries.*/
	public static final Set<BlockEntry> BLOCK_ENTRY_REGISTRY = new HashSet<>();
	private static int lastID = 0;
	
	public BlockEntry(String configEntry)
	{
		String[] split = splitEntry(configEntry);
		
		String props = split[0];
		String blocks = split[1];
		
		this.propertyGroup = PropertyGroup.findOrCreateGroup(props);
		this.blockGroups = getBlockGroups(blocks);
		this.ID = ++lastID;
		
		propertyGroup.setPropsInUse();
		
		BLOCK_ENTRY_REGISTRY.add(this);
	}
	
	private BlockEntry(PropertyGroup props, BlockGroup[] blocks)
	{
		this.propertyGroup = props;
		this.blockGroups = blocks;
		this.ID = ++lastID;
		
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
			
			String name = formatStateName(parent.getDefaultState());
			IBlockState[] states = parent.getBlockState().getValidStates().toArray(new IBlockState[0]);
			
			if (Cfg.blockRegistryCat.registry.separateAsteriskEntries)
			{
				BlockGroup[] groups = new BlockGroup[states.length];
				
				for (int i = 0; i < states.length; i++)
				{
					IBlockState state = states[i];
					name = formatStateName(state);
					
					groups[i] = new BlockGroup(name, new IBlockState[] { state });
				}
				
				return groups;
			}
			else return new BlockGroup[] { new BlockGroup(name, states) };
		}
		else
		{
			if (blocks.equals("all"))
			{
				return BLOCK_GROUP_REGISTRY.toArray(new BlockGroup[0]);
			}
			else if (blocks.equals("default"))
			{				
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
			if (entry.trim().isEmpty()) continue;
			
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
	
	public static void testForDuplicateEntries()
	{
		if (Cfg.blockRegistryCat.registry.safety)
		{
			logger.info("Testing for bad block entries.");
			
			forAllComponents(component1 -> { forAllComponents(component2 ->
			{
				if (component1.compareTo(component2))
				{
					throw new RuntimeException(
						"Error: Potentially dangerous config entries. Multiple blocks will be created using\n"
						+ "the properties \"" + component1.props + "\" inside of " + component1.block + ".\n"
						+ "It is advised that you avoid using repeated entries. To disable this crash,\n"
						+ "set \"Test For Duplicates\" in the config file to false");
				}
			});});
			
			logger.info("No duplicates found.");
		}
	}
	
	private static void forAllComponents(Consumer<EntryComponent> function)
	{
		for (BlockEntry entry : BLOCK_ENTRY_REGISTRY)
		{
			for (OreProperties props : entry.propertyGroup.getProperties())
			{
				for (BlockGroup blockGroup : entry.getBlockGroups())
				{
					for (IBlockState block : blockGroup.getBlocks())
					{
						function.accept(new EntryComponent(entry, props, block));
					}
				}
			}
		}
	}
	
	private static class EntryComponent
	{
		BlockEntry entry;
		OreProperties props;
		IBlockState block;
		
		private EntryComponent(BlockEntry entry, OreProperties props, IBlockState block)
		{
			this.entry = entry;
			this.props = props;
			this.block = block;
		}
		
		/**
		 * @return true if the entry # is not the same, but the elements are.
		 */
		private boolean compareTo(EntryComponent c)
		{
			return entry.ID != c.entry.ID && props.equals(c.props) && block.equals(c.block);
		}
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