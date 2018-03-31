package personthecat.mod.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.config.ModConfigReader;
import personthecat.mod.objects.blocks.BlockOresDynamic;
import personthecat.mod.objects.blocks.BlockOresEnumerated;
import personthecat.mod.objects.blocks.BlockOresEnumeratedQuark;
import personthecat.mod.properties.DefaultProperties;
import personthecat.mod.properties.DefaultProperties.DefaultOreProperties;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class BlockInit 
{	
public static final List<Block> BLOCKS = new ArrayList<Block>();
public static final List<IBlockState> BLOCKSTATES = new ArrayList<IBlockState>();

public static final Map<Block, OreProperties> BLOCK_PROPERTY_MAP = new HashMap<Block, OreProperties>();

//For convenience --at one point, I used an object to hold some similar values, but I ultimately decided to just use less code by creating these instead.
public static final Map<IBlockState, State> BLOCKSTATE_STATE_MAP = new HashMap<IBlockState, State>();
public static final Map<IBlockState, Integer> DYNAMIC_BLOCKSTATES_NUMBER_MAP = new HashMap<IBlockState, Integer>();

	public static void init()
	{	
		DefaultOreProperties[] initPropsHaxCheatQuitModsFakeNews = DefaultProperties.DefaultOreProperties.values();
		addStoneMores();
		addDynamicOres();
	}
	
	private static void addStoneMores()
	{
		initBaseOres("base");

		if (Main.isQuarkLoaded && ModConfigReader.quarkLimestoneOn && ModConfigReader.quarkMarbleOn && ConfigFile.quarkSupport) 
			initBaseOres("quark");
	}

	//This does mean I can't make static references to any ore. Oh well...
	private static void initBaseOres(String enumChooser)
	{				
		for (PropertyGroup group : PropertyGroup.PROPERTY_GROUP_REGISTRY)
		{						
			if (group.getConditions())
			{
				for (OreProperties property : group.getProperties())
				{					
					classChooser(property.getName(), enumChooser);
					
					if (ConfigFile.denseVariants)
					{
						classChooser("dense_" + property.getName(), enumChooser);
					}
				}
			}
		}
	}
	
	private static void classChooser(String name, String enumChooser)
	{
		if (!StringUtils.isAnyEmpty(ConfigInterpreter.disabledOres))
		{
			for (String blockToCancel : ConfigInterpreter.disabledOres)
			{
				if (blockToCancel.equals(NameReader.getOreIgnoreDense(name))) return;
			}
		}
		
		Block newBlock = null;
		
		if (enumChooser.equals("quark"))
		{
			newBlock = new BlockOresEnumeratedQuark(name);
		}
		
		else newBlock = new BlockOresEnumerated(name);

		BLOCKS.add(newBlock);
	}

	/*
	 * You can see where dense ores never get created for dynamic blocks. 
	 * That's just because it was never a part of my plans and adding them for these blocks was just causing too many issues.
	 * Definitely possible to fix, and doing so wouldn't take long, but testing that thoroughly will. 
	*/
	private static void addDynamicOres()
	{
		for (int i = 0; i < ConfigInterpreter.DYNAMIC_BLOCK_ENTRIES.size(); i++)
		{
			if (!StringUtils.isAnyEmpty(ConfigInterpreter.DYNAMIC_BLOCK_ENTRIES.get(i)))
			{
				String name = ConfigInterpreter.getUnenumeratedName(i);
				
				if (name.contains("_ore"))
				{
					Block newBlock = new BlockOresDynamic(i, name);
					OreProperties properties = OreProperties.propertiesOf(name);

					BLOCKS.add(newBlock);
				
					if (name.equals("redstone_ore"))
					{
						BLOCKS.add(new BlockOresDynamic(i, "lit_" + name)); //No need to map lit redstone--never gonna use it later.
					}
				}
				else
				{
					PropertyGroup list = null;
					
					if (name.equals("vanilla") || name.equals("base"))
					{
						list = PropertyGroup.PROPERTY_GROUP_MAP.get("minecraft");
					}
					
					else list = PropertyGroup.PROPERTY_GROUP_MAP.get(name);
					
					for (OreProperties property : list.getProperties())
					{						
						String oreType = property.getName();
						Block newBlock = new BlockOresDynamic(i, oreType);
						
						BLOCKS.add(newBlock);
					}
					
					//Don't need to create another block for lit redstone, because that's part of the property group. 
				}
			}
		}	
	}	
}