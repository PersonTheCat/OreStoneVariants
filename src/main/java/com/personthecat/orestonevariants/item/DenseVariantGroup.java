package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.init.LazyRegistries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import static com.personthecat.orestonevariants.util.CommonMethods.find;

public class DenseVariantGroup extends ItemGroup {
    public static DenseVariantGroup GROUP = new DenseVariantGroup();

    private DenseVariantGroup() {
        super("dense_variants");
    }

    @Override
    public ItemStack createIcon() {
        final Item item = find(LazyRegistries.ITEMS, i -> i.getRegistryName().getPath().equals("dense_diamond_ore_granite"))
            .orElseGet(DenseVariantGroup::firstDense);
        return new ItemStack(item);
    }

    private static VariantItem firstDense() {
        for (VariantItem item : LazyRegistries.ITEMS) {
            if (item instanceof DenseVariantItem) {
                return item;
            }
        }
        return LazyRegistries.ITEMS.iterator().next();
    }
}