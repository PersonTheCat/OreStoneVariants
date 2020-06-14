package com.personthecat.orestonevariants.recipes;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.RecipeProperties;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class RecipeHelper {

    /** Used internally by Forge to map ItemStacks to their ore dict IDs. */
    public static final Map<Integer, List<Integer>> stackToId = ReflectionTools
        .getValue(OreDictionary.class, "stackToId", "",6, null);
    /** Used internally by Forge to map item hashes to their ore dict names. */
    public static final List<String> idToName = ReflectionTools
        .getValue(OreDictionary.class, "idToName", "",2, null);

    /** Handles all recipe and tag configurations for OSV blocks. */
    public static void handleRecipes() {
        if (Cfg.BlocksCat.enableFurnaceRecipes) {
            registerRecipes();
        }
        if (Cfg.BlocksCat.enableOreDictionary) {
            registerOreDict();
        }
    }

    /** Generates all RecipeHelper for this mod and registers them into RecipeManager. */
    private static void registerRecipes() {
        for (RecipeProperties recipe : RecipeProperties.setupRecipes()) {
            // Todo: readability
            getBlocksForRecipe(recipe).forEach(block -> {
                // Do normal recipes.
                final ItemStack stack = block.getStack();
                GameRegistry.addSmelting(stack, recipe.result, recipe.xp);
                // Do dense recipes.
                final IBlockState denseState = block.getDefaultState()
                    .withProperty(BaseOreVariant.DENSE, true);
                final int multiple = Cfg.DenseCat.smeltMultiplier;
                final ItemStack denseStack = block.getStack(denseState);
                final ItemStack denseResult = recipe.result.copy();
                denseResult.setCount(multiple);
                GameRegistry.addSmelting(denseStack, denseResult, recipe.xp * multiple);
            });
        }
        info("Successfully setup all recipes with GameRegistry!");
    }

    /** Generates and registers all ore dictionary values. */
    private static void registerOreDict() {
        // Todo: Iterate over OreProperties instead?
        for (BaseOreVariant ore : Main.BLOCKS) {
            final IBlockState bgBlock = ore.properties.ore.get();
            final List<String> names = getRegisteredNames(toStack(bgBlock));
            debugNames(bgBlock, names);
            for (String name : names) {
                final ItemStack stack = ore.getStack();
                OreDictionary.registerOre(name, stack);
                final IBlockState denseState = ore.getDefaultState()
                    .withProperty(BaseOreVariant.DENSE, true);
                final ItemStack denseStack = ore.getStack(denseState);
                OreDictionary.registerOre("dense" + ore, denseStack);
                OreDictionary.registerOre(ore + "Dense", denseStack);
            }
        }
    }

    private static void debugNames(IBlockState bgBlock, List<String> names) {
        info("Found the following names registered to {}:", bgBlock);
        for (String name : names) {
            info(" * {}", name);
        }
    }

    /** Retrieves all of the ore dict names registered to the input stack via reflection. */
    private static List<String> getRegisteredNames(ItemStack ore) {
        final List<String> names = list();
        final int hash = getItemHash(ore);
        final List<Integer> ids = stackToId.get(hash);
        for (Integer i : ids) {
            names.add(idToName.get(i));
        }
        return names;
    }

    /** Generates a unique hash for the input item, copying Forge's convention. */
    private static int getItemHash(ItemStack ore) {
        final IRegistryDelegate<Item> delegate = ore.getItem().delegate;
        int hash = Item.REGISTRY.getIDForObject(delegate.get());
        if (ore.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            hash |= ((ore.getItemDamage() + 1) << 16);
        }
        return hash;
    }

    /** Returns all of the  */
    private static Stream<BaseOreVariant> getBlocksForRecipe(RecipeProperties recipe) {
        return Main.BLOCKS.stream()
            .filter(b -> recipe.getInputItem().equals(b.getBackgroundStack().getItem()));
    }

    /** Iterates through each cooking recipe . */
    public static void forEachRecipe(BiConsumer<ItemStack, ItemStack> fun) {
        FurnaceRecipes.instance().getSmeltingList().forEach(fun);
    }

    /** Neatly determines the direct result of smelting the input item. */
    public static Optional<ItemStack> resultOf(ItemStack item) {
        final FurnaceRecipes instance = FurnaceRecipes.instance();
        return nullable(instance.getSmeltingResult(item));
    }

    /** Convenience variant of #resultOf. */
    public static Optional<ItemStack> resultOf(Item item) {
        return resultOf(new ItemStack(item));
    }

    public static float getExperience(ItemStack result) {
        return FurnaceRecipes.instance().getSmeltingExperience(result);
    }
}