package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.item.DenseVariantItem;
import com.personthecat.orestonevariants.item.VariantItem;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemInit {
    public static Set<VariantItem> setupItems() {
        return LazyRegistries.BLOCKS.stream()
            .flatMap(ItemInit::toBlockItems)
            .collect(Collectors.toSet());
    }

    private static Stream<VariantItem> toBlockItems(BaseOreVariant block) {
        return Stream.of(new VariantItem(block), new DenseVariantItem(block));
    }
}