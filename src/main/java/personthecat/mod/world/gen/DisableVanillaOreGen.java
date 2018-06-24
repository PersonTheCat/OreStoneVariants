package personthecat.mod.world.gen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import personthecat.mod.config.ConfigFile;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.properties.OreProperties;

public class DisableVanillaOreGen
{
	private static boolean netherQuartzVariantExists;
	
	static
	{
		for (Block ore : BlockInit.BLOCKS)
		{
			if (ore instanceof BlockOresBase)
			{
				BlockOresBase asBOB = (BlockOresBase) ore;
				
				if (asBOB.getBackgroundBlockState().getBlock().equals(Blocks.NETHERRACK) &&
					asBOB.getProperties().equals(OreProperties.propertiesOf("quartz_ore")))
				{
					netherQuartzVariantExists = true;
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void disableVanillaOreGen(GenerateMinable event)
	{
		if (ConfigFile.replaceVanillaStoneGeneration && !event.getType().equals(EventType.SILVERFISH) && !event.getType().equals(EventType.QUARTZ))
		{
			event.setResult(Result.DENY);
		}
		
		if (netherQuartzVariantExists && event.getType().equals(EventType.QUARTZ))
		{
			event.setResult(Result.DENY);
		}
	}	
}