package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import personthecat.catlib.data.Range;
import personthecat.osv.world.decorator.DecoratorProvider;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class FlexibleDecoratorSettings implements DecoratorProvider<FlexibleDecoratorSettings> {

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
    public ConfiguredFeature<?, ?> decorate(final ConfiguredFeature<?, ?> feature) {
        // Todo: This is just a demo.
        if (this.chance < 1.0) {
            return feature.decorated(FeatureDecorator.CHANCE
                .configured(new ChanceDecoratorConfiguration((int) (1.0 / this.chance))));
        }
        return feature;
    }

    @Override
    public Codec<FlexibleDecoratorSettings> codec() {
        return CODEC;
    }
}
