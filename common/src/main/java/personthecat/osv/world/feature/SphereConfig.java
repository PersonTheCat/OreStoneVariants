package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.data.Range;
import personthecat.osv.preset.data.SphereSettings;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.field;

public class SphereConfig implements FeatureConfiguration {

    private static final Codec<SphereConfig> UNVALIDATED = codecOf(
        defaulted(Range.CODEC, SphereSettings.Fields.radiusX, Range.of(3, 5), c -> c.radiusX),
        defaulted(Range.CODEC, SphereSettings.Fields.radiusY, Range.of(2, 4), c -> c.radiusY),
        defaulted(Range.CODEC, SphereSettings.Fields.radiusZ, Range.of(3, 5), c -> c.radiusZ),
        defaulted(Codec.DOUBLE, SphereSettings.Fields.integrity, 1.0, c -> c.integrity),
        field(BlockPlacer.EITHER_CODEC, "placer", c -> c.placer),
        SphereConfig::new
    );

    public static final Codec<SphereConfig> CODEC =
        UNVALIDATED.flatXmap(c -> SphereSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ),
            c -> SphereSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ));

    final Range radiusX;
    final Range radiusY;
    final Range radiusZ;
    final double integrity;
    final BlockPlacer placer;

    public SphereConfig(final Range radiusX, final Range radiusY, final Range radiusZ, final double integrity,
                        final BlockPlacer placer) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.integrity = integrity;
        this.placer = placer;
    }
}
