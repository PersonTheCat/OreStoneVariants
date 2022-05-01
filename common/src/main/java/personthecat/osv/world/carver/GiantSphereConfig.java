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
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.preset.data.GiantSphereSettings;
import personthecat.osv.preset.data.SimplePlacementSettings;
import personthecat.osv.preset.data.SphereSettings;
import personthecat.osv.world.placer.BlockPlacer;

import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.codec.FieldDescriptor.field;

public class GiantSphereConfig extends CarverConfiguration {

    private static final Codec<GiantSphereConfig> UNVALIDATED = CodecUtils.codecOf(
        defaulted(Range.CODEC, GiantSphereSettings.Fields.radiusX, Range.of(15, 30), c -> c.radiusX),
        defaulted(Range.CODEC, GiantSphereSettings.Fields.radiusY, Range.of(10, 20), c -> c.radiusY),
        defaulted(Range.CODEC, GiantSphereSettings.Fields.radiusZ, Range.of(15, 30), c -> c.radiusZ),
        defaulted(Range.CODEC, SimplePlacementSettings.Fields.height, Range.of(15, 40), c -> c.height),
        defaulted(Codec.DOUBLE, GiantSphereSettings.Fields.integrity, 1.0, c -> c.integrity),
        defaulted(Codec.DOUBLE, SimplePlacementSettings.Fields.chance, 0.025, c -> c.chance),
        defaulted(Codec.INT, SimplePlacementSettings.Fields.count, 1, c -> c.count),
        defaulted(BiomePredicate.CODEC, PlacedFeatureSettings.Fields.biomes, BiomePredicate.ALL_BIOMES, c -> c.biomes),
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
    final double threshold;
    final int count;
    final BiomePredicate biomes;
    final BlockPlacer placer;

    public GiantSphereConfig(final Range radiusX, final Range radiusY, final Range radiusZ, final Range height,
                             final double integrity, final double chance, final int count, final BiomePredicate biomes,
                             final BlockPlacer placer) {
        super(1.0F, ConstantHeight.ZERO, ConstantFloat.ZERO, VerticalAnchor.BOTTOM, CarverDebugSettings.DEFAULT);
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.height = height;
        this.integrity = integrity;
        this.chance = chance;
        this.threshold = (1.0 - chance) * 92.0;
        this.count = count;
        this.biomes = biomes;
        this.placer = placer;
    }

    public static GiantSphereConfig fromStem(final FeatureStem stem) {
        final GiantSphereSettings c = (GiantSphereSettings) stem.getConfig().getConfig();
        final SimplePlacementSettings d = (SimplePlacementSettings) stem.getConfig().getPlacement();

        return new GiantSphereConfig(c.getRadiusX(), c.getRadiusY(), c.getRadiusZ(), d.getHeight(),
            c.getIntegrity(), d.getChance(), d.getCount(), stem.getConfig().getBiomes(), stem.getPlacer());
    }
}
