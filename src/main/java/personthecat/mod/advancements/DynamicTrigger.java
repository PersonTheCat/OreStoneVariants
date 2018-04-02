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
import personthecat.mod.Main;
import personthecat.mod.config.ConfigFile;

public class DynamicTrigger
{		
	//Currently unused. 
	//public static final DynamicInventoryChangeTrigger INVENTORY_CHANGED_MOD = new DynamicInventoryChangeTrigger();
	//public static final DynamicInventoryChangeTrigger[] TRIGGER_ARRAY = new DynamicInventoryChangeTrigger[] 
		{
			//INVENTORY_CHANGED_MOD	
		};

	public static final Map<String, ResourceLocation> ADVANCEMENT_MAP = new HashMap<String, ResourceLocation>();
	
	//We need a void for performance reasons, apparently. 
	public static void loadAdvancementList()
	{
		if (Main.isSimpleOresLoaded) ADVANCEMENT_MAP.put("simpleores_adamantium_ore", new ResourceLocation("simpleores:main/adamantium_ore"));
		if (Main.isSimpleOresLoaded) ADVANCEMENT_MAP.put("simpleores_copper_ore", new ResourceLocation("simpleores:main/copper_ore"));
		if (Main.isSimpleOresLoaded) ADVANCEMENT_MAP.put("simpleores_mythril_ore", new ResourceLocation("simpleores:main/mythril_ore"));
		if (Main.isSimpleOresLoaded) ADVANCEMENT_MAP.put("simpleores_tin_ore", new ResourceLocation("simpleores:main/tin_ore"));
	}
}
