package personthecat.mod.advancements;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import personthecat.mod.Main;

public class DynamicTrigger
{		
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
