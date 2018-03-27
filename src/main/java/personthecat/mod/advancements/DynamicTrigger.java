package personthecat.mod.advancements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import personthecat.mod.config.ConfigFile;

public class DynamicTrigger
{	
	private static boolean stop = false;
	
	//Currently unused. 
	//public static final DynamicInventoryChangeTrigger INVENTORY_CHANGED_MOD = new DynamicInventoryChangeTrigger();
	//public static final DynamicInventoryChangeTrigger[] TRIGGER_ARRAY = new DynamicInventoryChangeTrigger[] 
		{
			//INVENTORY_CHANGED_MOD	
		};

	public static final List<ResourceLocation> ADVANCEMENTS = new ArrayList<ResourceLocation>();
	public static final Map<String, Advancement> ADVANCEMENT_MAP = new HashMap<String, Advancement>();
	
	public static void loadAdvancementList()
	{
		ADVANCEMENTS.add(new ResourceLocation("simpleores:main/adamantium_ore"));
		ADVANCEMENTS.add(new ResourceLocation("simpleores:main/copper_ore"));
		ADVANCEMENTS.add(new ResourceLocation("simpleores:main/mythril_ore"));
		ADVANCEMENTS.add(new ResourceLocation("simpleores:main/tin_ore"));
	}
	
	@SubscribeEvent
	public static void onClientConnectedToServerEvent(ClientConnectedToServerEvent event)
	{
		stop = true;
	}
	
	@SideOnly(value = Side.CLIENT)
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event)
	{
		stop = false;
	}
	
	@SideOnly(value = Side.SERVER)
	@SubscribeEvent
	public static void onWorldEventServer(WorldEvent.Load event)
	{		
		loadAdvancementList();
		
		for (ResourceLocation location : ADVANCEMENTS)
		{			
			Advancement advancement = event.getWorld().getMinecraftServer().getAdvancementManager().getAdvancement(location);
			ADVANCEMENT_MAP.put(location.getResourcePath().replaceAll("main/", "simpleores_"), advancement);
		}
	}
	
	@SideOnly(value = Side.CLIENT)
	@SubscribeEvent
	public static void onWorldEventClient(WorldEvent.Load event)
	{
		if (!stop && ConfigFile.enableAdvancements)
		{
			loadAdvancementList();
			
			for (ResourceLocation location : ADVANCEMENTS)
			{			
				Advancement advancement = event.getWorld().getMinecraftServer().getAdvancementManager().getAdvancement(location);
				ADVANCEMENT_MAP.put(location.getResourcePath().replaceAll("main/", "simpleores_"), advancement);
			}
		}
	}
}
