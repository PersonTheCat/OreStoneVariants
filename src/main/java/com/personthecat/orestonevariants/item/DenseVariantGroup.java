package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.Main;
import net.minecraft.client.renderer.model.Variant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class DenseVariantGroup extends ItemGroup {
    public static DenseVariantGroup GROUP = new DenseVariantGroup();

    private DenseVariantGroup() {
        super("dense_variants");
    }

    @Override
    public ItemStack createIcon() {
        final Item item = find(Main.ITEMS, i -> i.getRegistryName().getPath().equals("dense_diamond_ore_granite"))
            .orElseGet(DenseVariantGroup::firstDense);
        return new ItemStack(item);
    }

    private static VariantItem firstDense() {
        for (VariantItem item : Main.ITEMS) {
            if (item instanceof DenseVariantItem) {
                return item;
            }
        }
        return Main.ITEMS.iterator().next();
    }
}