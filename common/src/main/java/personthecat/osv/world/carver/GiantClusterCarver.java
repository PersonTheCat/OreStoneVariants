package personthecat.osv.world.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import personthecat.catlib.util.HashGenerator;
import personthecat.osv.world.placer.BlockPlacer;

import java.util.BitSet;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GiantClusterCarver extends GlobalFeature<GiantClusterCollection> {

    public static final GiantClusterCarver INSTANCE = new GiantClusterCarver();
    private static final long FALLBACK_SEED = 1058509149410904L;

    private GiantClusterCarver() {
        super(GiantClusterCollection.CODEC, 256);
    }

    @Override
    public ConfiguredWorldCarver<?> configured(final Collection<FeatureStem> configs) {
        final Collection<GiantClusterConfig> mapped = configs.stream()
            .map(GiantClusterConfig::fromStem)
            .collect(Collectors.toList());
        return this.configured(new GiantClusterCollection(mapped));
    }

    @Override
    public boolean carve(ChunkAccess chunk, Function<BlockPos, Biome> biomes, Random rand, int seaLevel,
                         int dX, int dZ, int cX, int cZ, BitSet mask, GiantClusterCollection configs) {

        final Biome b = biomes.apply(new BlockPos((cX << 4) + 8, 64, (cZ << 4) + 8));
        final BitSet flags = new BitSet();
        boolean placed = false;

        for (final GiantClusterConfig cfg : configs.configs) {
            if (!cfg.biomes.test(b)) {
                continue;
            }
            for (int i = 0; i < cfg.count; i++) {
                final double hash = HashGenerator.getHash(getSeed(rand) + i, dX, 1024 + cfg.placer.getId(), dZ);

                if (hash > cfg.threshold) {
                    rand.setSeed(Double.doubleToLongBits(hash));
                    placed |= gen(chunk, rand, dX, dZ, cX, cZ, mask, flags, cfg);
                }
            }
        }
        return placed;
    }

    private static long getSeed(Random rand) {
        // Todo: handle the carver seed through CatLib.
        return FALLBACK_SEED;
    }

    private static boolean gen(
            ChunkAccess chunk, Random rand, int dX, int dZ, int cX, int cZ, BitSet mask, BitSet flags, GiantClusterConfig cfg) {

        int count = 0;

        final int radX = cfg.radiusX.rand(rand) - (cfg.radiusX.diff() / 2);
        if ((radX >> 4) < Mth.abs(dX - cX) - 1) return false;
        final int radZ = cfg.radiusZ.rand(rand) - (cfg.radiusZ.diff() / 2);
        if ((radZ >> 4) < Mth.abs(dZ - cZ) - 1) return false;
        final int radY = cfg.radiusY.rand(rand) - (cfg.radiusY.diff() / 2);

        final int rX2 = radX * radX;
        final int rZ2 = radZ * radZ;
        final int rY2 = radY * radY;
        final int aX = (dX << 4) + 8;
        final int aY = cfg.height.rand(rand);
        final int aZ = (dZ << 4) + 8;

        final int minY = Math.max(0, aY - radY);
        final int maxY = Math.min(255, aY + radY);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {

                    final int flag = (x << 12) | (z << 8) | y;
                    if (mask.get(flag) || flags.get(flag)) continue;

                    final double distX = ((cX << 4) + x) - aX;
                    final double distY = y - aY;
                    final double distZ = ((cZ << 4) + z) - aZ;
                    final double distX2 = distX * distX;
                    final double distY2 = distY * distY;
                    final double distZ2 = distZ * distZ;

                    final double sum = distX2 / rX2 + distY2 / rY2 + distZ2 / rZ2;
                    if (sum <= 1.0) {
                        if (place(chunk, cfg.placer, rand, cfg.integrity, x, y, z)) {
                            flags.set(flag);
                            count++;
                        }
                    } else if (sum <= 1.0 + cfg.amplitude) {
                        final float yaw = (float) Mth.atan2(distZ, distX);
                        final float pitch = (float) (Mth.atan2(Mth.sqrt(distX2 + distZ2), distY) + Math.PI);
                        final float cosPitch = Mth.cos(pitch);
                        final float sinPitch = Mth.sin(pitch);
                        final int oX = (int) (((float) aX) + (radX * Mth.cos(yaw) * cosPitch));
                        final int oY = (int) (((float) aY) + (radY * sinPitch));
                        final int oZ = (int) (((float) aZ) + (radZ * Mth.sin(yaw) * cosPitch));

                        if (sum <= 1.0 + cfg.noise.getNoiseScaled(oX, oY, oZ)) {
                            if (place(chunk, cfg.placer, rand, cfg.integrity, x, y, z)) {
                                flags.set(flag);
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count > 0;
    }

    private static boolean place(ChunkAccess reader, BlockPlacer placer, Random rand, double integrity, int x, int y, int z) {
        if (integrity == 1.0 || rand.nextDouble() <= integrity) {
            return placer.placeUnchecked(reader, rand, x, y, z);
        }
        return false;
    }
}
