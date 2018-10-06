package personthecat.mod.objects.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import personthecat.mod.config.ModConfigReader;

import static  personthecat.mod.util.CommonMethods.*;
import static personthecat.mod.Main.logger;

public class BlockGroup
{
	private final IBlockState[] blocks;
	private final String name;
	
	public static final List<BlockGroup> BLOCK_GROUP_REGISTRY = new ArrayList<>();
	
	public BlockGroup(String name, IBlockState[] blocks)
	{
		this.name = name;
		this.blocks = blocks;
		
		testBlocks();
		
		BLOCK_GROUP_REGISTRY.add(this);
	}
	
	public String getName()
	{
		return name;
	}
	
	public IBlockState[] getBlocks()
	{
		return blocks;
	}
	
	public int size()
	{
		return blocks.length;
	}
	
	public static boolean isGroupRegistered(String name)
	{
		for (BlockGroup group : BLOCK_GROUP_REGISTRY)
		{
			if (group.name.equals(name)) return true;
		}
		
		return false;
	}
	
	public static BlockGroup findOrCreateGroup(String nameOrRegistry)
	{
		for (BlockGroup group : BLOCK_GROUP_REGISTRY)
		{
			if (group.name.equals(nameOrRegistry))
			{
				return group;
			}
		}
		
		return new Builder(
			formatRL(new ResourceLocation(nameOrRegistry)),
			new String[] {nameOrRegistry}
		)
		.build();
	}
	
	public boolean isDefaultGroup()
	{
		return isDefaultGroup(name);
	}
	
	public static boolean isDefaultGroup(String name)
	{
		for (Builder b : Builder.DEFAULT_GROUP_INFO)
		{
			if (b.name.equals(name)) return true;
		}
		
		return false;
	}
	
	public static List<BlockGroup> getDefaultGroups()
	{
		List<BlockGroup> defaultGroups = new ArrayList<>();
		
		for (BlockGroup group : BLOCK_GROUP_REGISTRY)
		{
			if (group.isDefaultGroup()) defaultGroups.add(group);
		}
		
		return defaultGroups;
	}
	
