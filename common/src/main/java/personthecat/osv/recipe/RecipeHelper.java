package personthecat.osv.recipe;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import personthecat.osv.ModRegistries;
import personthecat.osv.config.Cfg;
import personthecat.osv.mixin.RecipeManagerAccessor;
import personthecat.osv.preset.data.RecipeSettings;

import java.util.*;

@Log4j2
public class RecipeHelper {

    public static void injectRecipes(final RecipeManager registry) {
        if (Cfg.furnaceRecipes()) {
            log.info("Injecting furnace recipes for {} items.", ModRegistries.ITEMS.size());
            injectAll(registry);
        }
    }

    private static void injectAll(final RecipeManager registry) {
        final RecipeManagerAccessor accessor = (RecipeManagerAccessor) registry;
        final Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = mutableCopyOf(accessor.getRecipes());
        final Map<ResourceLocation, Recipe<?>> smelting = recipes.get(RecipeType.SMELTING);
        final Map<ResourceLocation, Recipe<?>> blasting = recipes.get(RecipeType.BLASTING);

        ModRegistries.ITEMS.forEach((id, variant) -> {
            final RecipeSettings.Checked recipe = variant.getPreset().getCheckedRecipe(registry);
            if (!recipe.isNone()) {
                register(smelting, recipe.getRecipe(id, variant, false));
                register(blasting, recipe.getRecipe(id, variant, true));
            }
        });
        accessor.setRecipes(immutableCopyOf(recipes));
    }

    private static <K1, K2, V> Map<K1, Map<K2, V>> mutableCopyOf(final Map<K1, Map<K2, V>> map) {
        final Map<K1, Map<K2, V>> parentCopy = new HashMap<>();
        for (final Map.Entry<K1, Map<K2, V>> parentEntry : map.entrySet()) {
            final Map<K2, V> childCopy = new HashMap<>(parentEntry.getValue());
            parentCopy.put(parentEntry.getKey(), childCopy);
        }
        return parentCopy;
    }

    private static void register(final Map<ResourceLocation, Recipe<?>> registry, final Recipe<?> recipe) {
        registry.put(recipe.getId(), recipe);
    }

    private static <K1, K2, V> Map<K1, Map<K2, V>> immutableCopyOf(final Map<K1, Map<K2, V>> map) {
        final ImmutableMap.Builder<K1, Map<K2, V>> parentCopy = ImmutableMap.builder();
        for (final Map.Entry<K1, Map<K2, V>> parentEntry : map.entrySet()) {
            final Map<K2, V> childCopy = ImmutableMap.copyOf(parentEntry.getValue());
            parentCopy.put(parentEntry.getKey(), childCopy);
        }
        return parentCopy.build();
    }
}
