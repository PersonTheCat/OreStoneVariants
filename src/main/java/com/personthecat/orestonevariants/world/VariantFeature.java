package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.DualMap;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Random;

import static net.minecraft.util.math.MathHelper.*;

public class VariantFeature extends Feature<VariantFeatureConfig> {
    /** A map containing all of the ores that should be spawned given the background and properties. */
    private static final Lazy<DualMap<BlockState, OreProperties, BlockState>> FEATURE_MAP =
        new Lazy<>(VariantFeature::createFeatureMap);

    public VariantFeature(VariantFeatureConfig config) {
        super(dyn -> config);
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> chunk, Random rand, BlockPos pos, VariantFeatureConfig config) {
        float randPI = rand.nextFloat() * (float) Math.PI;

        float sizeA = (float) config.size / 8.0F;
        float sinSizeA = sin(randPI) * sizeA;
        float costSizeA = cos(randPI) * sizeA;
        double aX = (float) pos.getX() + sinSizeA;
        double bX = (float) pos.getX() - sinSizeA;
        double aZ = (float) pos.getZ() + costSizeA;
        double bZ = (float) pos.getZ() - costSizeA;
        double aY = pos.getY() + rand.nextInt(3) - 2;
        double bY = pos.getY() + rand.nextInt(3) - 2;

        int sizeB = ceil((sizeA + 1.0F) / 2.0F);
        int startX = pos.getX() - ceil(sizeA) - sizeB;
        int y = pos.getY() - 2 - sizeB;
        int startZ = pos.getZ() - ceil(sizeA) - sizeB;

        // Max cluster size?
        int offset = 2 * (ceil(sizeA) + sizeB);
        int diameter = 2 * (2 + sizeB);

        for (int x = startX; x <= startX + offset; x++) {
            for (int z = startZ; z <= startZ + offset; z++) {
                if (y <= world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, x, z)) {
                    return doPlace(world, rand, config, aX, bX, aZ, bZ, aY, bY, startX, y, startZ, offset, diameter);
                }
            }
        }

        return false;
    }

    protected boolean doPlace(IWorld world, Random rand, VariantFeatureConfig config, double aX, double bX, double aZ, double bZ, double aY, double bY, int startX, int startY, int startZ, int offset, int diameter) {
        BitSet flags = new BitSet(offset * diameter * offset);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double[] values = getValues(rand, config.size, aX, bX, aY, bY, aZ, bZ);
        int count = 0;

        for (int i = 0; i < config.size; i++) {
            double radius = values[i * 4 + 3];
            if (radius >= 0.0D) { // Value not drained; inside range.
                double centerX = values[i * 4];
                double centerY = values[i * 4 + 1];
                double centerZ = values[i * 4 + 2];
                int minX = Math.max(floor(centerX - radius), startX);
                int minY = Math.max(floor(centerY - radius), startY);
                int minZ = Math.max(floor(centerZ - radius), startZ);
                int maxX = Math.max(floor(centerX + radius), minX);
                int maxY = Math.max(floor(centerY + radius), minY);
                int maxZ = Math.max(floor(centerZ + radius), minZ);

                for (int x = minX; x <= maxX; x++) {
                    double finalX = ((double) x + 0.5D - centerX) / radius;

                    if (finalX * finalX < 1.0D) {
                        for (int y = minY; y <= maxY; y++) {
                            double finalY = ((double) y + 0.5D - centerY) / radius;

                            if (finalX * finalX + finalY * finalY < 1.0D) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    double finalZ = ((double) z + 0.5D - centerZ) / radius;

                                    if (finalX * finalX + finalY * finalY + finalZ * finalZ < 1.0D) { // Inside sphere
                                        int flag = x - startX + (y - startY) * offset + (z - startZ) * offset * diameter;

                                        if (!flags.get(flag)) { // Block already placed?
                                            flags.set(flag);
                                            pos.setPos(x, y, z);

                                            if (tryPlace(config, rand, world, pos)) {
                                                count++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return count > 0;
    }

    private double[] getValues(Random rand, int size, double aX, double bX, double aY, double bY, double aZ, double bZ) {
        double[] values = new double[size * 4];
        for (int i = 0; i < size; i++) {
            double fraction = (double) i / (double) size;
            double centerX = lerp(fraction, aX, bX);
            double centerY = lerp(fraction, aY, bY);
            double centerZ = lerp(fraction, aZ, bZ);
            double diameter = rand.nextDouble() * (double) size / 16.0D;
            double finalRadius = ((sin((float) (Math.PI * fraction)) + 1.0F) * diameter + 1.0D) / 2.0D;
            setAll(values, i * 4, centerX, centerY, centerZ, finalRadius);
        }
        return drainOutOfBounds(values, size);
    }

    private double[] drainOutOfBounds(double[] values, int size) {
        for (int i = 0; i < size - 1; i++) {
            if (values[i * 4 + 3] > 0.0D) { // Inside range; value not drained
                for (int j = i + 1; j < size; j++) {
                    if (values[j * 4 + 3] > 0.0D) {
                        double distX = values[i * 4] - values[j * 4];
                        double distY = values[i * 4 + 1] - values[j * 4 + 1];
                        double distZ = values[i * 4 + 2] - values[j * 4 + 2];
                        double radius = values[i * 4 + 3] - values[j * 4 + 3];

                        if (radius * radius > distX * distX + distY * distY + distZ * distZ) {
                            if (radius > 0.0D) {
                                values[j * 4 + 3] = -1.0D;
                            } else {
                                values[i * 4 + 3] = -1.0D;
                            }
                        }
                    }
                }
            }
        }
        return values;
    }

    private void setAll(double[] values, int index, double a, double b, double c, double d) {
        values[index] = a;
        values[index + 1] = b;
        values[index + 2] = c;
        values[index + 3] = d;
    }

    private boolean tryPlace(VariantFeatureConfig config, Random rand, IWorld world, BlockPos pos) {
        final BlockState bg = world.getBlockState(pos);
        BlockState state = getSpawnCandidate(config, bg);
        if (state != null) {
            if (config.denseChance != 0 && rand.nextFloat() <= config.denseChance) {
                state = state.with(BaseOreVariant.DENSE, true);
            }
            return world.setBlockState(pos, state, 2);
        }
        return false;
    }

    @Nullable
    private static BlockState getSpawnCandidate(VariantFeatureConfig config, BlockState bg) {
        return FEATURE_MAP.get().get(bg, config.target);
    }

    private static DualMap<BlockState, OreProperties, BlockState> createFeatureMap() {
        final DualMap.Builder<BlockState, OreProperties, BlockState> builder = new DualMap.Builder<>();
        for (BaseOreVariant block : Main.BLOCKS) {
            builder.put(block.bgBlock, block.properties, block.getDefaultState());
        }
        return builder.build();
    }
}