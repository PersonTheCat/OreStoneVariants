package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import personthecat.catlib.data.Range;
import personthecat.osv.world.decorator.NoneDecoratorProvider;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class SimpleDecoratorSettings implements NoneDecoratorProvider<SimpleDecoratorSettings> {

    @Default double chance = 0.15;
    @Default int count = 1;
    @Default Range height = new Range(15, 40);

    public static final Codec<SimpleDecoratorSettings> CODEC = codecOf(
        defaulted(Codec.DOUBLE, Fields.chance,  0.15, SimpleDecoratorSettings::getChance),
        defaulted(Codec.INT, Fields.count, 1, SimpleDecoratorSettings::getCount),
        defaulted(Range.CODEC, Fields.height, new Range(15, 40), SimpleDecoratorSettings::getHeight),
        SimpleDecoratorSettings::new
    );

    @Override
    public Codec<SimpleDecoratorSettings> codec() {
        return CODEC;
    }
}
