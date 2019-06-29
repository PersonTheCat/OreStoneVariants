package com.personthecat.orestonevariants.recipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import java.util.function.Consumer;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class FurnaceRecipes {

    /** Iterates through each cooking recipe contained within the input registry. */
    public static void forEachRecipe(RecipeManager registry, Consumer<AbstractCookingRecipe> fun) {
        for (IRecipe<?> recipe : registry.getRecipes()) {
            if (recipe instanceof AbstractCookingRecipe) {
                fun.accept((AbstractCookingRecipe) recipe);
            }
        }
    }

    /** Variant of RecipeManager#func_215367_a(mapped?) which is specific to furnace recipes. */
    public static Optional<AbstractCookingRecipe> byOutput(RecipeManager registry, ResourceLocation location) {
        return find(registry.getRecipes(), recipe ->
            recipe instanceof AbstractCookingRecipe && recipe.getId().equals(location)
        ).map(recipe -> (AbstractCookingRecipe) recipe);
    }

    /** Variant of RecipeManager#func_215371_a(mapped?) which does not require a World object. */
    public static Optional<AbstractCookingRecipe> byInput(RecipeManager registry, Item item) {
        return find(registry.getRecipes(), recipe -> {
            if (recipe instanceof AbstractCookingRecipe) {
                for (Ingredient i : recipe.getIngredients()) {
                    if (i.test(new ItemStack(item))) {
                        return true;
                    }
                }
            }
            return false;
        }).map(recipe -> (AbstractCookingRecipe) recipe);
    }

    /** Neatly determines the direct result of smelting the input item. */
    public static Optional<ItemStack> resultOf(RecipeManager registry, Item item) {
        return byInput(registry, item).map(AbstractCookingRecipe::getRecipeOutput);
    }
}