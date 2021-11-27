package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.catlib.util.HashGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class GiantSphereFeature extends GlobalFeature<GiantSphereCollection> {

    public static final GiantSphereFeature INSTANCE = new GiantSphereFeature();

    private GiantSphereFeature() {
        super(GiantSphereCollection.CODEC);
    }

    @Override
    public ConfiguredFeature<?, ?> configured(final Collection<FeatureStem> configs) {
        final Collection<GiantSphereConfig> mapped = configs.stream()
            .map(GiantSphereConfig::fromStem)
            .collect(Collectors.toList());
        return this.configured(new GiantSphereCollection(mapped));
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunk, Random rand, BlockPos pos, GiantSphereCollection cfg) {
        final ChunkAccess reader = level.getChunk(pos);
        final int chunkX = reader.getPos().x;
        final int chunkZ = reader.getPos().z;

        final List<Sphere> located = locateSpheres(level, chunkX, chunkZ, cfg);
        if (located.isEmpty()) return false;

        final BitSet flags = new BitSet();
        final int aX = chunkX << 4;
        final int aZ = chunkZ << 4;
        int count = 0;

        for (final Sphere sphere : located) {
            for (int x = aX; x < aX + 16; x++) {
                for (int z = aZ; z < aZ + 16; z++) {
                    for (int y = cfg.bounds.min; y < cfg.bounds.max; y++) {

                        final int flag = ((x & 15) << 12) | ((z & 15) << 8) | y;
                        if (flags.get(flag)) continue;

                        final double distX = x - sphere.x;
                        final double distY = y - sphere.y;
                        final double distZ = z - sphere.z;
                        final double distX2 = distX * distX;
                        final double distY2 = distY * distY;
                        final double distZ2 = distZ * distZ;

                        if (distX2 / sphere.radX2 + distY2 / sphere.radY2 + distZ2 / sphere.radZ2 <= 1) {
                            if (sphere.cfg.integrity == 1.0 || sphere.rand.nextDouble() <= sphere.cfg.integrity) {
                                if (sphere.cfg.placer.placeUnchecked(reader, sphere.rand, x, y, z)) {
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

    private static List<Sphere> locateSpheres(WorldGenLevel level, int chunkX, int chunkZ, GiantSphereCollection configs) {
        final List<Sphere> spheres = new ArrayList<>();
        final MutableBlockPos mutable = new MutableBlockPos();

        for (final GiantSphereConfig cfg : configs.configs) {
            final int cRadiusX = (cfg.radiusX.max >> 4) + 1;
            final int cRadiusZ = (cfg.radiusZ.max >> 4) + 1;

            for (int cX = chunkX - cRadiusX; cX <= chunkX + cRadiusX; cX++) {
                for (int cZ = chunkZ - cRadiusZ; cZ <= chunkZ + cRadiusZ; cZ++) {
                    final double hash = HashGenerator.getHash(level.getSeed(), cX, cfg.placer.getId(), cZ);

                    if (hash > cfg.threshold) {
                        final int aX = (cX << 4) + 8;
                        final int aZ = (cZ << 4) + 8;

                        if (cfg.biomes.isEmpty() || cfg.biomes.test(level.getBiome(mutable.set(aX, 0, aZ)))) {
                            final Random localRand = new Random(Double.doubleToLongBits(hash));
                            spheres.add(new Sphere(cfg, localRand, aX, aZ));
                        }
                    }
                }
            }
        }
        return spheres;
    }

    private static class Sphere {
        final GiantSphereConfig cfg;
        final Random rand;
        final int x;
        final int y;
        final int z;
        final int radX2;
        final int radY2;
        final int radZ2;

        Sphere(GiantSphereConfig cfg, Random rand, int x, int z) {
            this.cfg = cfg;
            this.rand = rand;
            this.x = x;
            this.y = cfg.height.rand(rand);
            this.z = z;
            int radX = cfg.radiusX.rand(rand) - (cfg.radiusX.diff() / 2);
            int radY = cfg.radiusY.rand(rand) - (cfg.radiusY.diff() / 2);
            int radZ = cfg.radiusZ.rand(rand) - (cfg.radiusZ.diff() / 2);
            this.radX2 = radX * radX;
            this.radY2 = radY * radY;
            this.radZ2 = radZ * radZ;
        }
    }
}
