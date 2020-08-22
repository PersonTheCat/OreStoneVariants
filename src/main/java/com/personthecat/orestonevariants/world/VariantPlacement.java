package com.personthecat.orestonevariants.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.placement.SimplePlacement;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VariantPlacement extends SimplePlacement<VariantPlacementConfig> {

    public static final VariantPlacement INSTANCE = new VariantPlacement(VariantPlacementConfig.CODEC);

    private VariantPlacement(Codec<VariantPlacementConfig> config) {
        super(config);
    }

    @Override
    public Stream<BlockPos> getPositions(Random rand, VariantPlacementConfig config, BlockPos origin) {
        return IntStream.range(0, config.count)
            .filter(i -> config.chance == 1.0 || rand.nextFloat() <= config.chance)
            .mapToObj(i -> genPos(rand, config, origin));
    }

    private static BlockPos genPos(Random rand, VariantPlacementConfig config, BlockPos origin) {
        return new BlockPos(
            rand.nextInt(16) + origin.getX(),
            rand.nextInt(config.incrHeight + 1) + config.minHeight,
            rand.nextInt(16) + origin.getZ()
        );
    }
}