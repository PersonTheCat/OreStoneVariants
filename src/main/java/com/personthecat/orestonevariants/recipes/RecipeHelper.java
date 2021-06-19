package com.personthecat.orestonevariants.recipes;

import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.properties.RecipeProperties;
import lombok.extern.log4j.Log4j2;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.find;

@Log4j2
public class RecipeHelper {

    /** Handles all recipe and tag configurations for OSV blocks. */
    public static void handleRecipes(RecipeManager registry) {
        if (Cfg.furnaceRecipes.get()) {
            log.info("Loading furnace recipes.");
            registerRecipes(registry);
        }
    }

    /** Generates all RecipeHelper for this mod and registers them into RecipeManager. */
    private static void registerRecipes(RecipeManager registry) {
        final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = mutableCopyOf(registry.recipes);
        final Map<ResourceLocation, IRecipe<?>> craftingRecipes = recipes.get(IRecipeType.SMELTING);
        final Map<ResourceLocation, IRecipe<?>> blastingRecipes = recipes.get(IRecipeType.BLASTING);

        for (RecipeProperties.Checked recipe : RecipeProperties.setupRecipes(registry)) {
            getBlocksForRecipe(recipe).flatMap(RecipeHelper::getItemsForBlock).forEach(item -> {
                register(craftingRecipes, recipe.fromItem(item, false));
                register(blastingRecipes, recipe.fromItem(item, true));
            });
        }
        registry.recipes = recipes;
        log.info("Successfully replaced all recipes in RecipeManager!");
    }

    /** Converts an (immutable) map into a standard HashMap. */
    private static <K1, K2, V> Map<K1, Map<K2, V>> mutableCopyOf(Map<K1, Map<K2, V>> map) {
        final Map<K1, Map<K2, V>> parentCopy = Collections.synchronizedMap(new HashMap<>());
        for (Map.Entry<K1, Map<K2, V>> parentEntry : map.entrySet()) {
            final Map<K2, V> childCopy = Collections.synchronizedMap(new HashMap<>(parentEntry.getValue()));
            parentCopy.put(parentEntry.getKey(), childCopy);
        }
        return parentCopy;
    }

    /** Registers a new recipe into the given registry using its own ID. */
    private static void register(Map<ResourceLocation, IRecipe<?>> registry, AbstractCookingRecipe recipe) {
        registry.put(recipe.getId(), recipe);
    }

    private static Stream<OreVariant> getBlocksForRecipe(RecipeProperties.Checked recipe) {
        return LazyRegistries.BLOCKS.values().stream()
            .filter(b -> recipe.getInputItem().equals(b.getOreStack().getItem()));
    }

    private static Stream<Item> getItemsForBlock(OreVariant block) {
        return Stream.of(block.normalItem.get(), block.denseItem.get());
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
}