package com.personthecat.orestonevariants.recipes;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.RecipeProperties;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.find;
import static com.personthecat.orestonevariants.util.CommonMethods.info;

public class RecipeHelper {

    /** Necessary for modifying the values held by RecipeManager. */
    private static final Field RECIPES = ReflectionTools.getField(RecipeManager.class, "recipes", 2);

    /** Handles all recipe and tag configurations for OSV blocks. */
    public static void handleRecipes(RecipeManager registry) {
        if (Cfg.furnaceRecipes.get()) {
            registerRecipes(registry);
        }
    }

    /** Generates all RecipeHelper for this mod and registers them into RecipeManager. */
    private static void registerRecipes(RecipeManager registry) {
        final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = mutableCopyOf(getRecipes(registry));
        final Map<ResourceLocation, IRecipe<?>> craftingRecipes = recipes.get(IRecipeType.SMELTING);
        final Map<ResourceLocation, IRecipe<?>> blastingRecipes = recipes.get(IRecipeType.BLASTING);

        for (RecipeProperties.Checked recipe : RecipeProperties.setupRecipes(registry)) {
            getBlocksForRecipe(recipe).flatMap(RecipeHelper::getItemsForBlock).forEach(item -> {
                // Todo: catch and rethrow any NPEs to clarify issues to the end user.
                register(craftingRecipes, recipe.forInput(item, false));
                register(blastingRecipes, recipe.forInput(item, true));
            });
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
        final Map<K1, Map<K2, V>> parentCopy = Collections.synchronizedMap(new HashMap<>());
        for (Map.Entry<K1, Map<K2, V>> parentEntry : map.entrySet()) {
            final Map<K2, V> childCopy = Collections.synchronizedMap(new HashMap<>(parentEntry.getValue()));
            parentCopy.put(parentEntry.getKey(), childCopy);
        }
        return parentCopy;
    }

    /** Registers a new recipe into the given registry using its own ID. */
    private static void register(Map<ResourceLocation, IRecipe<?>> registry, FurnaceRecipe recipe) {
        registry.put(recipe.getId(), recipe);
    }

    private static Stream<BaseOreVariant> getBlocksForRecipe(RecipeProperties.Checked recipe) {
        return Main.BLOCKS.stream()
            .filter(b -> recipe.getInputItem().equals(b.getOreStack().getItem()));
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