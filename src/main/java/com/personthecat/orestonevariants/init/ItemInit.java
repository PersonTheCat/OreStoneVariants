package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.item.VariantItem;
import net.minecraft.item.Item;

import java.util.Set;
import java.util.stream.Collectors;

public class ItemInit {
    public static Set<Item> setupItems() {
        return Main.BLOCKS.stream()
            .map(VariantItem::new)
            .collect(Collectors.toSet());
    }
}