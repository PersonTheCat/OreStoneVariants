package personthecat.osv.preset.resolver;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.preset.data.RecipeSettings;

public class RecipeResolver {

    public static RecipeSettings.Checked resolve(final RecipeManager recipes, @Nullable final Block block) {
        if (block == null) return RecipeSettings.NONE_CHECKED;

        final SmeltingRecipe recipe = getRecipe(recipes, block.asItem());
        if (recipe == null) return RecipeSettings.NONE_CHECKED;

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
