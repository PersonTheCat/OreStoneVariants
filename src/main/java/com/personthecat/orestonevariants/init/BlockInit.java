package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.config.Cfg;

import java.util.HashSet;
import java.util.Set;

public class BlockInit {
    public static Set<OreVariant> setupBlocks() {
        final Set<OreVariant> variants = new HashSet<>();
        Cfg.forEachVariant((props, block) -> variants.add(new OreVariant(props, block)));
        return variants;
    }
}