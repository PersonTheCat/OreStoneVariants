package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Collections;
import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BlockInit {
    public static Set<BaseOreVariant> setupBlocks() {
        final OreProperties testProps = new OreProperties(
            "coal_ore",
            "minecraft",
            "assets/minecraft/textures/blocks/coal_ore.png",
            "coal_ore",
            Block.Properties.from(Blocks.COAL_ORE),
            empty(),
            Collections.emptyList(),
            null,
            false,
            false
        );
        final BlockState bgBlock = Blocks.SAND.getDefaultState();
        return Collections.singleton(new BaseOreVariant(testProps, bgBlock));
    }
}