package personthecat.osv.preset.resolver;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.RecipeSettings;

public class RecipeResolver {

    @Nullable
    public static RecipeSettings.Checked resolve(final RecipeManager recipes, final OrePreset preset) {
        final SmeltingRecipe recipe = getRecipe(recipes, preset.getOriginal().getBlock().asItem());
        if (recipe == null) return null;

        final Ingredient original = recipe.getIngredients().get(0);
        final ItemStack result = recipe.getResultItem();
        final String group = recipe.getGroup();
        final int time = recipe.getCookingTime();
        final float xp = recipe.getExperience();
        return new RecipeSettings.Checked(original, result.getItem(), group, time, xp, result.getCount());
    }

    @Nullable
    private static SmeltingRecipe getRecipe(final RecipeManager recipes, final Item item) {
        for (final SmeltingRecipe recipe : recipes.getAllRecipesFor(RecipeType.SMELTING)) {
            for (final Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.test(new ItemStack(item))) {
                    return recipe;
                }
            }
        }
        return null;
    }
}
