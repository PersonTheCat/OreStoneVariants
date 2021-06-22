package com.personthecat.orestonevariants.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.personthecat.orestonevariants.properties.OreProperties;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NestedType {

    /** So that containers can be serialized through vanilla means. */
    public static final Codec<NestedType> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            OreProperties.CODEC.fieldOf("type").forGetter(config -> config.type),
            Codec.DOUBLE.fieldOf("chance").forGetter(config -> config.chance))
        .apply(instance, NestedType::new));

    public final OreProperties type;
    public final double chance;
}
