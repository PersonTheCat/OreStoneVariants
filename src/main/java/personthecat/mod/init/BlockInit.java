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
import personthecat.mod.objects.blocks.BlockOresBase;
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
						
						if (property.getName().equals("redstone_ore"))
						{
							for (BlockOresBase newBlock : newBlocks)
							{
								BlockOresBase litVariant = ClassChooser.createLitVariant(newBlock);
								
								if (ConfigFile.denseVariants)
								{
									ClassChooser.createDenseVariant(litVariant);
								}
							}
						}
						
						if (ConfigFile.denseVariants)
						{
							for (BlockOresBase newBlock : newBlocks)
							{
								ClassChooser.createDenseVariant(newBlock);
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
			BlockOresBase litVariant = ClassChooser.createLitVariant(newBlock);
			
			if (ConfigFile.denseVariants) //dense lit
			{
				ClassChooser.createDenseVariant(litVariant);
			}
		}
		
		if (ConfigFile.denseVariants) //dense
		{
			ClassChooser.createDenseVariant(newBlock);
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
			
			else return new BlockOresBase[]
			{
			 	new BlockOresEnumerated(name)
			};
		}
		
		/*
		 * There has to be a better way to do these...
		 * Serialization? Cloning? Composition?
		 */
		public static BlockOresBase createLitVariant(BlockOresBase ofBlock)
		{
			BlockOresBase litVariant = null;
			
			String newName = ofBlock.getOriginalName().replaceAll("redstone_ore", "lit_redstone_ore");
			
			if (ofBlock instanceof BlockOresEnumerated)
			{
				if (ofBlock instanceof BlockOresEnumeratedMineralogy1)
				{
					litVariant = new BlockOresEnumeratedMineralogy1(newName);
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedMineralogy2)
				{
					litVariant = new BlockOresEnumeratedMineralogy2(newName);
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedQuark)
				{
					litVariant = new BlockOresEnumeratedQuark(newName);
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedUndergroundBiomes1)
				{
					litVariant = new BlockOresEnumeratedUndergroundBiomes1(newName);
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedUndergroundBiomes2)
				{
					litVariant = new BlockOresEnumeratedUndergroundBiomes2(newName);
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedUndergroundBiomes3)
				{
					litVariant = new BlockOresEnumeratedUndergroundBiomes3(newName);
				}
				
				else litVariant = new BlockOresEnumerated(newName);
			}
			
			else if (ofBlock instanceof BlockOresDynamic)
			{
				BlockOresDynamic asDynamicBlock = (BlockOresDynamic) ofBlock;
				
				litVariant = new BlockOresDynamic(asDynamicBlock.getOriginalEnumeration(), newName, true);			
			}
			
			else litVariant = new BlockOresBase(newName);
			
			BlockOresBase.assignNormalAndLitRedstone(ofBlock, litVariant);
			
			return litVariant;
		}
		
		public static BlockOresBase createDenseVariant(BlockOresBase ofBlock)
		{
			BlockOresBase denseVariant = null;
			
			String newName = "dense_" + ofBlock.getOriginalName();
			
			if (ofBlock instanceof BlockOresEnumerated)
			{
				if (ofBlock instanceof BlockOresEnumeratedMineralogy1)
				{
					denseVariant = new BlockOresEnumeratedMineralogy1(newName);		
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedMineralogy2)
				{
					denseVariant = new BlockOresEnumeratedMineralogy2(newName);		
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedQuark)
				{
					denseVariant = new BlockOresEnumeratedQuark(newName);							
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedUndergroundBiomes1)
				{
					denseVariant = new BlockOresEnumeratedUndergroundBiomes1(newName);							
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedUndergroundBiomes2)
				{
					denseVariant = new BlockOresEnumeratedUndergroundBiomes2(newName);							
				}
				
				else if (ofBlock instanceof BlockOresEnumeratedUndergroundBiomes3)
				{
					denseVariant = new BlockOresEnumeratedUndergroundBiomes3(newName);							
				}
				
				else
				{
					denseVariant = new BlockOresEnumerated(newName);
				}
			}
			
			else if (ofBlock instanceof BlockOresDynamic)
			{
				BlockOresDynamic asDynamicBlock = (BlockOresDynamic) ofBlock;
				
				denseVariant = new BlockOresDynamic(asDynamicBlock.getOriginalEnumeration(), newName, true);			
			}
			
			else denseVariant = new BlockOresBase(newName);
			
			BlockOresBase.assignDenseAndNormalVariants(denseVariant, ofBlock);
			
			return denseVariant;
		}
	}
}