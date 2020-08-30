package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.item.DenseVariantItem;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.Set;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

/**
 * An object used for holding information about smelting recipes.
 * A slightly more convenient way to share smelting recipes between
 * blocks.
 *
 * To-do: implement tags instead of RecipeProperties for unmodified
 * recipes.
 */
public class RecipeProperties {
    public final Ingredient input;
    public final Item result;
    public final String group;
    public final int time;
    public final float xp;


    /** Variant of RecipeProperties#new in which the item is known up front. */
    public RecipeProperties(Ingredient input, Item result, String group, int time, float xp) {
        this.input = input;
        this.result = result;
        this.group = group;
        this.time = time;
        this.xp = xp;
    }

    /** Generates a new, standard furnace recipe for the given item. */
    public FurnaceRecipe forInput(Item item, boolean blasting) {
        final ResourceLocation id = item.getRegistryName();
        final Ingredient ingredient = Ingredient.fromItems(item);
        final int quantity = item instanceof DenseVariantItem ? Cfg.denseSmeltMultiplier.get() : 1;
        final float xp = (float) quantity * this.xp;
        final ItemStack result = new ItemStack(this.result, quantity);
        final int t = getMax(time, 1) / (blasting ? 2 : 1);
        return new FurnaceRecipe(id, group, ingredient, result, xp, t);
    }

    public Item getInputItem() {
        return input.getMatchingStacks()[0].getItem();
    }

    /** Generates recipes for all OreProperties. */
    public static Set<RecipeProperties> setupRecipes(RecipeManager registry) {
        return Main.ORE_PROPERTIES.stream()
            .map(props -> create(props, registry))
            .collect(Collectors.toSet());
    }

    /**
     * Generates a RecipeProperties holder from the matching FurnaceRecipe,
     * overriding with values from the respective mod json.
     */
    private static RecipeProperties create(OreProperties props, RecipeManager registry) {
        AbstractCookingRecipe recipe = RecipeHelper.byInput(registry, props.ore.get().getBlock().asItem())
            .orElseThrow(() -> runExF("No recipe found for {}. Cannot generate properties.", props.ore.get()));

        ItemStack resultStack = recipe.getRecipeOutput();
        Item result = getItemOr(props.recipe, "result", resultStack.getItem());
        int time = getIntOr(props.recipe, "time", resultStack.getBurnTime());
        float xp = getFloatOr(props.recipe, "xp", recipe.getExperience());
        String group = getStringOr(props.recipe, "group", recipe.getGroup());
        time = time < 0 ? 200 : time;

        return new RecipeProperties(recipe.getIngredients().get(0), result, group, time, xp);
    }
}