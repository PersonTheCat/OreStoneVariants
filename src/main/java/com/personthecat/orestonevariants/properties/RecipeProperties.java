package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.item.DenseVariantItem;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import org.hjson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    @Nullable public final Ingredient input;
    @Nullable public final Lazy<Item> result;
    @Nullable public final String group;
    @Nullable public final Integer time;
    @Nullable public final Float xp;

    /** Variant of RecipeProperties#new in which the item is known up front. */
    public RecipeProperties(
        @Nullable Ingredient input,
        @Nullable Lazy<Item> result,
        @Nullable String group,
        @Nullable Integer time,
        @Nullable Float xp
    ) {
        this.input = input;
        this.result = result;
        this.group = group;
        this.time = time;
        this.xp = xp;
    }

    public RecipeProperties(JsonObject json) {
        this.input = null; // Manual ingredient overrides not supported.
        this.result = new Lazy<>(getItemOr(json, "result", null));
        this.group = getStringOr(json, "group", null);
        this.time = getIntOr(json, "time", null);
        this.xp = getFloatOr(json, "xp", null);
    }

    public static RecipeProperties from(JsonObject json) {
        return new RecipeProperties(json);
    }

    /**
     * Generates a new, standard furnace recipe for the given item.
     *
     * @throws NullPointerException if this object is used without
     * using {@link #create};
     */
    @SuppressWarnings("ConstantConditions")
    public FurnaceRecipe forInput(Item item, boolean blasting) {
        final ResourceLocation itemRegistry = nullable(item.getRegistryName())
            .orElseThrow(() -> runEx("Attempted to generate FurnaceRecipe for unregistered item."));
        final String path = f("{}_{}", itemRegistry.getPath(), blasting ? "blasting" : "furnace");
        final ResourceLocation id = new ResourceLocation(itemRegistry.getNamespace(), path);
        final Ingredient ingredient = Ingredient.fromItems(item);
        final int quantity = item instanceof DenseVariantItem ? Cfg.denseSmeltMultiplier.get() : 1;
        final float xp = (float) quantity * this.xp;
        final ItemStack result = new ItemStack(this.result.get(), quantity);
        final int t = getMax(time, 1) / (blasting ? 2 : 1);
        return new FurnaceRecipe(id, group, ingredient, result, xp, t);
    }

    public Item getInputItem() {
        return input.getMatchingStacks()[0].getItem();
    }

    /** Generates recipes for all OreProperties. */
    public static Set<RecipeProperties> setupRecipes(RecipeManager registry) {
        final Set<RecipeProperties> recipes = new HashSet<>();
        for (OreProperties props : Main.ORE_PROPERTIES) {
            create(props, registry).ifPresent(recipes::add);
        }
        return recipes;
    }

    /**
     * Generates a RecipeProperties holder from the matching FurnaceRecipe,
     * overriding with values from the respective mod json.
     *
     * Todo: break this up into smaller pieces.
     */
    private static Optional<RecipeProperties> create(OreProperties props, RecipeManager registry) {
        final Item oreItem =  props.ore.get().getBlock().asItem();
        final RecipeProperties template = props.recipe;
        final Optional<AbstractCookingRecipe> found = RecipeHelper.byInput(registry, oreItem);
        if (!found.isPresent()) {
            if (template.result == null) {
                return empty();
            }
            final Ingredient input = Ingredient.fromStacks(new ItemStack(props.ore.get().getBlock()));
            return full(new RecipeProperties(input, template.result, template.group, template.time, template.xp));
        }
        final AbstractCookingRecipe recipe = found.get();
        final Ingredient ingredient = recipe.getIngredients().get(0);
        final ItemStack resultStack = recipe.getRecipeOutput();
        final Item result = nullable(template.result)
            .map(Lazy::get)
            .orElse(resultStack.getItem());
        int time = nullable(template.time).orElse(resultStack.getBurnTime());
        float xp = nullable(template.xp).orElse(recipe.getExperience());
        final String group = nullable(template.group).orElse(recipe.getGroup());
        time = time < 0 ? 200 : time;

        return full(new RecipeProperties(ingredient, new Lazy<>(result), group, time, xp));
    }
}