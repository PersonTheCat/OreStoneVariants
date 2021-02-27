package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.BlockState;

import java.util.HashSet;
import java.util.Set;

public class BlockInit {
    public static Set<BaseOreVariant> setupBlocks() {
        Set<BaseOreVariant> variants = new HashSet<>();
        for (BlockEntry entry : LazyRegistries.BLOCK_ENTRIES) {
            for (BlockState block : entry.blocks.blocks.get()) {
                for (OreProperties props : entry.properties.properties) {
                    variants.add(new BaseOreVariant(props, block));
                }
            }
        }
        return variants;
    }
}