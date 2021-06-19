package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.item.DenseVariantItem;
import com.personthecat.orestonevariants.item.VariantItem;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemInit {
    public static Map<ResourceLocation, VariantItem> setupItems() {
        return LazyRegistries.BLOCKS.values().stream()
            .flatMap(ItemInit::toBlockItems)
            .collect(Collectors.toMap(VariantItem::getRegistryName, v -> v));
    }

    private static Stream<VariantItem> toBlockItems(OreVariant block) {
        return Stream.of(new VariantItem(block), new DenseVariantItem(block));
    }
}