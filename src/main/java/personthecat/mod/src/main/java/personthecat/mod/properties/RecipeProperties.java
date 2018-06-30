package personthecat.mod.properties;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RecipeProperties
{
	private String name, result;
	private int resultMeta, quantity;
	private float xp;
	
	protected static final Map<String, RecipeProperties> RECIPE_PROPERTY_MAP = new HashMap<>();
	public static final RecipeProperties DO_NOTHING = null;
	
	public RecipeProperties(String name, String result, int resultMeta, int quantity, float xp)
	{
		this.name = name;
		this.result = result;
		this.resultMeta = resultMeta;
		this.quantity = quantity;
		this.xp = xp;
		
		RECIPE_PROPERTY_MAP.put(name, this);
	}

	public String getResultString()
	{
		return result;
	}
	
	public Item getResult()
	{
		if (ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(result)))
		{
			return Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(result))); 
		}
		
		else
		{
			return ForgeRegistries.ITEMS.getValue(new ResourceLocation(result));
		}
	}

	public int getResultMeta()
	{
		return resultMeta;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public float getXp()
	{
		return xp;
	}
}
