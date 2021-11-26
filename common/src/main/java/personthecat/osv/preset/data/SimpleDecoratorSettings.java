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

    @Default double chance = 1.0;
    @Default Range count = new Range(2);
    @Default Range height = new Range(0, 32);

    public static final Codec<SimpleDecoratorSettings> CODEC = codecOf(
        defaulted(Codec.DOUBLE, Fields.chance, 1.0, SimpleDecoratorSettings::getChance),
        defaulted(Range.CODEC, Fields.count, new Range(2), SimpleDecoratorSettings::getCount),
        defaulted(Range.CODEC, Fields.height, new Range(0, 32), SimpleDecoratorSettings::getHeight),
        SimpleDecoratorSettings::new
    );

    @Override
    public Codec<SimpleDecoratorSettings> codec() {
        return CODEC;
    }
}
