package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.item.DenseVariantItem;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.Lazy;
import lombok.AllArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import org.hjson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.nullable;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.HjsonTools.getFloatOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getIntOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getItemOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getStringOr;

/**
 * An object used for holding information about smelting recipes. A slightly more convenient
 * way to share smelting recipes between blocks.
 */
public class RecipeProperties {

    /**
     * Generates a new set of recipe properties from JSON.
     *
     * @param json A regular JSON containing OSV recipe data.
     * @return An unchecked, nullable set of recipe data.
     */
    public static Unchecked from(JsonObject json) {
        return new Unchecked(json);
    }

    /**
     * Generates recipes for all OreProperties.
     *
     * @param registry The registry containing all known furnace recipes.
     * @return A set of data for every validated recipe currently enabled.
     */
    public static Set<Checked> setupRecipes(RecipeManager registry) {
        final Set<Checked> recipes = new HashSet<>();
        for (OreProperties props : LazyRegistries.ORE_PROPERTIES) {
            create(props.recipe, props.ore.get().getBlock(), registry).ifPresent(recipes::add);
        }
        return recipes;
    }

    /**
     * Generates a RecipeProperties holder from the matching FurnaceRecipe, overriding with
     * values from the respective mod json.
     *
     * @param template A set of data containing overrides for this recipe.
     * @param ore The original ore block which this recipe is derived from.
     * @param registry The registry containing all known furnace recipes.
     * @return A validated recipe, if possible.
     */
    private static Optional<Checked> create(Unchecked template, Block ore, RecipeManager registry) {
        return RecipeHelper.byInput(registry, ore.asItem())
            .map(abstractCookingRecipe -> full(fromRecipe(template, abstractCookingRecipe)))
            .orElseGet(() -> fromTemplate(template, ore));
    }

    /**
     * Will produce a new set of checked properties from a template, if possible.
     *
     * @param template The complete set of data for this template.
     * @param ore The original ore block which this recipe is made for.
     * @return A validated recipe, if possible.
     */
    private static Optional<Checked> fromTemplate(Unchecked template, Block ore) {
        if (template.result == null || template.result.get() == null) {
            return empty();
        }
        final Ingredient input = Ingredient.fromStacks(new ItemStack(ore));
        final int time = nullable(template.time).orElse(200);
        final float xp = nullable(template.xp).orElse(0f);

        return full(new Checked(input, template.result.get(), template.group, time, xp));
    }

    /**
     * Guaranteed to produce a new set of properties from a known, existing recipe.
     *
     * @param template A set of data containing overrides for this recipe.
     * @param recipe The original recipe which the output is derived from.
     * @return A validated recipe containing properties from the original with overrides.
     */
    private static Checked fromRecipe(Unchecked template, AbstractCookingRecipe recipe) {
        final Ingredient ingredient = recipe.getIngredients().get(0);
        final ItemStack resultStack = recipe.getRecipeOutput();
        final Item result = nullable(template.result)
            .map(Lazy::get)
            .orElse(resultStack.getItem());
        int time = nullable(template.time).orElse(resultStack.getBurnTime());
        float xp = nullable(template.xp).orElse(recipe.getExperience());
        final String group = nullable(template.group).orElse(recipe.getGroup());
        time = time < 0 ? 200 : time;

        return new Checked(ingredient, result, group, time, xp);
    }

    /**
     * Contains a set of optional instructions for how a furnace recipe should be created.
     * These properties must be checked at a later point in order to be used.
     */
    public static class Unchecked {
        @Nullable public final Lazy<Item> result;
        @Nullable public final String group;
        @Nullable public final Integer time;
        @Nullable public final Float xp;

        private Unchecked(JsonObject json) {
            this.result = new Lazy<>(() -> getItemOr(json, "result", null));
            this.group = getStringOr(json, "group", null);
            this.time = getIntOr(json, "time", null);
            this.xp = getFloatOr(json, "xp", null);
        }
    }

    /**
     * Contains a set of instructions for how to construct a new furnace recipe. These
     * data are guaranteed to be non-null and are substituted with default values or
     * values from some other known recipe before this point.
     */
    @AllArgsConstructor
    public static class Checked {
        public final Ingredient input;
        public final Item result;
        public final String group;
        public final int time;
        public final float xp;

        public Item getInputItem() {
            return input.getMatchingStacks()[0].getItem();
        }

        /**
         * Generates a new, standard furnace recipe for the given item.
         *
         * @param item The item which yields this recipe.
         * @param blasting Whether to create a blasting recipe instead.
         * @return A standard, vanilla furnace or blasting recipe.
         */
        public AbstractCookingRecipe fromItem(Item item, boolean blasting) {
            final ResourceLocation itemRegistry = nullable(item.getRegistryName())
                .orElseThrow(() -> runEx("Attempted to generate FurnaceRecipe for unregistered item."));
            final String path = f("{}_{}", itemRegistry.getPath(), blasting ? "blasting" : "furnace");
            final ResourceLocation id = new ResourceLocation(itemRegistry.getNamespace(), path);
            final Ingredient ingredient = Ingredient.fromItems(item);
            final int quantity = item instanceof DenseVariantItem ? Cfg.denseSmeltMultiplier.get() : 1;
            final float xp = (float) quantity * this.xp;
            final ItemStack result = new ItemStack(this.result, quantity);
            final int t = Math.max(time, 1) / (blasting ? 2 : 1);

            return blasting ? new BlastingRecipe(id, group, ingredient, result, xp, t)
                : new FurnaceRecipe(id, group, ingredient, result, xp, t);
        }
    }
}