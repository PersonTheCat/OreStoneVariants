package com.personthecat.orestonevariants.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class VariantPlacementConfig implements IPlacementConfig {

    /** Required so that VPC may be serialized internally via vanilla functions. */
    public static final Codec<VariantPlacementConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("count").forGetter(config -> config.count),
            Codec.INT.fieldOf("minHeight").forGetter(config -> config.minHeight),
            Codec.INT.fieldOf("maxHeight").forGetter(config -> config.minHeight + config.incrHeight),
            Codec.DOUBLE.fieldOf("chance").forGetter(config -> config.chance)
        ).apply(instance, VariantPlacementConfig::new)
    );

    /** The number of attempted spawns in the current chunk. */
    public final int count;
    /** The minimum height at which these settings apply. */
    public final int minHeight;
    /** The distance above the minimum height at which these settings apply. */
    public final int incrHeight;
    /** The chance that these settings will succeed for each attempted spawn. */
    public final double chance;

    public VariantPlacementConfig(int count, int minHeight, int maxHeight, double chance) {
        this.count = count;
        this.minHeight = minHeight;
        this.incrHeight = maxHeight - minHeight;
        this.chance = chance;
    }
}