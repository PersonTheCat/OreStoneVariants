package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

public class ItemInit {
    public static Set<Item> setupItems() {
        return Main.BLOCKS.stream()
            .map(ItemInit::toBlockItem)
            .collect(Collectors.toSet());
    }

    private static Item toBlockItem(Block block) {
        return new BlockItem(block, new Item.Properties()).setRegistryName(block.getRegistryName());
    }
}