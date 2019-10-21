package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.item.DenseVariantItem;
import com.personthecat.orestonevariants.item.VariantItem;
import net.minecraft.item.Item;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemInit {
    public static Set<Item> setupItems() {
        return Main.BLOCKS.stream()
            .flatMap(ItemInit::toBlockItems)
            .collect(Collectors.toSet());
    }

    private static Stream<Item> toBlockItems(BaseOreVariant block) {
        final Item.Properties dummy = new Item.Properties();
        return Stream.of(
            new VariantItem(block, dummy),
            new DenseVariantItem(block, dummy)
        );
    }
}