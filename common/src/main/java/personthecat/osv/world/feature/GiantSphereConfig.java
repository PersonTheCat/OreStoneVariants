package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.data.Range;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.data.GiantSphereSettings;
import personthecat.osv.preset.data.SimpleDecoratorSettings;
import personthecat.osv.preset.data.SphereSettings;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.field;

public class GiantSphereConfig implements FeatureConfiguration {

    private static final Codec<GiantSphereConfig> UNVALIDATED = codecOf(
        defaulted(Range.CODEC, GiantSphereSettings.Fields.radiusX, Range.of(15, 30), c -> c.radiusX),
        defaulted(Range.CODEC, GiantSphereSettings.Fields.radiusY, Range.of(10, 20), c -> c.radiusY),
        defaulted(Range.CODEC, GiantSphereSettings.Fields.radiusZ, Range.of(15, 30), c -> c.radiusZ),
        defaulted(Range.CODEC, SimpleDecoratorSettings.Fields.height, Range.of(15, 40), c -> c.height),
        defaulted(Codec.DOUBLE, GiantSphereSettings.Fields.integrity, 1.0, c -> c.integrity),
        defaulted(Codec.DOUBLE, SimpleDecoratorSettings.Fields.chance, 0.15, c -> c.chance),
        defaulted(Codec.INT, SimpleDecoratorSettings.Fields.count, 1, c -> c.count),
        defaulted(BiomePredicate.CODEC, DecoratedFeatureSettings.Fields.biomes, BiomePredicate.ALL_BIOMES, c -> c.biomes),
        field(BlockPlacer.EITHER_CODEC, "placer", c -> c.placer),
        GiantSphereConfig::new
    );

    public static final Codec<GiantSphereConfig> CODEC =
        UNVALIDATED.flatXmap(c -> GiantSphereSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ),
            c -> SphereSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ));

    final Range radiusX;
    final Range radiusY;
    final Range radiusZ;
    final Range height;
    final double integrity;
    final double chance;
    final double selectionThreshold;
    final int count;
    final BiomePredicate biomes;
    final BlockPlacer placer;

    public GiantSphereConfig(final Range radiusX, final Range radiusY, final Range radiusZ, final Range height,
                             final double integrity, final double chance, final int count, final BiomePredicate biomes,
                             final BlockPlacer placer) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.height = height;
        this.integrity = integrity;
        this.chance = chance;
        this.selectionThreshold = (1.0 - chance) * 92.0;
        this.count = count;
        this.biomes = biomes;
        this.placer = placer;
    }
}