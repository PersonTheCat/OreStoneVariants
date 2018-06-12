package personthecat.mod.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.config.ModConfigReader;
import personthecat.mod.objects.blocks.BlockOresDynamic;
import personthecat.mod.objects.blocks.BlockOresEnumerated;
import personthecat.mod.objects.blocks.BlockOresEnumeratedMineralogy1;
import personthecat.mod.objects.blocks.BlockOresEnumeratedMineralogy2;
import personthecat.mod.objects.blocks.BlockOresEnumeratedQuark;
import personthecat.mod.objects.blocks.BlockOresEnumeratedUndergroundBiomes1;
import personthecat.mod.objects.blocks.BlockOresEnumeratedUndergroundBiomes2;
import personthecat.mod.objects.blocks.BlockOresEnumeratedUndergroundBiomes3;
import personthecat.mod.properties.DefaultProperties;
import personthecat.mod.properties.DefaultProperties.DefaultOreProperties;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class BlockInit 
{	
public static final List<Block> BLOCKS = new ArrayList<>();
public static final List<IBlockState> BLOCKSTATES = new ArrayList<>();

public static final Map<IBlockState, State> BLOCKSTATE_STATE_MAP = new HashMap<>();
public static final Map<IBlockState, Integer> DYNAMIC_BLOCKSTATES_NUMBER_MAP = new HashMap<>();

	public static void init()
	{
		addStoneMores();
		addDynamicOres();
	}
	
	private static void addStoneMores()
	{
		if (!ConfigFile.disableVanillaVariants())
		{
			initBaseOres("base");
		}
		
		if (Main.isQuarkLoaded() && ModConfigReader.quarkLimestoneOn && ModConfigReader.quarkMarbleOn && ConfigFile.quarkSupport)
		{
			initBaseOres("quark");
		}
		
		if (Main.isMineralogyLoaded() && ConfigFile.mineralogySupport)
		{
			initBaseOres("mineralogy");
		}
		
		if (Main.isUndergroundBiomesLoaded() && ConfigFile.undergroundBiomesSupport)
		{
			initBaseOres("undergroundbiomes");
		}
	}

	//This does mean I can't make static references to any ore. Oh well...
	private static void initBaseOres(String enumChooser)
	{				
		for (PropertyGroup group : PropertyGroup.getPropertyGroupRegistry())
		{						
			//Only add Undergound Biomes variants if the original mod does not already have the variants.
			
			if (enumChooser.equals("undergroundbiomes"))
			{
				if (doesUndergroundBiomesSupportMod(group.getModName())) continue;
			}
			
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
	
	private static boolean doesUndergroundBiomesSupportMod(String modName)
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
	
	private static void classChooser(String name, String enumChooser)
	{
		if (!StringUtils.isAnyEmpty(ConfigFile.disabledOres))
		{
			for (String blockToCancel : ConfigFile.disabledOres)
			{
				if (blockToCancel.equals(NameReader.getOreIgnoreDense(name))) return;
			}
		}
		
		if (enumChooser.equals("quark"))
		{
			BLOCKS.add(new BlockOresEnumeratedQuark(name));
		}
		
		else if (enumChooser.equals("mineralogy"))
		{
			BLOCKS.add(new BlockOresEnumeratedMineralogy1(name));
			BLOCKS.add(new BlockOresEnumeratedMineralogy2(name));
		}
		
		else if (enumChooser.equals("undergroundbiomes"))
		{
			BLOCKS.add(new BlockOresEnumeratedUndergroundBiomes1(name));
			BLOCKS.add(new BlockOresEnumeratedUndergroundBiomes2(name));
			BLOCKS.add(new BlockOresEnumeratedUndergroundBiomes3(name));
		}
		
		else BLOCKS.add(new BlockOresEnumerated(name));
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
					BLOCKS.add(new BlockOresDynamic(i, name));
				
					if (name.equals("redstone_ore")) BLOCKS.add(new BlockOresDynamic(i, "lit_" + name));
				}
				
				else
				{
					PropertyGroup list = PropertyGroup.getPropertyGroup(name);
					
					if (name.equals("vanilla") || name.equals("base"))
					{
						list = PropertyGroup.getPropertyGroup("minecraft");
					}
					
					for (OreProperties property : list.getProperties())
					{
						BLOCKS.add(new BlockOresDynamic(i, property.getName()));
					}
				}
			}
		}	
	}	
}