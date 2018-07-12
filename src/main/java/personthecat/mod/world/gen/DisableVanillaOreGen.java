package personthecat.mod.world.gen;

import net.minecraft.init.Blocks;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.fml.common.Loader;
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
		for (BlockOresBase ore : BlockInit.BLOCKS)
		{
			if (ore.getBackgroundBlockState().getBlock().equals(Blocks.NETHERRACK) &&
				ore.getProperties().equals(OreProperties.propertiesOf("quartz_ore")))
			{
				netherQuartzVariantExists = true;
			}
		}
	}
	
	@SubscribeEvent
	public static void disableVanillaOreGen(GenerateMinable event)
	{
		switch (event.getType())
		{
			case SILVERFISH: /*Do nothing*/
				break;
			case QUARTZ: if (netherQuartzVariantExists) event.setResult(Result.DENY);
				break;
			case CUSTOM: if (!Loader.isModLoaded("undergroundbiomes")) event.setResult(Result.DENY);
				break;
			default: if (ConfigFile.replaceVanillaStoneGeneration) event.setResult(Result.DENY);
		}
	}	
}