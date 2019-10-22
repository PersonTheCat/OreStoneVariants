package com.personthecat.orestonevariants.recipes;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.properties.RecipeProperties;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import com.personthecat.orestonevariants.util.unsafe.Result;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class FurnaceRecipes {
    /** Necessary for modifying the values held by RecipeManager. */
    private static final Field RECIPES = ReflectionTools.getField(RecipeManager.class, "recipes");

    /** Handles all recipe and tag configurations for OSV blocks. */
    public static void handleRecipes(RecipeManager registry) {
        final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = mutableCopyOf(getRecipes(registry));
        final Map<ResourceLocation, IRecipe<?>> craftingRecipes = recipes.get(IRecipeType.SMELTING);

        for (RecipeProperties recipe : RecipeProperties.setupRecipes(registry)) {
            getBlocksForRecipe(recipe).flatMap(FurnaceRecipes::getItemsForBlock)
                .forEach(item -> craftingRecipes.put(item.getRegistryName(), recipe.forInput(item)));
        }
        ReflectionTools.setValue(RECIPES, registry, recipes);
        info("Successfully replaced all recipes in RecipeManager!");
    }

    /** Retrieves the original registry of recipes from RecipeManager. */
    private static Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> getRecipes(RecipeManager registry) {
        return ReflectionTools.getValue(RECIPES, registry);
    }

    /** Converts an (immutable) map into a standard HashMap. */
    private static <K1, K2, V> Map<K1, Map<K2, V>> mutableCopyOf(Map<K1, Map<K2, V>> map) {
        final Map<K1, Map<K2, V>> parentCopy = new HashMap<>();
        for (Map.Entry<K1, Map<K2, V>> parentEntry : map.entrySet()) {
            final Map<K2, V> childCopy = new HashMap<>();
            for (Map.Entry<K2, V> childEntry : parentEntry.getValue().entrySet()) {
                childCopy.put(childEntry.getKey(), childEntry.getValue());
            }
            parentCopy.put(parentEntry.getKey(), childCopy);
        }
        return parentCopy;
    }

    private static Stream<BaseOreVariant> getBlocksForRecipe(RecipeProperties recipe) {
        return Main.BLOCKS.stream()
            .filter(b -> recipe.getInputItem().equals(b.getBackgroundStack().getItem()));
    }

    private static Stream<Item> getItemsForBlock(BaseOreVariant block) {
        return Stream.of(block.normalItem.get(), block.denseItem.get());
    }

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