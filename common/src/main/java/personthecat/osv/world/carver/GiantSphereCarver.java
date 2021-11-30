package personthecat.osv.world.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import personthecat.catlib.util.HashGenerator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GiantSphereCarver extends GlobalFeature<GiantSphereCollection> {

    public static final GiantSphereCarver INSTANCE = new GiantSphereCarver();
    private static final long FALLBACK_SEED = 1058509149410904L;

    private GiantSphereCarver() {
        super(GiantSphereCollection.CODEC, 255);
    }

    @Override
    public ConfiguredWorldCarver<?> configured(final Collection<FeatureStem> configs) {
        final Collection<GiantSphereConfig> mapped = configs.stream()
            .map(GiantSphereConfig::fromStem)
            .collect(Collectors.toList());
        return this.configured(new GiantSphereCollection(mapped));
    }

    @Override
    public boolean carve(ChunkAccess chunk, Function<BlockPos, Biome> biomes, Random rand, int seaLevel,
                         int dX, int dZ, int cX, int cZ, BitSet mask, GiantSphereCollection configs) {

        final Biome b = biomes.apply(new BlockPos((cX << 4) + 8, 64, (cZ << 4) + 8));
        final BitSet flags = new BitSet();
        boolean placed = false;

        for (final GiantSphereConfig cfg : configs.configs) {
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
            ChunkAccess chunk, Random rand, int dX, int dZ, int cX, int cZ, BitSet mask, BitSet flags, GiantSphereConfig cfg) {

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
                        if (cfg.placer.placeUnchecked(chunk, rand, x, y, z)) {
                            flags.set(flag);
                            count++;
                        }
                    }
                }
            }
        }
        return count > 0;
    }
}
