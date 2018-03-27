package personthecat.mod.world.gen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import personthecat.mod.config.ConfigFile;

public class DisableVanillaOreGen
{
	@SubscribeEvent
	public static void disableVanillaOreGen(GenerateMinable event)
	{
		if (ConfigFile.replaceVanillaStoneGeneration && event.getType() != EventType.SILVERFISH && event.getType() != EventType.QUARTZ)
			event.setResult(Result.DENY);		
	}	
}
