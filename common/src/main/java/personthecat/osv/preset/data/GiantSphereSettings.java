package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.data.Range;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.world.feature.*;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class GiantSphereSettings implements GlobalFeatureProvider<GiantSphereSettings> {

    @Default Range radiusX = Range.of(15, 30);
    @Default Range radiusY = Range.of(10, 20);
    @Default Range radiusZ = Range.of(15, 30);
    @Default Range height = Range.of(15, 40);
    @Default double integrity = 1.0;
    @Default double chance = 0.15;
    @Default int count = 1;

    private static final Range VALID_RADIUS = Range.of(1, 250);

    public static final Codec<GiantSphereSettings> CODEC = codecOf(
        defaulted(Range.CODEC, Fields.radiusX, Range.of(15, 30), GiantSphereSettings::getRadiusX),
        defaulted(Range.CODEC, Fields.radiusY, Range.of(10, 20), GiantSphereSettings::getRadiusY),
        defaulted(Range.CODEC, Fields.radiusZ, Range.of(15, 30), GiantSphereSettings::getRadiusZ),
        defaulted(Range.CODEC, Fields.height, Range.of(15, 40), GiantSphereSettings::getRadiusZ),
        defaulted(Codec.DOUBLE, Fields.integrity, 1.0, GiantSphereSettings::getIntegrity),
        defaulted(Codec.DOUBLE, Fields.chance, 0.15, GiantSphereSettings::getChance),
        defaulted(Codec.INT, Fields.count, 1, GiantSphereSettings::getCount),
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
        return GiantSphereFeature.INSTANCE;
    }

    @Override
    public Codec<GiantSphereSettings> codec() {
        return CODEC;
    }
}
