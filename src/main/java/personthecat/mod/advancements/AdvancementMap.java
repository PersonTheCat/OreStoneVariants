package personthecat.mod.advancements;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import personthecat.mod.Main;
import personthecat.mod.util.NameReader;

public class AdvancementMap
{		
	public static final Map<String, ResourceLocation> ADVANCEMENT_MAP = new HashMap<String, ResourceLocation>();
	
	//We need a void for performance reasons, apparently. 
	public static void loadAdvancementList()
	{
		//Why test four times?
		if (Main.isSimpleOresLoaded())
		{
			ADVANCEMENT_MAP.put("simpleores_adamantium_ore", new ResourceLocation("simpleores:main/adamantium_ore"));
			ADVANCEMENT_MAP.put("simpleores_copper_ore", new ResourceLocation("simpleores:main/copper_ore"));
			ADVANCEMENT_MAP.put("simpleores_mythril_ore", new ResourceLocation("simpleores:main/mythril_ore"));
			ADVANCEMENT_MAP.put("simpleores_tin_ore", new ResourceLocation("simpleores:main/tin_ore"));
		}
	}
	
	public static Advancement getAdvancementFromMap(String oreName, World worldIn)
	{
		return getAdvancement(ADVANCEMENT_MAP.get(NameReader.getOre(oreName)), worldIn);
	}
	
	public static Advancement getAdvancement(ResourceLocation location, World worldIn)
	{
		Advancement advancement = null;
		
		try
		{
			advancement = worldIn.getMinecraftServer().getAdvancementManager().getAdvancement(location);
		}
		
		catch (NullPointerException ignored) {}
		
		return advancement;
	}
	
	public static void grantAdvancement(Advancement advancement, Entity entityIn)
	{
		if (advancement != null && entityIn instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP) entityIn;
			
			if (playerHasAdvancement(advancement, player))
			{
				for (String criteria : player.getAdvancements().getProgress(advancement).getRemaningCriteria())
				{
					player.getAdvancements().grantCriterion(advancement, criteria);
				}
			}
		}
	}
	
	public static boolean playerHasAdvancement(Advancement advancement, EntityPlayerMP player)
	{
		return player.getAdvancements().getProgress(advancement).isDone();
	}
}
