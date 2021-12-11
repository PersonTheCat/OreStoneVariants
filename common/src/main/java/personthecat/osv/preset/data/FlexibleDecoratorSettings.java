package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import personthecat.catlib.data.Range;
import personthecat.osv.world.decorator.DecoratorProvider;
import personthecat.osv.world.decorator.FlexibleDecoratorConfig;
import personthecat.osv.world.decorator.FlexibleVariantDecorator;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class FlexibleDecoratorSettings implements DecoratorProvider<FlexibleDecoratorSettings> {

    public static final FlexibleDecoratorSettings DEFAULTS = FlexibleDecoratorSettings.builder().build();

    @Default int spread = 0;
    @Default double chance = 1.0;
    @Default int bias = 0;
    @Default int extraCount = 0;
    @Default double extraChance = 0;
    @Default Range count = new Range(2);
    @Default Range height = new Range(0, 32);

    public static final Codec<FlexibleDecoratorSettings> CODEC = codecOf(
        defaulted(Codec.INT, Fields.spread, 0, FlexibleDecoratorSettings::getSpread),
        defaulted(Codec.DOUBLE, Fields.chance, 1.0, FlexibleDecoratorSettings::getChance),
        defaulted(Codec.INT, Fields.bias, 0, FlexibleDecoratorSettings::getBias),
        defaulted(Codec.INT, Fields.extraCount, 0, FlexibleDecoratorSettings::getExtraCount),
        defaulted(Codec.DOUBLE, Fields.extraChance, 0.0, FlexibleDecoratorSettings::getExtraChance),
        defaulted(Range.CODEC, Fields.count, new Range(2), FlexibleDecoratorSettings::getCount),
        defaulted(Range.CODEC, Fields.height, new Range(0, 32), FlexibleDecoratorSettings::getHeight),
        FlexibleDecoratorSettings::new
    );

    @Override
    public ConfiguredFeature<?, ?> decorate(ConfiguredFeature<?, ?> feature) {
        // Todo: This feature is old now. Needs to be updated.
        feature = feature.decorated(FlexibleVariantDecorator.INSTANCE.configured(
            new FlexibleDecoratorConfig(this.count, this.height, this.chance)));

        if (this.extraChance > 0 && this.extraCount > 0) {
            feature = feature.decorated(FeatureDecorator.COUNT_EXTRA.configured(
                new FrequencyWithExtraChanceDecoratorConfiguration(0, (float) this.extraChance, this.extraCount)));
        }

        return feature;
    }

    @Override
    public Codec<FlexibleDecoratorSettings> codec() {
        return CODEC;
    }
}
