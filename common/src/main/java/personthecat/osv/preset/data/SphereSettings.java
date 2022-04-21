package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.catlib.data.Range;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.world.feature.FeatureProvider;
import personthecat.osv.world.feature.SphereConfig;
import personthecat.osv.world.feature.SphereFeature;
import personthecat.osv.world.placer.StoneBlockPlacer;
import personthecat.osv.world.placer.VariantBlockPlacer;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class SphereSettings implements FeatureProvider<SphereSettings> {

    @Default Range radiusX = Range.of(3, 5);
    @Default Range radiusY = Range.of(2, 4);
    @Default Range radiusZ = Range.of(3, 5);
    @Default double integrity = 1.0;

    private static final Range VALID_RADIUS = Range.of(1, 9);

    public static final Codec<SphereSettings> CODEC = codecOf(
        defaulted(Range.CODEC, Fields.radiusX, Range.of(3, 5), SphereSettings::getRadiusX),
        defaulted(Range.CODEC, Fields.radiusY, Range.of(3, 5), SphereSettings::getRadiusY),
        defaulted(Range.CODEC, Fields.radiusZ, Range.of(3, 5), SphereSettings::getRadiusZ),
        defaulted(Codec.DOUBLE, Fields.integrity, 1.0, SphereSettings::getIntegrity),
        SphereSettings::new
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
    public ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(SphereFeature.INSTANCE,
            new SphereConfig(this.radiusX, this.radiusY, this.radiusZ, this.integrity, new VariantBlockPlacer(cfg, ore)));
    }

    @Override
    public ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(SphereFeature.INSTANCE,
            new SphereConfig(this.radiusX, this.radiusY, this.radiusZ, this.integrity, new StoneBlockPlacer(stone)));
    }

    @Override
    public Codec<SphereSettings> codec() {
        return CODEC;
    }
}
