package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.CommonMethods;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.Set;
import java.util.stream.Collectors;

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
    public final ItemStack input;
    public final ItemStack result;
    public final int time;
    public final float xp;

    /** Variant of RecipeProperties#new in which the item is known up front. */
    public RecipeProperties(ItemStack input, ItemStack result, int time, float xp) {
        this.input = input;
        this.result = result;
        this.time = time;
        this.xp = xp;
    }

//    /** Generates a new furnace recipe, adjusted for item type. */
//    public RecipeProperties forInput(Item item) {
//        final ResourceLocation id = item.getRegistryName();
//        final Ingredient ingredient = Ingredient.fromItems(item);
//        // Todo: Dense variants
//        final int multiple = false ? Cfg.DenseCat.smeltMultiplier : 1;
//        final float xp = (float) multiple * this.xp;
//        result.setCount(multiple * result.getCount());
//        return new RecipeProperties(ingredient, result, time, xp);
//    }

    public Item getInputItem() {
        return input.getItem();
    }

    /** Generates recipes for all OreProperties. */
    public static Set<RecipeProperties> setupRecipes() {
        return Main.ORE_PROPERTIES.stream()
            .map(RecipeProperties::create)
            .collect(Collectors.toSet());
    }

    /**
     * Generates a RecipeProperties holder from the matching FurnaceRecipe,
     * overriding with values from the respective mod json..
     */
    private static RecipeProperties create(OreProperties props) {
        final IBlockState fromState = props.ore.get();
        final Block fromBlock = fromState.getBlock();
        final int meta = fromBlock.getMetaFromState(fromState);

        final ItemStack fromStack = new ItemStack(fromBlock, 1, meta);
        final FurnaceRecipes recipes = FurnaceRecipes.instance();
        final ItemStack resultStack = recipes.getSmeltingResult(fromStack);
        final ItemStack result = getStackOr(props.recipe, "result", resultStack);

        int time = getIntOr(props.recipe, "time", result.getItem().getItemBurnTime(resultStack));
        float xp = getFloatOr(props.recipe, "xp", recipes.getSmeltingExperience(resultStack));
        time = time < 0 ? 200 : time;

        return new RecipeProperties(fromStack, result, time, xp);
    }
}