	private void testBlocks()
	{
		for (IBlockState state : blocks)
		{
			if (state.equals(Blocks.AIR.getDefaultState()))
			{
				throw new IllegalStateException(
					"Error: Unable to find one or more of the blocks listed in block group \"" + name + ".\" "
					+ "Please ensure that everything is typed correctly.");
			}
		}
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	public static class Builder
	{
		private final String[] blocks;
		private final String name;

		private static final List<Builder> BG_BUILDER_REGISTRY = new ArrayList<>();
		/**Need this list before Blocks have been registered.*/
		public static Builder[] DEFAULT_GROUP_INFO = new Builder[]
		{
			new Builder("minecraft", true, new String[] 
			{
			 	"stone", "stone:5", "stone:3", "stone:1"
			}),
			new Builder("earthworks", true, new String[]
			{
			 	"earthworks:block_chalk", "earthworks:block_slate",
			 	"earthworks:block_slate_green", "earthworks:block_slate_purple"
			}),
			new Builder("mineralogy", true, new String[]
			{
			 	"mineralogy:amphibolite", "mineralogy:andesite", "mineralogy:basalt",
			 	"mineralogy:chert", "mineralogy:conglomerate", "mineralogy:diorite",
			 	"mineralogy:dolomite", "mineralogy:granite", "mineralogy:gypsum",
			 	"mineralogy:limestone", "mineralogy:marble", "mineralogy:pegmatite",
			 	"mineralogy:phyllite", "mineralogy:pumice", "mineralogy:rhyolite",
			 	"mineralogy:schist"
			}),
			new Builder("mineralogy2", true, new String[]
			{
			 	"mineralogy:shale", "mineralogy:slate"
			}),
			new Builder("quark", true, new String[]
			{
			 	"quark:limestone", "quark:marble"
			}),
			new Builder("rustic", true, new String[]
			{
			 	"rustic:slate"
			}),
			/**These were previously sorted in alphabetical order.*/
			new Builder("undergroundbiomes", true, new String[]
			{
			 	"undergroundbiomes:igneous_stone:3", "undergroundbiomes:igneous_stone:5",
			 	"undergroundbiomes:igneous_stone:1", "undergroundbiomes:igneous_stone:7",
			 	"undergroundbiomes:igneous_stone:4", "undergroundbiomes:igneous_stone:6",
			 	"undergroundbiomes:igneous_stone", "undergroundbiomes:igneous_stone:2"
			}),
			new Builder("undergroundbiomes2", true, new String[]
			{
			 	"undergroundbiomes:metamorphic_stone:4", "undergroundbiomes:metamorphic_stone:1",
			 	"undergroundbiomes:metamorphic_stone", "undergroundbiomes:metamorphic_stone:5",
			 	"undergroundbiomes:metamorphic_stone:2", "undergroundbiomes:metamorphic_stone:7",
			 	"undergroundbiomes:metamorphic_stone:3", "undergroundbiomes:metamorphic_stone:6"
			}),
			new Builder("undergroundbiomes3", true, new String[]
			{
			 	"undergroundbiomes:sedimentary_stone:1", "undergroundbiomes:sedimentary_stone:7",
			 	"undergroundbiomes:sedimentary_stone:5", "undergroundbiomes:sedimentary_stone:6",
			 	"undergroundbiomes:sedimentary_stone:4", "undergroundbiomes:sedimentary_stone",
			 	"undergroundbiomes:sedimentary_stone:2", "undergroundbiomes:sedimentary_stone:3"
			})
		};
		
		public Builder(String name, String[] blocks)
		{
			this.name = name;
			this.blocks = blocks;
			
			if (blocks.length > 16)
			{
				throw new IllegalArgumentException("Error: BlockGroups can only hold 16 states.");
			}
			
			BG_BUILDER_REGISTRY.add(this);
		}
		
		private Builder(String name, boolean avoidConflicts, String[] blocks)
		{
			this.name = name;
			this.blocks = blocks;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String[] getPropertyNames()
		{
			return blocks;
		}
		
		public static boolean isBuilderRegistered(String name)
		{
			for (Builder b : BG_BUILDER_REGISTRY)
			{
				if (b.name.equals(name)) return true;
			}
			
			return false;
		}
		
		public void convertRegistry()
		{
			BG_BUILDER_REGISTRY.add(this);
			
			for (int i = 0; i < DEFAULT_GROUP_INFO.length; i++)
			{
				Builder b = DEFAULT_GROUP_INFO[i];
				
				if (b.equals(this))
				{
					// DEFAULT_GROUP_INFO = ArrayUtils.remove(DEFAULT_GROUP_INFO, i);
					
					return;
				}
			}
			
			throw new IllegalStateException(
				"I'm not sure how you got here. Please report this crash to PersonTheCat.");
		}
		
		public static List<Builder> getMatchingDefaultBuilders(String modid)
		{
			List<Builder> builders = new ArrayList<>();
			
			for (Builder b : DEFAULT_GROUP_INFO)
			{
				String name = b.name;
				
				//If the last character is a number.
				if (StringUtils.isNumeric(name.substring(name.length() - 1)))
				{
					if (name.substring(0, name.length() - 1).equals(modid))
					{
						builders.add(b);
					}
				}
				else if (name.equals(modid))
				{
					builders.add(b);
				}
			}
			
			return builders;
		}
		
		public static void buildAll()
		{
			for (Builder b : BG_BUILDER_REGISTRY)
			{
				b.build();
			}
			
			BG_BUILDER_REGISTRY.clear();
		}
		
		public BlockGroup build()
		{
			IBlockState[] finalBlocks = new IBlockState[blocks.length];
			
			for (int i = 0; i < blocks.length; i++)
			{
				finalBlocks[i] = getBlockState(blocks[i]);
			}
			
			return new BlockGroup(name, finalBlocks);
		}
	}
}