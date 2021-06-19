package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BlockInit {
    public static Map<ResourceLocation, OreVariant> setupBlocks() {
        final Map<ResourceLocation, OreVariant> variants = new HashMap<>();
        Cfg.forEachVariant((props, block) -> {
            final OreVariant v = new OreVariant(props, block);
            variants.put(v.getRegistryName(), v);
        });
        return variants;
    }
}