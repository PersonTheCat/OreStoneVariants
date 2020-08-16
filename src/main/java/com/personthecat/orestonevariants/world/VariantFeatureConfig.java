package com.personthecat.orestonevariants.world;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Map;

public class VariantFeatureConfig implements IFeatureConfig {
    public final OreProperties target;
    public final int size;
    public final double chance;
    public final double denseChance;

    public VariantFeatureConfig(OreProperties target, int size, double chance, double denseChance) {
        this.target = target;
        this.size = size;
        this.chance = chance;
        this.denseChance = denseChance;
    }

    // 1.16.2?
    @SuppressWarnings("unchecked")
    public <T> Dynamic<T> serialize(DynamicOps<T> dyn) {
        Map<T, T> map = ImmutableMap.of(
            dyn.createString("target"), dyn.createString(target.oreLookup),
            dyn.createString("size"), dyn.createInt(size),
            dyn.createString("chance"), dyn.createDouble(chance),
            dyn.createString("denseChance"), dyn.createDouble(denseChance)
        );
        return new Dynamic(dyn, dyn.createMap(map));
    }
}
