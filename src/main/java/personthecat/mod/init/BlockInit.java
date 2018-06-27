package personthecat.mod.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.CreativeTab;
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.config.ConfigInterpreter;
import personthecat.mod.config.ModConfigReader;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.objects.blocks.BlockOresBase.VariantType;
import personthecat.mod.objects.blocks.BlockOresDynamic;
import personthecat.mod.objects.blocks.BlockOresEnumerated;
import personthecat.mod.objects.blocks.BlockOresEnumeratedEarthworks;
import personthecat.mod.objects.blocks.BlockOresEnumeratedMineralogy1;
import personthecat.mod.objects.blocks.BlockOresEnumeratedMineralogy2;
import personthecat.mod.objects.blocks.BlockOresEnumeratedQuark;
import personthecat.mod.objects.blocks.BlockOresEnumeratedUndergroundBiomes1;
import personthecat.mod.objects.blocks.BlockOresEnumeratedUndergroundBiomes2;
import personthecat.mod.objects.blocks.BlockOresEnumeratedUndergroundBiomes3;
import personthecat.mod.properties.OreProperties;
import personthecat.mod.properties.PropertyGroup;
import personthecat.mod.util.NameReader;
import personthecat.mod.util.handlers.BlockStateGenerator.State;

public class BlockInit 
{	
public static final List<BlockOresBase> BLOCKS = new ArrayList<>();
public static final List<IBlockState> BLOCKSTATES = new ArrayList<>();

public static final Map<IBlockState, State> BLOCKSTATE_STATE_MAP = new HashMap<>();

	public static void init()
	{
		addStoneMores();
		addDynamicOres();
		
		CreativeTab.postBlockInit();
	}
	
	private static void addStoneMores()
	{
		if (!ConfigFile.disableVanillaVariants())
		{
			initBaseOres("base");
		}
		
		if (Main.isQuarkLoaded() && ModConfigReader.quarkLimestoneOn && ModConfigReader.quarkMarbleOn && ConfigFile.isSupportEnabled("quark"))
		{
			initBaseOres("quark");
		}
		
		if (Main.isMineralogyLoaded() && ConfigFile.isSupportEnabled("mineralogy"))
		{
			initBaseOres("mineralogy");
		}
		
		if (Main.isUndergroundBiomesLoaded() && ConfigFile.isSupportEnabled("undergroundbiomes"))
		{
			initBaseOres("undergroundbiomes");
		}
		
		if (Main.isEarthworksLoaded() && ConfigFile.isSupportEnabled("earthworks"))
		{
			initBaseOres("earthworks");
		}
	}
	
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
					if (!property.getName().equals("lit_redstone_ore"))
					{
						BlockOresBase[] newBlocks = ClassChooser.choose(property.getName(), enumChooser);
						
						for (BlockOresBase newBlock: newBlocks)
						{
							if (property.getName().equals("redstone_ore"))
							{
								BlockOresBase litVariant = newBlock.createVariant(VariantType.LIT_REDSTONE);
								
								if (ConfigFile.denseVariants)
								{
									litVariant.createVariant(VariantType.DENSE);
								}
							}
							
							if (ConfigFile.denseVariants)
							{
								newBlock.createVariant(VariantType.DENSE);
							}
						}

						for (BlockOresBase newBlock : newBlocks)
						{
							newBlock.finalizePropertiesAndRegisterAllVariants();
						}
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

	private static void addDynamicOres()
	{
		for (int i = 0; i < ConfigInterpreter.DYNAMIC_BLOCK_ENTRIES.size(); i++)
		{
			if (!StringUtils.isAnyEmpty(ConfigInterpreter.DYNAMIC_BLOCK_ENTRIES.get(i)))
			{
				String name = ConfigInterpreter.getUnenumeratedName(i);
				
				if (name.contains("_ore"))
				{
					createAndRegisterDynamicBlock(i, name);
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
						if (!property.getName().equals("lit_redstone_ore"))
						{
							createAndRegisterDynamicBlock(i, property.getName());
						}
					}
				}
			}
		}	
	}
	
	private static void createAndRegisterDynamicBlock(int enumerate, String oreName)
	{
		BlockOresBase newBlock = new BlockOresDynamic(enumerate, oreName);
		
		if (oreName.equals("redstone_ore")) //lit
		{
			BlockOresBase litVariant = newBlock.createVariant(VariantType.LIT_REDSTONE);
			
			if (ConfigFile.denseVariants) //dense lit
			{
				litVariant.createVariant(VariantType.DENSE);
			}
		}
		
		if (ConfigFile.denseVariants) //dense
		{
			newBlock.createVariant(VariantType.DENSE);
		}
		
		newBlock.finalizePropertiesAndRegisterAllVariants();
	}
	
	public static class ClassChooser
	{
		private static BlockOresBase[] choose(String name, String enumChooser)
		{
			if (!StringUtils.isAnyEmpty(ConfigFile.disabledOres))
			{
				for (String blockToCancel : ConfigFile.disabledOres)
				{
					if (blockToCancel.equals(NameReader.getOreIgnoreDense(name)))
					{
						return new BlockOresBase[] {};
					}
				}
			}
			
			if (enumChooser.equals("quark"))
			{
				return new BlockOresBase[]
				{
				 	new BlockOresEnumeratedQuark(name)
				};
			}
			
			else if (enumChooser.equals("mineralogy"))
			{
				return new BlockOresBase[]
				{
					new BlockOresEnumeratedMineralogy1(name),
					new BlockOresEnumeratedMineralogy2(name)
				};
			}
			
			else if (enumChooser.equals("undergroundbiomes"))
			{
				return new BlockOresBase[]
				{
					new BlockOresEnumeratedUndergroundBiomes1(name),
					new BlockOresEnumeratedUndergroundBiomes2(name),
					new BlockOresEnumeratedUndergroundBiomes3(name)
				};
			}
			
			else if (enumChooser.equals("earthworks"))
			{
				return new BlockOresBase[]
				{
					new BlockOresEnumeratedEarthworks(name)	 
				};
			}
			
			else return new BlockOresBase[]
			{
			 	new BlockOresEnumerated(name)
			};
		}
	}
}