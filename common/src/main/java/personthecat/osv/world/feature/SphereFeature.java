package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.Random;

public class SphereFeature extends Feature<SphereConfig> {

    public static final SphereFeature INSTANCE = new SphereFeature();

    private SphereFeature() {
        super(SphereConfig.CODEC);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunk, Random rand, BlockPos pos, SphereConfig cfg) {
        final double radX = cfg.radiusX.rand(rand);
        final double radY = cfg.radiusY.rand(rand);
        final double radZ = cfg.radiusZ.rand(rand);
        final double radX2 = radX * radX;
        final double radY2 = radY * radY;
        final double radZ2 = radZ * radZ;
        final int minY = Math.max(0, pos.getY() - (int) radY);
        final int maxY = Math.min(255, pos.getY() + (int) radY);
        final BlockPos.MutableBlockPos mutable = pos.mutable();
        int count = 0;

        for (int x = pos.getX() - (int) radX; x <= pos.getX() + (int) radX; x++) {
            final double distX = x - pos.getX();
            final double distX2 = distX * distX;

            for (int z = pos.getZ() - (int) radZ; z <= pos.getZ() + (int) radZ; z++) {
                final double distZ = z - pos.getZ();
                final double distZ2 = distZ * distZ;

                for (int y = minY; y <= maxY; y++) {
                    final double distY = y - pos.getY();
                    final double distY2 = distY * distY;

                    if (distX2 / radX2 + distY2 / radY2 + distZ2 / radZ2 <= 1.0) {
                        if (cfg.integrity == 1.0 || rand.nextDouble() <= cfg.integrity) {
                            if (cfg.placer.place(level, rand, mutable.set(x, y, z))) {
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
