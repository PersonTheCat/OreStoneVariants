package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import personthecat.catlib.data.Range;
import personthecat.osv.world.carver.GiantSphereCarver;
import personthecat.osv.world.carver.GlobalFeature;
import personthecat.osv.world.carver.GlobalFeatureProvider;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class GiantSphereSettings implements GlobalFeatureProvider<GiantSphereSettings> {

    @Default Range radiusX = Range.of(15, 30);
    @Default Range radiusY = Range.of(10, 20);
    @Default Range radiusZ = Range.of(15, 30);
    @Default double integrity = 1.0;

    private static final Range VALID_RADIUS = Range.of(1, 128);

    public static final Codec<GiantSphereSettings> CODEC = codecOf(
        defaulted(Range.CODEC, Fields.radiusX, Range.of(15, 30), GiantSphereSettings::getRadiusX),
        defaulted(Range.CODEC, Fields.radiusY, Range.of(10, 20), GiantSphereSettings::getRadiusY),
        defaulted(Range.CODEC, Fields.radiusZ, Range.of(15, 30), GiantSphereSettings::getRadiusZ),
        defaulted(Codec.DOUBLE, Fields.integrity, 1.0, GiantSphereSettings::getIntegrity),
        GiantSphereSettings::new
    ).flatXmap(c -> validate(c, c.radiusX, c.radiusY, c.radiusZ),
        c -> validate(c, c.radiusX, c.radiusY, c.radiusZ));

    public static <T> DataResult<T> validate(final T config, final Range... radii) {
        for (final Range radius : radii) {
            if (!(VALID_RADIUS.contains(radius.min) && VALID_RADIUS.contains(radius.max))) {
                return DataResult.error("Radius outside of range: " + VALID_RADIUS);
            }
        }
        return DataResult.success(config);
    }

    @Override
    public GlobalFeature<?> getFeatureType() {
        return GiantSphereCarver.INSTANCE;
    }

    @Override
    public Codec<GiantSphereSettings> codec() {
        return CODEC;
    }
}
