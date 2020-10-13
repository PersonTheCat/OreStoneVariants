package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.api.VariantRegistryEvent;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashSet;
import java.util.Set;

public class BlockInit {
    public static Set<BaseOreVariant> setupBlocks() {
        Set<BaseOreVariant> variants = new HashSet<>();
        for (BlockEntry entry : Main.BLOCK_ENTRIES) {
            for (BlockState block : entry.blocks.blocks.get()) {
                for (OreProperties props : entry.properties.properties) {
                    variants.add(BaseOreVariant.of(props, block));
                }
            }
        }
        MinecraftForge.EVENT_BUS.post(new VariantRegistryEvent(variants));
        return variants;
    }
}