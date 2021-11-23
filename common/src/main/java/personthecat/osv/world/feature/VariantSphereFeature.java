package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.Random;

public class VariantSphereFeature extends Feature<VariantSphereConfig> {

    public static final VariantSphereFeature INSTANCE = new VariantSphereFeature();

    private VariantSphereFeature() {
        super(VariantSphereConfig.CODEC);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunk, Random rand, BlockPos pos, VariantSphereConfig cfg) {
        final int radX = cfg.radiusX.rand(rand);
        final int radY = cfg.radiusY.rand(rand);
        final int radZ = cfg.radiusZ.rand(rand);
        final int radX2 = radX * radX;
        final int radY2 = radY * radY;
        final int radZ2 = radZ * radZ;
        final int minY = Math.max(0, pos.getY() - radY);
        final int maxY = Math.min(255, pos.getY() + radY);
        int count = 0;

        for (int x = pos.getX() - radX; x < pos.getX() + radX; x++) {
            final int distX = x - pos.getX();
            final int distX2 = distX * distX;

            for (int z = pos.getZ() - radZ; x < pos.getZ() + radZ; z++) {
                final int distZ = z - pos.getZ();
                final int distZ2 = distZ * distZ;

                for (int y = minY; y < maxY; y++) {
                    final int distY = y - pos.getY();
                    final int distY2 = distY * distY;

                    if (distX2 / radX2 + distY2 / radY2 + distZ2 / radZ2 <= 1) {
                        if (cfg.integrity == 1 || rand.nextDouble() <= cfg.integrity) {
                            if (BlockMatchingSpawnConfig.tryPlace(cfg.blocks, rand, level, pos)) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count > 0;
    }
}
