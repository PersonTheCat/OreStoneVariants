package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.recipes.FurnaceRecipes;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.RecipeManager;
import org.hjson.JsonObject;

import java.io.File;
import java.util.Optional;

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
    public final Lazy<Item> result;
    public final int time;
    public final float xp;

    /**
     * Primary constructor. Assumes that the resulting item may not have
     * been registered upon creation.
     */
    public RecipeProperties(String result, int time, float xp) {
        this.result = new Lazy<>(() -> getItem(result)
            .orElseThrow(() -> runExF("'result: {}' produced no item.", result)));
        this.time = time;
        this.xp = xp;
    }

    /**
     * Variant of RecipeProperties#new in which the item is known up front.
     */
    public RecipeProperties(Item result, int time, float xp) {
        this.result = new Lazy<>(result);
        this.time = time;
        this.xp = xp;
    }

    /** Generates a stand-in holder from the input json object. */
    public RecipeProperties(JsonObject json) {
        this(
            getStringOr(json, "result", "air"),
            getIntOr(json, "time", 1),
            getFloatOr(json, "xp", 0.5f)
        );
    }

    /**
     * Generates a RecipeProperties holder from the matching FurnaceRecipe,
     * overriding with values from the respective mod json.
     */
    public static RecipeProperties create(String path, RecipeManager registry, ItemStack from, boolean testForOverrides) {
        AbstractCookingRecipe recipe = FurnaceRecipes.byInput(registry, from.getItem())
            .orElseThrow(() -> runExF("No recipe found for {}. Cannot generate properties.", from));
        ItemStack resultStack = recipe.getRecipeOutput();
        Item result = resultStack.getItem();
        int time = resultStack.getBurnTime();
        float xp = recipe.getExperience();

        if (testForOverrides) {
            // Overrides can no longer be handled this way. Redo.
            Optional<JsonObject> obj = readJson(new File(path)); // Dummy
            if (obj.isPresent() && obj.get().has("recipe")) {
                JsonObject props = obj.get().get("recipe").asObject();
                result = getItemOr(props, "result", result);
                time = getIntOr(props, "time", time);
                xp = getFloatOr(props, "xp", xp);
            }
        }
        return new RecipeProperties(result, time, xp);
    }
}