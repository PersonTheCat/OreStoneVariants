package personthecat.osv.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.data.Range;
import personthecat.catlib.serialization.codec.CodecUtils;
import personthecat.fastnoise.FastNoise;
import personthecat.fastnoise.data.NoiseType;
import personthecat.osv.preset.data.GiantClusterSettings;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.preset.data.SimplePlacementSettings;
import personthecat.osv.preset.data.SphereSettings;
import personthecat.osv.world.placer.BlockPlacer;

import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.codec.FieldDescriptor.field;

public class GiantClusterConfig extends CarverConfiguration {

    private static final Codec<GiantClusterConfig> UNVALIDATED = CodecUtils.codecOf(
        defaulted(Range.CODEC, GiantClusterSettings.Fields.radiusX, Range.of(15, 30), c -> c.radiusX),
        defaulted(Range.CODEC, GiantClusterSettings.Fields.radiusY, Range.of(10, 20), c -> c.radiusY),
        defaulted(Range.CODEC, GiantClusterSettings.Fields.radiusZ, Range.of(15, 30), c -> c.radiusZ),
        defaulted(Range.CODEC, SimplePlacementSettings.Fields.height, Range.of(15, 40), c -> c.height),
        defaulted(Codec.DOUBLE, GiantClusterSettings.Fields.frequency, 0.1, c -> c.frequency),
        defaulted(Codec.doubleRange(0.0, 1.0), GiantClusterSettings.Fields.amplitude, 0.1, c -> c.amplitude),
        defaulted(Codec.DOUBLE, GiantClusterSettings.Fields.integrity, 1.0, c -> c.integrity),
        defaulted(Codec.DOUBLE, SimplePlacementSettings.Fields.chance, 0.025, c -> c.chance),
        defaulted(Codec.INT, SimplePlacementSettings.Fields.count, 1, c -> c.count),
        defaulted(BiomePredicate.CODEC, PlacedFeatureSettings.Fields.biomes, BiomePredicate.ALL_BIOMES, c -> c.biomes),
        field(BlockPlacer.EITHER_CODEC, "placer", c -> c.placer),
        GiantClusterConfig::new
    );

    public static final Codec<GiantClusterConfig> CODEC =
        UNVALIDATED.flatXmap(c -> GiantClusterSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ),
            c -> SphereSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ));

    final Range radiusX;
    final Range radiusY;
    final Range radiusZ;
    final Range height;
    final double frequency;
    final double amplitude;
    final double integrity;
    final double chance;
    final double threshold;
    final int count;
    final BiomePredicate biomes;
    final BlockPlacer placer;
    final FastNoise noise;

    public GiantClusterConfig(final Range radiusX, final Range radiusY, final Range radiusZ, final Range height,
                              final double frequency, final double amplitude, final double integrity, 
                              final double chance, final int count, final BiomePredicate biomes,
                              final BlockPlacer placer) {
        super(1.0F, ConstantHeight.ZERO, ConstantFloat.ZERO, VerticalAnchor.BOTTOM, CarverDebugSettings.DEFAULT);
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.height = height;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.integrity = integrity;
        this.chance = chance;
        this.threshold = (1.0 - chance) * 92.0;
        this.count = count;
        this.biomes = biomes;
        this.placer = placer;
        this.noise = FastNoise.createDescriptor().seed(placer.getId()).noise(NoiseType.SIMPLEX)
            .frequency((float) frequency).range(0.0F, (float) amplitude).generate();
    }

    public static GiantClusterConfig fromStem(final FeatureStem stem) {
        final GiantClusterSettings c = (GiantClusterSettings) stem.getConfig().getConfig();
        final SimplePlacementSettings d = (SimplePlacementSettings) stem.getConfig().getPlacement();

        return new GiantClusterConfig(c.getRadiusX(), c.getRadiusY(), c.getRadiusZ(), d.getHeight(),
            c.getFrequency(), c.getAmplitude(), c.getIntegrity(), d.getChance(), d.getCount(),
            stem.getConfig().getBiomes(), stem.getPlacer());
    }
}
