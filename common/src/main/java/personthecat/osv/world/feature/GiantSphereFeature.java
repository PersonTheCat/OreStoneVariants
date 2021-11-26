package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.catlib.util.RandomChunkSelector;

import java.util.*;
import java.util.stream.Collectors;

public class GiantSphereFeature extends GlobalFeature<GiantSphereCollection> {

    public static final GiantSphereFeature INSTANCE = new GiantSphereFeature();
    private static final RandomChunkSelector SELECTOR = RandomChunkSelector.DEFAULT;

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
        final int chunkX = pos.getX() >> 4;
        final int chunkZ = pos.getZ() >> 4;

        final List<Sphere> located = locateSpheres(level, chunkX, chunkZ, cfg);
        if (located.isEmpty()) return false;

        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        final BitSet flags = new BitSet();
        final int aX = chunkX << 4;
        final int aZ = chunkZ << 4;
        int count = 0;

        for (final Sphere sphere : located) {
            for (int x = aX; x < aX + 16; x++) {
                for (int z = aZ; z < aZ + 16; z++) {
                    for (int y = cfg.bounds.min; y < cfg.bounds.max; y++) {
                        final int flag = x << 12 | z << 8 | y;
                        if (flags.get(flag)) continue;
                        flags.set(flag);

                        final double distX = x - sphere.x;
                        final double distY = y - sphere.y;
                        final double distZ = z - sphere.z;
                        final double distX2 = distX * distX;
                        final double distY2 = distY * distY;
                        final double distZ2 = distZ * distZ;

                        if (distX2 / sphere.radX2 + distY2 / sphere.radY2 + distZ2 / sphere.radZ2 <= 1) {
                            if (sphere.cfg.chance == 1.0 || sphere.rand.nextDouble() <= sphere.cfg.chance) {
                                if (sphere.cfg.placer.place(level, sphere.rand, mutable.set(x, y, z))) {
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
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (final GiantSphereConfig cfg : configs.configs) {
            final int cRadiusX = (cfg.radiusX.max >> 4) + 1;
            final int cRadiusZ = (cfg.radiusZ.max >> 4) + 1;

            for (int cX = chunkX - cRadiusX; cX <= chunkX + cRadiusX; cX++) {
                for (int cZ = chunkZ - cRadiusZ; cZ <= chunkZ + cRadiusZ; cZ++) {
                    final int aX = (cX << 4) + 8;
                    final int aZ = (cZ << 4) + 8;

                    if (SELECTOR.testCoordinates(level.getSeed(), cfg.placer.getId(), aX, aZ, cfg.threshold)) {
                        if (cfg.biomes.test(level.getBiome(mutable.set(aX, 0, aZ)))) {
                            final Random localRand = new Random(level.getSeed() ^ cX ^ cZ);
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
        final int id;
        final int x;
        final int y;
        final int z;
        final int radY;
        final int radX2;
        final int radY2;
        final int radZ2;

        Sphere(GiantSphereConfig cfg, Random rand, int x, int z) {
            this.cfg = cfg;
            this.rand = rand;
            this.id = cfg.placer.getId();
            this.x = x;
            this.y = cfg.height.rand(rand);
            this.z = z;
            int radX = cfg.radiusX.rand(rand) - (cfg.radiusX.diff() / 2);
            this.radY = cfg.radiusY.rand(rand) - (cfg.radiusY.diff() / 2);
            int radZ = cfg.radiusZ.rand(rand) - (cfg.radiusZ.diff() / 2);
            this.radX2 = radX * radX;
            this.radY2 = radY * radY;
            this.radZ2 = radZ * radZ;
        }
    }
}
