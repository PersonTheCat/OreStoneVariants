package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class VariantClusterFeature extends Feature<VariantClusterConfig> {

    public static final VariantClusterFeature INSTANCE = new VariantClusterFeature();

    private VariantClusterFeature() {
        super(VariantClusterConfig.CODEC);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunk, Random rand, BlockPos pos, VariantClusterConfig cfg) {
        float randPI = rand.nextFloat() * (float) Math.PI;

        float sizeA = (float) cfg.size / 8.0F;
        float sinSizeA = Mth.sin(randPI) * sizeA;
        float costSizeA = Mth.cos(randPI) * sizeA;
        double aX = (float) pos.getX() + sinSizeA;
        double bX = (float) pos.getX() - sinSizeA;
        double aZ = (float) pos.getZ() + costSizeA;
        double bZ = (float) pos.getZ() - costSizeA;
        double aY = pos.getY() + rand.nextInt(3) - 2;
        double bY = pos.getY() + rand.nextInt(3) - 2;

        int sizeB = Mth.ceil((sizeA + 1.0F) / 2.0F);
        int startX = pos.getX() - Mth.ceil(sizeA) - sizeB;
        int y = pos.getY() - 2 - sizeB;
        int startZ = pos.getZ() - Mth.ceil(sizeA) - sizeB;

        // Max cluster size?
        int offset = 2 * (Mth.ceil(sizeA) + sizeB);
        int diameter = 2 * (2 + sizeB);

        for (int x = startX; x <= startX + offset; x++) {
            for (int z = startZ; z <= startZ + offset; z++) {
                if (y <= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z)) {
                    return doPlace(level, rand, cfg, aX, bX, aZ, bZ, aY, bY, startX, y, startZ, offset, diameter);
                }
            }
        }
        return false;
    }

    private boolean doPlace(WorldGenLevel level, Random rand, VariantClusterConfig cfg, double aX, double bX, double aZ,
                            double bZ, double aY, double bY, int startX, int startY, int startZ, int offset, int diameter) {

        BitSet flags = new BitSet(offset * diameter * offset);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double[] values = getValues(rand, cfg.size, aX, bX, aY, bY, aZ, bZ);
        int count = 0;

        for (int i = 0; i < cfg.size; i++) {
            final double radius = values[i * 4 + 3];
            if (radius >= 0.0D) { // Value not drained; inside range.
                double centerX = values[i * 4];
                double centerY = values[i * 4 + 1];
                double centerZ = values[i * 4 + 2];
                int minX = Math.max(Mth.floor(centerX - radius), startX);
                int minY = Math.max(Mth.floor(centerY - radius), startY);
                int minZ = Math.max(Mth.floor(centerZ - radius), startZ);
                int maxX = Math.max(Mth.floor(centerX + radius), minX);
                int maxY = Math.max(Mth.floor(centerY + radius), minY);
                int maxZ = Math.max(Mth.floor(centerZ + radius), minZ);

                for (int x = minX; x <= maxX; x++) {
                    double finalX = ((double) x + 0.5D - centerX) / radius;

                    if (finalX * finalX < 1.0) {
                        for (int y = minY; y <= maxY; y++) {
                            double finalY = ((double) y + 0.5 - centerY) / radius;

                            if (finalX * finalX + finalY * finalY < 1.0) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    double finalZ = ((double) z + 0.5 - centerZ) / radius;

                                    if (finalX * finalX + finalY * finalY + finalZ * finalZ < 1.0) { // Inside sphere
                                        // A unique, numeric identifier for each position.
                                        int flag = x - startX + (y - startY) * offset + (z - startZ) * offset * diameter;

                                        if (!flags.get(flag)) { // rand.nextFloat() <= config.chance) {
                                            flags.set(flag);
                                            pos.set(x, y, z);

                                            if (tryPlace(cfg, rand, level, pos)) {
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
            double centerX = Mth.lerp(fraction, aX, bX);
            double centerY = Mth.lerp(fraction, aY, bY);
            double centerZ = Mth.lerp(fraction, aZ, bZ);
            double diameter = rand.nextDouble() * (double) size / 16.0;
            double finalRadius = ((Mth.sin((float) (Math.PI * fraction)) + 1.0F) * diameter + 1.0) / 2.0;
            values[i * 4] = centerX;
            values[i * 4 + 1] = centerY;
            values[i * 4 + 2] = centerZ;
            values[i * 4 + 3] = finalRadius;
        }
        return drainOutOfBounds(values, size);
    }

    private double[] drainOutOfBounds(double[] values, int size) {
        for (int i = 0; i < size - 1; i++) {
            if (values[i * 4 + 3] > 0.0) { // Inside range; value not drained
                for (int j = i + 1; j < size; j++) {
                    if (values[j * 4 + 3] > 0.0) {
                        double distX = values[i * 4] - values[j * 4];
                        double distY = values[i * 4 + 1] - values[j * 4 + 1];
                        double distZ = values[i * 4 + 2] - values[j * 4 + 2];
                        double radius = values[i * 4 + 3] - values[j * 4 + 3];

                        if (radius * radius > distX * distX + distY * distY + distZ * distZ) {
                            if (radius > 0.0) {
                                values[j * 4 + 3] = -1.0;
                            } else {
                                values[i * 4 + 3] = -1.0;
                            }
                        }
                    }
                }
            }
        }
        return values;
    }

    private boolean tryPlace(VariantClusterConfig config, Random rand, WorldGenLevel level, BlockPos pos) {
        final BlockState bg = level.getBlockState(pos);
        final List<BlockMatchingSpawnConfig> blocks = config.blocks.get(bg);
        if (blocks != null) {
            for (final BlockMatchingSpawnConfig block : blocks) {
                if (rand.nextDouble() <= block.chance) {
                    level.setBlock(pos, block.block, 2);
                    return true;
                }
            }
        }
        return false;
    }
}
