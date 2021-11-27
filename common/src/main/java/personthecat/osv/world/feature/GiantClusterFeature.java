package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.catlib.util.HashGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class GiantClusterFeature extends GlobalFeature<GiantClusterCollection> {

    public static final GiantClusterFeature INSTANCE = new GiantClusterFeature();

    private GiantClusterFeature() {
        super(GiantClusterCollection.CODEC);
    }

    @Override
    public ConfiguredFeature<?, ?> configured(final Collection<FeatureStem> configs) {
        final Collection<GiantClusterConfig> mapped = configs.stream()
            .map(GiantClusterConfig::fromStem)
            .collect(Collectors.toList());
        return this.configured(new GiantClusterCollection(mapped));
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunk, Random rand, BlockPos pos, GiantClusterCollection cfg) {
        final ChunkAccess reader = level.getChunk(pos);
        final int chunkX = reader.getPos().x;
        final int chunkZ = reader.getPos().z;

        final List<Cluster> located = locateClusters(level, chunkX, chunkZ, cfg);
        if (located.isEmpty()) return false;

        final BitSet flags = new BitSet();
        final int aX = chunkX << 4;
        final int aZ = chunkZ << 4;
        int count = 0;

        for (final Cluster cluster : located) {
            for (int x = aX; x < aX + 16; x++) {
                for (int z = aZ; z < aZ + 16; z++) {
                    for (int y = cfg.bounds.min; y < cfg.bounds.max; y++) {

                        final int flag = ((x & 15) << 12) | ((z & 15) << 8) | y;
                        if (flags.get(flag)) continue;

                        final double distX = x - cluster.x;
                        final double distY = y - cluster.y;
                        final double distZ = z - cluster.z;
                        final double distX2 = distX * distX;
                        final double distY2 = distY * distY;
                        final double distZ2 = distZ * distZ;

                        final double sum = distX2 / cluster.radX2 + distY2 / cluster.radY2 + distZ2 / cluster.radZ2;
                        if (sum <= 1.0) {
                            if (place(reader, cluster, x, y, z)) {
                                flags.set(flag);
                                count++;
                            }
                        } else if (sum <= 1.0 + cluster.cfg.amplitude) {
                            final float yaw = (float) Mth.atan2(distZ, distX);
                            final float pitch = (float) (Mth.atan2(Mth.sqrt(distX2 + distZ2), distY) + Math.PI);
                            final float cosPitch = Mth.cos(pitch);
                            final float sinPitch = Mth.sin(pitch);
                            final int oX = (int) (((float) cluster.x) + (cluster.radX * Mth.cos(yaw) * cosPitch));
                            final int oY = (int) (((float) cluster.y) + (cluster.radY * sinPitch));
                            final int oZ = (int) (((float) cluster.z) + (cluster.radZ * Mth.sin(yaw) * cosPitch));

                            if (sum <= 1.0 + cluster.cfg.noise.getNoiseScaled(oX, oY, oZ)) {
                                if (place(reader, cluster, x, y, z)) {
                                    flags.set(flag);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return count > 0;
    }

    private static boolean place(final ChunkAccess reader, final Cluster cluster, final int x, final int y, final int z) {
        if (cluster.cfg.integrity == 1.0 || cluster.rand.nextDouble() <= cluster.cfg.integrity) {
            return cluster.cfg.placer.placeUnchecked(reader, cluster.rand, x, y, z);
        }
        return false;
    }

    private static List<Cluster> locateClusters(WorldGenLevel level, int chunkX, int chunkZ, GiantClusterCollection configs) {
        final List<Cluster> clusters = new ArrayList<>();
        final MutableBlockPos mutable = new MutableBlockPos();

        for (final GiantClusterConfig cfg : configs.configs) {
            final int cRadiusX = (int) (((double) cfg.radiusX.max) * (1.0 + cfg.amplitude)) + 1;
            final int cRadiusZ = (int) (((double) cfg.radiusZ.max) * (1.0 + cfg.amplitude)) + 1;

            for (int cX = chunkX - cRadiusX; cX <= chunkX + cRadiusX; cX++) {
                for (int cZ = chunkZ - cRadiusZ; cZ <= chunkZ + cRadiusZ; cZ++) {
                    final double hash = HashGenerator.getHash(level.getSeed(), cX, cfg.placer.getId(), cZ);

                    if (hash > cfg.threshold) {
                        final int aX = (cX << 4) + 8;
                        final int aZ = (cZ << 4) + 8;

                        if (cfg.biomes.isEmpty() || cfg.biomes.test(level.getBiome(mutable.set(aX, 0, aZ)))) {
                            final Random localRand = new Random(Double.doubleToLongBits(hash));
                            clusters.add(new Cluster(cfg, localRand, aX, aZ));
                        }
                    }
                }
            }
        }
        return clusters;
    }

    private static class Cluster {
        final GiantClusterConfig cfg;
        final Random rand;
        final int x;
        final int y;
        final int z;
        final int radX;
        final int radY;
        final int radZ;
        final int radX2;
        final int radY2;
        final int radZ2;

        Cluster(GiantClusterConfig cfg, Random rand, int x, int z) {
            this.cfg = cfg;
            this.rand = rand;
            this.x = x;
            this.y = cfg.height.rand(rand);
            this.z = z;
            this.radX = cfg.radiusX.rand(rand) - (cfg.radiusX.diff() / 2);
            this.radY = cfg.radiusY.rand(rand) - (cfg.radiusY.diff() / 2);
            this.radZ = cfg.radiusZ.rand(rand) - (cfg.radiusZ.diff() / 2);
            this.radX2 = radX * radX;
            this.radY2 = radY * radY;
            this.radZ2 = radZ * radZ;
        }
    }
}
