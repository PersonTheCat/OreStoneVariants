package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.BlockState;

import java.util.HashSet;
import java.util.Set;

public class BlockInit {
    public static Set<OreVariant> setupBlocks() {
        final Set<OreVariant> variants = new HashSet<>();
        for (BlockEntry entry : LazyRegistries.BLOCK_ENTRIES) {
            for (BlockState block : entry.blocks.blocks.get()) {
                for (OreProperties props : entry.properties.properties) {
                    variants.add(new OreVariant(props, block));
                }
            }
        }
        return variants;
    }
}