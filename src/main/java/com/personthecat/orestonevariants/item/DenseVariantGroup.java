package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.init.LazyRegistries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;

public class DenseVariantGroup extends ItemGroup {

    private static final ResourceLocation DEFAULT_ITEM = osvLocation("dense_diamond_ore_granite");

    public static DenseVariantGroup GROUP = new DenseVariantGroup();

    private DenseVariantGroup() {
        super("dense_variants");
    }

    @NotNull
    @Override
    public ItemStack createIcon() {
        final Item item = LazyRegistries.ITEMS.getOptional(DEFAULT_ITEM)
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