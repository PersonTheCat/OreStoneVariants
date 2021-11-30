package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import personthecat.catlib.data.Range;
import personthecat.osv.world.carver.GiantClusterCarver;
import personthecat.osv.world.carver.GlobalFeature;
import personthecat.osv.world.carver.GlobalFeatureProvider;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class GiantClusterSettings implements GlobalFeatureProvider<GiantClusterSettings> {

    @Default Range radiusX = Range.of(15, 30);
    @Default Range radiusY = Range.of(10, 20);
    @Default Range radiusZ = Range.of(15, 30);
    @Default double frequency = 0.1;
    @Default double amplitude = 0.1;
    @Default double integrity = 1.0;

    public static final Range VALID_RADIUS = Range.of(1, 250);

    public static final Codec<GiantClusterSettings> CODEC = codecOf(
        defaulted(Range.CODEC, Fields.radiusX, Range.of(15, 30), GiantClusterSettings::getRadiusX),
        defaulted(Range.CODEC, Fields.radiusY, Range.of(10, 20), GiantClusterSettings::getRadiusY),
        defaulted(Range.CODEC, Fields.radiusZ, Range.of(15, 30), GiantClusterSettings::getRadiusZ),
        defaulted(Codec.DOUBLE, Fields.frequency, 0.1, GiantClusterSettings::getFrequency),
        defaulted(Codec.doubleRange(0.0, 1.0), Fields.amplitude, 0.1, GiantClusterSettings::getAmplitude),
        defaulted(Codec.DOUBLE, Fields.integrity, 1.0, GiantClusterSettings::getIntegrity),
        GiantClusterSettings::new
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
        return GiantClusterCarver.INSTANCE;
    }

    @Override
    public Codec<GiantClusterSettings> codec() {
        return CODEC;
    }
}
