package personthecat.mod.properties;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import personthecat.mod.config.JsonReader;

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

        register();
    }

    public static void createAllRecipeProperties()
    {
        for (OreProperties props : OreProperties.getOrePropertyRegistry())
        {
            if (props.inUse())
            {
                props.createRecipeProperties(!(props instanceof OrePropertiesDynamic));
            }
        }
    }

    public static RecipeProperties createRecipeProperties(String name, ItemStack fromStack)
    {
        return createRecipeProperties(name, fromStack, false);
    }

    public static RecipeProperties createRecipeProperties(String name, ItemStack fromStack, boolean testForOverrides)
    {
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(fromStack);

        String resultName = result.getItem().getRegistryName().toString();
        int resultMeta = result.getItemDamage();
        int quantity = result.getCount();
        float xp = FurnaceRecipes.instance().getSmeltingExperience(result);

        if (testForOverrides)
        {
            JsonObject obj = JsonReader.getProperties(name, "RecipeProperties.json");

            if (obj != null)
            {
                resultName = obj.has("result") ? obj.get("result").getAsString() : resultName;
                resultMeta = obj.has("resultMeta") ? obj.get("resultMeta").getAsInt() : resultMeta;
                quantity = obj.has("quantity") ? obj.get("quantity").getAsInt() : quantity;
                xp = obj.has("xp") ? obj.get("xp").getAsFloat() : xp;
            }
        }

        return new RecipeProperties(name, resultName, resultMeta, quantity, xp);
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
        else return ForgeRegistries.ITEMS.getValue(new ResourceLocation(result));
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

    public void register()
    {
        RECIPE_PROPERTY_MAP.put(name, this);
    }
}
