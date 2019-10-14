package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.HashSet;
import java.util.Set;

public class BlockInit {
    public static Set<BaseOreVariant> setupBlocks() {
        Set<BaseOreVariant> blocks = new HashSet<>();
        // Temporarily spawn all test properties in sand.
        for (OreProperties props : Main.ORE_PROPERTIES) {
            final BlockState bgBlock = Blocks.SAND.getDefaultState();
            blocks.add(BaseOreVariant.of(props, bgBlock));
        }
        return blocks;
    }
}